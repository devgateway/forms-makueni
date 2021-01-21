package org.devgateway.ocds.web.db;

import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.persistence.mongo.repository.main.ProcurementPlanMongoRepository;
import org.devgateway.ocds.web.convert.MongoFileStorageService;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.Statusable;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.persistence.repository.AdminSettingsRepository;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author idobre
 * @since 2019-07-02
 * <p>
 * <p>
 * Imports all the makueni data into the mongo db.
 */
@Service
public class ImportPostgresToMongo {
    private static final Logger logger = LoggerFactory.getLogger(ImportPostgresToMongo.class);

    @Resource
    private ImportPostgresToMongo self; // Self-autowired reference to proxified bean of this class.

    @Autowired
    private ProcurementPlanService procurementPlanService;

    @Autowired
    private ProcurementPlanMongoRepository procurementPlanMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private MongoFileStorageService mongoFileStorageService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private DgFmService fmService;

    @Transactional(readOnly = true)
    public void formStatusIntegrityCheck() {
        logger.info("Checking forms status integrity...");
        StringBuffer sb = new StringBuffer();
        tenderProcessService.findAll().forEach(e -> formStatusIntegrityCheck(e, sb));
        logger.info("Forms status integrity check done.");

        logger.info("Form Status Integrity Checks Failure: " + sb.toString());
        
        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage, "UTF-8");
            msg.setTo(SecurityUtil.getSuperAdminEmail(adminSettingsRepository));
            msg.setFrom(DBConstants.FROM_EMAIL);
            msg.setSubject("Form Status Integrity Checks Failure");
            msg.setText(sb.toString());
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send ocds validation failure emails ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public void formStatusIntegrityCheck(TenderProcess tp, StringBuffer sb) {
        if (fmService.isFeatureVisible("purchaseRequisitionForm")
                && tp.getSinglePurchaseRequisition() != null) {
            formStatusIntegrityCheck(tp.getSinglePurchaseRequisition(), sb);
        } else {
            formStatusIntegrityCheck(tenderProcessService.getNextStatusable(tp, PurchaseRequisitionGroup.class), sb);
        }
    }

    @Transactional(readOnly = true)
    public void formStatusIntegrityCheck(AbstractMakueniEntity p, StringBuffer sb) {
        if (p == null) {
            return;
        }
        p.getDirectChildrenEntitiesNotNull().forEach(e -> {
            if (!goodParentStatus(p.getStatus(), e.getStatus())) {
                sb.append("Parent ").append(p.getClass().getSimpleName()).append(" with id ").append(p.getId())
                        .append(" and status ").append(p.getStatus()).append(" has child ")
                        .append(e.getClass().getSimpleName())
                        .append(" with id ").append(e.getId()).append(" and status ").append(e.getStatus())
                        .append("\n");
            }
            formStatusIntegrityCheck(e, sb);
        });
    }

    @Transactional(readOnly = true)
    public boolean goodParentStatus(String parentStatus, String childStatus) {
        return !DBConstants.Status.DRAFT.equals(parentStatus) || DBConstants.Status.DRAFT.equals(childStatus);
    }

    @Transactional(readOnly = true)
    public void importToMongo() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Mongo import started");

        // first clean the procurement plan collection and delete all saved files
        mongoTemplate.dropCollection(ProcurementPlan.class);
        //gridFsOperations.delete(new Query());

        procurementPlanService.findAllStream().filter(Statusable::isExportable).forEach(pp -> {
            pp.setProjects(new HashSet<>(filterNotExportable(pp.getProjects())));
                pp.getTenderProcesses().stream().forEach(pr -> {
                    pr.setPurchaseRequisition(new HashSet<>(filterNotExportable(pr.getPurchaseRequisition())));
                    pr.getPurchaseRequisition().stream().flatMap(i -> i.getPurchRequisitions().stream())
                            .forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));
                    pr.getPurchaseRequisition().stream().forEach(item -> self.storeMakueniFormFiles(
                            item.getFormDocs()));

                    pr.setTender(new HashSet<>(filterNotExportable(pr.getTender())));
                    pr.getTender().stream().forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));

                    pr.setTenderQuotationEvaluation(new HashSet<>(
                            filterNotExportable(pr.getTenderQuotationEvaluation())));
                    pr.getTenderQuotationEvaluation().stream()
                            .forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));

                    pr.setProfessionalOpinion(new HashSet<>(filterNotExportable(pr.getProfessionalOpinion())));
                    pr.getProfessionalOpinion().stream().flatMap(i -> i.getItems().stream())
                            .forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));

                    pr.setAwardNotification(new HashSet<>(filterNotExportable(pr.getAwardNotification())));
                    pr.getAwardNotification().stream().flatMap(i -> i.getItems().stream())
                            .forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));

                    pr.setAwardAcceptance(new HashSet<>(filterNotExportable(pr.getAwardAcceptance())));
                    pr.getAwardAcceptance().stream().flatMap(i -> i.getItems().stream())
                            .forEach(item -> self.storeMakueniFormFiles(item.getFormDocs()));

                    pr.setContract(new HashSet<>(filterNotExportable(pr.getContract())));
                    pr.getContract().stream()
                            .forEach(item -> item.getContractDocs().stream()
                                    .forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs())));

                    pr.setAdministratorReports(new HashSet<>(filterNotExportable(pr.getAdministratorReports())));
                    pr.getAdministratorReports().stream().forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs()));

                    pr.setInspectionReports(new HashSet<>(filterNotExportable(pr.getInspectionReports())));
                    pr.getInspectionReports().stream().forEach(doc -> {
                        self.storeMakueniFormFiles(doc.getFormDocs());
                        doc.getPrivateSectorRequests().stream()
                                .forEach(psr -> self.storeMakueniFormFiles(psr.getUpload()));
                    });

                    pr.setPmcReports(new HashSet<>(filterNotExportable(pr.getPmcReports())));
                    pr.getPmcReports().stream().forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs()));

                    pr.setMeReports(new HashSet<>(filterNotExportable(pr.getMeReports())));
                    pr.getMeReports().stream().forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs()));

                    pr.setPaymentVouchers(new HashSet<>(filterNotExportable(pr.getPaymentVouchers())));
                    pr.getPaymentVouchers().stream().forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs()));
                });

                pp.getProjects().stream().flatMap(p->p.getCabinetPapers().stream()).
                        forEach(doc -> self.storeMakueniFormFiles(doc.getFormDocs()));

            self.storeMakueniFormFiles(pp.getFormDocs());
            procurementPlanMongoRepository.save(pp);
            logger.info("Procurement Plan " + pp.getId() + " " + pp.getLabel() + " saved to MongoDB.");
        });

        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("status", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("department._id", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("fiscalYear._id", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("projects.subcounties._id", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("projects.wards._id", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("tenderProcesses.tender.tenderItems.purchaseItem.planItem.item._id",
                        Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("tenderProcesses.tender.tenderValue", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("tenderProcesses.tender.closingDate", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("tenderProcesses.lastModifiedDate", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new Index().on("tenderProcesses.tender.lastModifiedDate", Sort.Direction.ASC));

        Document fyDepartmentIndex = new Document();
        fyDepartmentIndex.put("fiscalYear.startDate", -1);
        fyDepartmentIndex.put("department.label", 1);
        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(new CompoundIndexDefinition(fyDepartmentIndex));

        mongoTemplate.indexOps(ProcurementPlan.class).ensureIndex(
                new TextIndexDefinition.TextIndexDefinitionBuilder()
                        .withDefaultLanguage(MongoConstants.MONGO_LANGUAGE)
                        .onField("tenderProcesses.tender.tenderTitle")
                        .onField("projects.projectTitle")
                        .build());

        // clear cache
        cacheManager.getCacheNames().forEach(c -> cacheManager.getCache(c).clear());

        stopWatch.stop();
        logger.info("Mongo import ended in: " + stopWatch.getTime() + "ms");
    }

    public void storeMakueniFormFiles(final Set<FileMetadata> formDocs) {
        for (final FileMetadata fileMetadata : formDocs) {
            mongoFileStorageService.storeFile(fileMetadata);
        }
    }

    /**
     * Filter a {@link Statusable} collection and keep only the exportable items.
     *
     * @param collection
     * @param <S>
     * @return
     */
    private <S extends Statusable> List<S> filterNotExportable(final Collection<S> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }

        return collection.stream()
                .filter(item -> item.isExportable())
                .collect(Collectors.toList());
    }
}
