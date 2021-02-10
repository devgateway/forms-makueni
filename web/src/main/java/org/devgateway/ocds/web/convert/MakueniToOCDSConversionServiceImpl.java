package org.devgateway.ocds.web.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.WordUtils;
import org.devgateway.jocds.OcdsValidatorConstants;
import org.devgateway.jocds.OcdsValidatorNodeRequest;
import org.devgateway.jocds.OcdsValidatorService;
import org.devgateway.ocds.persistence.mongo.Address;
import org.devgateway.ocds.persistence.mongo.Amount;
import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.Bids;
import org.devgateway.ocds.persistence.mongo.Budget;
import org.devgateway.ocds.persistence.mongo.Classification;
import org.devgateway.ocds.persistence.mongo.ContactPoint;
import org.devgateway.ocds.persistence.mongo.Contract;
import org.devgateway.ocds.persistence.mongo.Detail;
import org.devgateway.ocds.persistence.mongo.Document;
import org.devgateway.ocds.persistence.mongo.Identifier;
import org.devgateway.ocds.persistence.mongo.Implementation;
import org.devgateway.ocds.persistence.mongo.Item;
import org.devgateway.ocds.persistence.mongo.MakueniBudget;
import org.devgateway.ocds.persistence.mongo.MakueniBudgetBreakdown;
import org.devgateway.ocds.persistence.mongo.MakueniContract;
import org.devgateway.ocds.persistence.mongo.MakueniItem;
import org.devgateway.ocds.persistence.mongo.MakueniLocation;
import org.devgateway.ocds.persistence.mongo.MakueniLocationType;
import org.devgateway.ocds.persistence.mongo.MakueniMilestone;
import org.devgateway.ocds.persistence.mongo.MakueniOrganization;
import org.devgateway.ocds.persistence.mongo.MakueniPlanning;
import org.devgateway.ocds.persistence.mongo.MakueniTender;
import org.devgateway.ocds.persistence.mongo.Milestone;
import org.devgateway.ocds.persistence.mongo.Organization;
import org.devgateway.ocds.persistence.mongo.OrganizationReference;
import org.devgateway.ocds.persistence.mongo.Period;
import org.devgateway.ocds.persistence.mongo.Release;
import org.devgateway.ocds.persistence.mongo.Tag;
import org.devgateway.ocds.persistence.mongo.Tender;
import org.devgateway.ocds.persistence.mongo.Transaction;
import org.devgateway.ocds.persistence.mongo.Unit;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.persistence.mongo.repository.main.MakueniLocationRepository;
import org.devgateway.ocds.persistence.mongo.repository.main.OrganizationRepository;
import org.devgateway.ocds.persistence.mongo.repository.main.ReleaseRepository;
import org.devgateway.ocds.web.rest.controller.OcdsController;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.categories.Category;
import org.devgateway.toolkit.persistence.dao.categories.ContractDocumentType;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.categories.LocationPointCategory;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethod;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.dao.form.AbstractAuthImplTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AbstractImplTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptanceItem;
import org.devgateway.toolkit.persistence.dao.form.AwardNotificationItem;
import org.devgateway.toolkit.persistence.dao.form.Bid;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.dao.form.ContractDocument;
import org.devgateway.toolkit.persistence.dao.form.FiscalYearBudget;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PaymentVoucher;
import org.devgateway.toolkit.persistence.dao.form.PlanItem;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.PurchRequisition;
import org.devgateway.toolkit.persistence.dao.form.PurchaseItem;
import org.devgateway.toolkit.persistence.dao.form.Statusable;
import org.devgateway.toolkit.persistence.dao.form.TenderItem;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.persistence.repository.AdminSettingsRepository;
import org.devgateway.toolkit.persistence.service.PersonService;
import org.devgateway.toolkit.persistence.service.form.FiscalYearBudgetService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.web.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.MONGO_DECIMAL_SCALE;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.OCDSSchemes.X_KE_INTERNAL_SCHEMA;

@Service
@Transactional(readOnly = true)
public class MakueniToOCDSConversionServiceImpl implements MakueniToOCDSConversionService {

    @Value("${serverURL}")
    private String serverURL;

    @Autowired
    private DgFmService fmService;

    private static final Logger logger = LoggerFactory.getLogger(MakueniToOCDSConversionServiceImpl.class);

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private OcdsValidatorService ocdsValidatorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    private StringBuffer validationErrors;

    private static final String OCID_PREFIX = "ocds-muq5cl-";

    private ImmutableMap<String, Tender.ProcurementMethod> procurementMethodMap;

    private ImmutableMap<String, Milestone.Status> meMilestoneMap;

    @Autowired
    private MongoFileStorageService mongoFileStorageService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private MakueniLocationRepository makueniLocationRepository;

    @Autowired
    private FiscalYearBudgetService fiscalYearBudgetService;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private PersonService personService;

    private void sendValidationFailureAlert(String txt) {
        if (txt.isEmpty()) {
            return;
        }
        
        logger.info("OCDS Validation Failures After Import: " + txt);

        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage, "UTF-8");
            msg.setTo(SecurityUtil.getSuperAdminEmail(adminSettingsRepository));
            msg.setFrom(DBConstants.FROM_EMAIL);
            msg.setSubject("OCDS Validation Failures After Import");
            msg.setText(txt);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send ocds validation failure emails ", e);
            throw e;
        }
    }


    public MakueniLocation lpcToMakueniLocation(LocationPointCategory lpc) {
        MakueniLocation makueniLocation = makueniLocationRepository.findByDescription(lpc.getLabel());
        if (makueniLocation != null) {
            return makueniLocation;
        }
        MakueniLocation ml = new MakueniLocation();
        ml.setDescription(lpc.getLabel());
        if (lpc instanceof Ward) {
            ml.setType(MakueniLocationType.ward);
        } else if (lpc instanceof Subcounty) {
            ml.setType(MakueniLocationType.subcounty);
        } else {
            throw new RuntimeException("Unkown Makueni location type");
        }
        ml.setGeometry(new GeoJsonPoint(lpc.getLocationPoint().getX(), lpc.getLocationPoint().getY()));
        makueniLocation = makueniLocationRepository.save(ml);
        return makueniLocation;
    }


    public MakueniTender createTender(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        MakueniTender ocdsTender = new MakueniTender();
        safeSet(ocdsTender::setId, tender::getId, this::longIdToString);
        safeSet(ocdsTender::setTitle, tender::getTitle);
        safeSet(ocdsTender::setTenderPeriod, () -> tender, this::createTenderPeriod);
        safeSet(ocdsTender::setProcurementMethod, tender::getProcurementMethod, this::createProcurementMethod);
        safeSet(ocdsTender::setProcurementMethodRationale, tender::getProcurementMethodRationale,
                this::categoryLabel);

        safeSetEach(ocdsTender.getItems()::add, tender::getTenderItems, this::createTenderItem);
        safeSet(ocdsTender::setDescription, tender::getObjective);
        safeSet(ocdsTender::setProcuringEntity, tender::getIssuedBy, this::convertProcuringEntity);
        safeSet(ocdsTender::setValue, tender::getTenderValue, this::convertAmount);
        safeSet(ocdsTender::setTargetGroup, tender::getTargetGroup, this::categoryLabel);
        safeSet(ocdsTender::setStatus, () -> tender, this::createTenderStatus);
        safeSet(ocdsTender::setNumberOfTenderers, () -> tender.getTenderProcess().getSingleTenderQuotationEvaluation(),
                this::convertNumberOfTenderers
        );

        safeSetEach(ocdsTender.getLocations()::add, tenderProjectWards(tender), this::lpcToMakueniLocation);
        safeSetEach(ocdsTender.getLocations()::add, tenderProjectSubcounties(tender), this::lpcToMakueniLocation);

        //documents
        safeSet(ocdsTender.getDocuments()::add, tender::getFormDoc, this::storeAsDocumentTenderNotice);
        safeSet(ocdsTender.getDocuments()::add, tender::getTenderLink, this::createDocumentFromUrlTenderNotice);
        safeSetEach(ocdsTender.getDocuments()::add,
                () -> tender.getTenderProcess().getPurchaseRequisition().stream().filter(Statusable::isExportable)
                        .flatMap(pr->pr.getPurchRequisitions().stream())
                        .flatMap(prr->prr.getFormDocs().stream()).collect(Collectors.toList()),
                this::storeAsDocumentApprovedPurchaseRequisition
        );

        safeSetEach(ocdsTender.getDocuments()::add,
                () -> Optional.ofNullable(tender.getTenderProcess().getSingleTenderQuotationEvaluation())
                        .map(TenderQuotationEvaluation::getFormDocs).orElse(null),
                this::storeAsDocumentEvaluationReports
        );


        return ocdsTender;
    }

    public Supplier<Collection<Ward>> tenderProjectWards(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        return tender == null || tender.getProject() == null ? null : tender.getProject()::getWards;
    }

    public Supplier<Collection<Subcounty>> tenderProjectSubcounties(org.devgateway.toolkit.persistence.dao.form.Tender
                                                                          tender) {
        return tender == null || tender.getProject() == null ? null : tender.getProject()::getSubcounties;
    }


    public OrganizationReference addOrUpdateOrganizationSetByRole(HashMap<String, Organization> orgs,
                                                                  OrganizationReference or) {
        if (ObjectUtils.isEmpty(or)) {
            return null;
        }
        Organization o = (Organization) or;
        if (!orgs.containsKey(o.getId())) {
            orgs.put(o.getId(), o);
        } else {
            orgs.get(o.getId()).getRoles().addAll(o.getRoles());
        }

        return orgToReference(or);
    }

    public Collection<Organization> createParties(Release release) {
        HashMap<String, Organization> orgs = new HashMap<>();
        release.setBuyer(addOrUpdateOrganizationSetByRole(orgs, release.getBuyer()));

        if (!ObjectUtils.isEmpty(release.getTender())) {
            release.getTender().setProcuringEntity(addOrUpdateOrganizationSetByRole(orgs,
                    release.getTender().getProcuringEntity()));
        }

        if (!ObjectUtils.isEmpty(release.getBids())) {
            release.getBids().getDetails().stream().filter(d -> !ObjectUtils.isEmpty(d.getTenderers()))
                    .forEach(d -> d.setTenderers(d.getTenderers().stream()
                            .map(s -> addOrUpdateOrganizationSetByRole(orgs, s)).collect(Collectors.toSet())));
        }

        if (!ObjectUtils.isEmpty(release.getTender())) {
            release.getTender().setTenderers(release.getTender().getTenderers().stream().map(
                    s -> addOrUpdateOrganizationSetByRole(orgs, s)).collect(Collectors.toSet()));
        }

        if (!ObjectUtils.isEmpty(release.getAwards())) {
            release.getAwards().stream().filter(a -> !a.getSuppliers().isEmpty())
                    .forEach(a -> a.setSuppliers(a.getSuppliers().stream().map(
                            s -> addOrUpdateOrganizationSetByRole(orgs, s)).collect(Collectors.toSet())));
        }

        if (!ObjectUtils.isEmpty(release.getContracts())) {
            release.getContracts().stream().
                    map(Contract::getImplementation).filter(Objects::nonNull)
                    .forEach(i -> i.getTransactions().forEach(t -> {
                        t.setPayee(addOrUpdateOrganizationSetByRole(orgs, t.getPayee()));
                        t.setPayer(addOrUpdateOrganizationSetByRole(orgs, t.getPayer()));
                    }));
            release.getContracts().forEach(c -> ((MakueniContract) c).setContractor(
                    addOrUpdateOrganizationSetByRole(orgs, ((MakueniContract) c).getContractor())
            ));
        }

        if (!ObjectUtils.isEmpty(release.getPlanning().getBudget())) {
            Set<MakueniBudgetBreakdown> budgetBreakdown = ((MakueniBudget) release.getPlanning().getBudget())
                    .getBudgetBreakdown();
            if (budgetBreakdown != null) {
                budgetBreakdown.forEach(bb -> bb.setSourceParty(addOrUpdateOrganizationSetByRole(orgs,
                        bb.getSourceParty())));
            }
        }


        return orgs.values();
    }


    public Integer convertNumberOfTenderers(TenderQuotationEvaluation tenderQuotationEvaluation) {
        return tenderQuotationEvaluation.getBids().size();
    }

    public Tender.Status createTenderStatus(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        if (tender.getTenderProcess().isTerminated()) {
            return Tender.Status.cancelled;
        }

        org.devgateway.toolkit.persistence.dao.form.Contract contract =
                Optional.ofNullable(tender.getTenderProcess().getSingleContract())
                .filter(Statusable::isExportable)
               .orElse(null);
        if (!ObjectUtils.isEmpty(contract)) {
            return Tender.Status.complete;
        }

        if (PersistenceUtil.convertDateToZonedDateTime(tender.getInvitationDate()).isBefore(
                PersistenceUtil.currentDate())) {
            return Tender.Status.planned;
        }
        return Tender.Status.active;
    }

    public String convertIdentifierToId(Identifier identifier) {
        return identifier.getScheme() + "-" + identifier.getId();
    }

    public Organization convertBuyer(Department department) {
        Organization ocdsBuyer = new Organization();
        safeSet(ocdsBuyer::setName, () -> department, this::categoryLabel, WordUtils::capitalizeFully);
        safeSet(ocdsBuyer::setIdentifier, () -> department, this::convertCategoryToIdentifier,
                this::convertToLocalIdentifier);
        safeSet(ocdsBuyer::setId, ocdsBuyer::getIdentifier, Identifier::getId);
        safeSet(ocdsBuyer.getRoles()::add, () -> Organization.OrganizationType.buyer,
                Organization.OrganizationType::toValue);
        return ocdsBuyer;
    }

    public String entityIdToString(GenericPersistable p) {
        return p.getId().toString();
    }


    public Organization convertProcuringEntity(ProcuringEntity procuringEntity) {
        Organization ocdsProcuringEntity = new Organization();
        safeSet(ocdsProcuringEntity::setIdentifier, () -> procuringEntity, this::convertCategoryToIdentifier,
                this::convertToLocalIdentifier);
        safeSet(ocdsProcuringEntity::setId, ocdsProcuringEntity::getIdentifier, Identifier::getId);
        safeSet(ocdsProcuringEntity.getAdditionalIdentifiers()::add, () -> procuringEntity,
                this::convertCategoryCodeToIdentifier, this::convertToOrgIdentifier);

        safeSet(ocdsProcuringEntity::setAddress, () -> procuringEntity, this::createProcuringEntityAddress);
        safeSet(ocdsProcuringEntity::setName, procuringEntity::getLabel, WordUtils::capitalizeFully);
        safeSet(ocdsProcuringEntity::setContactPoint, () -> procuringEntity, this::createProcuringEntityContactPoint);
        safeSet(ocdsProcuringEntity.getRoles()::add, () -> Organization.OrganizationType.procuringEntity,
                Organization.OrganizationType::toValue
        );

        return ocdsProcuringEntity;
    }

    public String categoryLabel(Category category) {
        return category.getLabel();
    }

    public ContactPoint createProcuringEntityContactPoint(ProcuringEntity procuringEntity) {
        ContactPoint ocdsContactPoint = new ContactPoint();
        safeSet(ocdsContactPoint::setEmail, procuringEntity::getEmailAddress);
        return ocdsContactPoint;
    }

    public Address createProcuringEntityAddress(ProcuringEntity procuringEntity) {
        Address ocdsAddress = new Address();
        safeSet(ocdsAddress::setCountryName, this::getCountry);
        safeSet(ocdsAddress::setStreetAddress, procuringEntity::getAddress);
        return ocdsAddress;
    }

    public String getCountry() {
        return "Kenya";
    }


    public Item createTenderItem(TenderItem tenderItem) {
        Item ocdsItem = new Item();
        safeSet(ocdsItem::setId, tenderItem::getId, this::longIdToString);
        safeSet(ocdsItem::setUnit, () -> tenderItem, this::createTenderItemUnit);
        safeSet(ocdsItem::setQuantity, tenderItem::getQuantity, BigDecimal::doubleValue);
        safeSet(ocdsItem::setClassification, tenderItem::getNonNullPlanItem,
                this::createPlanItemClassification);
        return ocdsItem;
    }


    public Unit createTenderItemUnit(TenderItem tenderItem) {
        Unit unit = new Unit();
        safeSet(unit::setScheme, () -> MongoConstants.OCDSSchemes.UNCEFACT);
        safeSet(unit::setName, tenderItem::getNonNullPlanItem, PlanItem::getUnitOfIssue,
                Category::getLabel
        );

        safeSet(unit::setId, tenderItem::getNonNullPlanItem,  PlanItem::getUnitOfIssue,
                Category::getCode
        );
        safeSet(unit::setValue, tenderItem::getUnitPrice, this::convertAmount);
        return unit;
    }

    public Classification createPlanItemClassification(PlanItem planItem) {
        Classification classification = new Classification();
        safeSet(classification::setScheme, () -> MongoConstants.OCDSSchemes.X_KE_IFMIS);
        safeSet(classification::setId, () -> planItem, PlanItem::getItem, Category::getCode
        );
        safeSet(classification::setDescription, () -> planItem, PlanItem::getItem, Category::getLabel,
                WordUtils::capitalizeFully
        );
        return classification;
    }

    public Tender.ProcurementMethod createProcurementMethod(ProcurementMethod procurementMethod) {
        Tender.ProcurementMethod pm = procurementMethodMap.get(procurementMethod.getLabel());
        if (pm == null) {
            throw new RuntimeException("Procurement method mapping unknown " + procurementMethod);
        }
        return pm;
    }

    public Milestone.Status createMeReportMilestoneStatus(MEReport report) {
        return meMilestoneMap.get(report.getMeStatus().getCode());
    }

    @PostConstruct
    public void init() {
        procurementMethodMap = ImmutableMap.<String, Tender.ProcurementMethod>builder()
                .put("Direct Procurement", Tender.ProcurementMethod.direct)
                .put("Open Tender - National", Tender.ProcurementMethod.open)
                .put("Request for Proposal", Tender.ProcurementMethod.limited)
                .put("Request for Quotation", Tender.ProcurementMethod.selective)
                .put("Restricted Tender", Tender.ProcurementMethod.limited)
                .put("Specially Permitted", Tender.ProcurementMethod.limited)
                .put("Low Value Procurement", Tender.ProcurementMethod.direct)
                .put("Framework Agreement", Tender.ProcurementMethod.direct)
                .put("Two-stage Tendering", Tender.ProcurementMethod.selective)
                .put("Design Competition", Tender.ProcurementMethod.selective)
                .put("Force Account", Tender.ProcurementMethod.direct)
                .put("Electronic Reverse Auction", Tender.ProcurementMethod.selective)
                .put("Open Tender - International", Tender.ProcurementMethod.open).build();

        meMilestoneMap = ImmutableMap.<String, Milestone.Status>builder()
                .put("completedNotInUse", Milestone.Status.MET)
                .put("completeInUse", Milestone.Status.MET)
                .put("ongoing", Milestone.Status.NOT_MET)
                .put("notStarted", Milestone.Status.NOT_MET)
                .put("stalled", Milestone.Status.NOT_MET)
                .put("delayed", Milestone.Status.NOT_MET)
                .build();
    }

    public Period createTenderPeriod(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        Period period = new Period();
        safeSet(period::setStartDate, tender::getInvitationDate);
        safeSet(period::setEndDate, tender::getClosingDate);
        return period;
    }

    public Milestone createAuthImplMilestone(AbstractAuthImplTenderProcessMakueniEntity report) {
        MakueniMilestone milestone = new MakueniMilestone();
        safeSet(milestone::setId, report::getId, this::longIdToString);
        safeSet(milestone::setTitle, () -> "Payment Authorization " + report.getId());
        safeSet(milestone::setType, Milestone.MilestoneType.FINANCING::toString);
        safeSet(milestone::setCode, () -> report.getClass().getSimpleName());
        safeSet(milestone::setDateModified, report::getApprovedDate);
        safeSet(milestone::setAuthorizePayment, report::getAuthorizePayment);
        safeSet(milestone::setDateMet, () -> BooleanUtils.isTrue(report.getAuthorizePayment())
                ? report.getApprovedDate() : null);
        safeSet(milestone::setStatus,
                () -> BooleanUtils.isTrue(report.getAuthorizePayment())
                        ? Milestone.Status.MET : Milestone.Status.NOT_MET);
        safeSetEach(milestone.getDocuments()::add, report::getFormDocs, this::storeAsDocumentPhProgressReport);
        return milestone;
    }


    public OrganizationReference orgToReference(OrganizationReference org) {
        if (org == null) {
            return null;
        }
        OrganizationReference or = new OrganizationReference();
        or.setId(org.getId());
        or.setName(org.getName());
        return or;
    }

    public Milestone createMEMilestone(MEReport report) {
        MakueniMilestone milestone = new MakueniMilestone();
        safeSet(milestone::setId, report::getId, this::longIdToString);
        safeSet(milestone::setTitle, () -> "ME Report " + report.getId());
        safeSet(milestone::setType, Milestone.MilestoneType.DELIVERY::toString);
        safeSet(milestone::setCode, () -> report.getClass().getSimpleName());
        safeSet(milestone::setDateModified, report::getApprovedDate);
        safeSet(milestone::setDescription, report::getProjectProgress);
        safeSet(milestone::setStatus, () -> report, this::createMeReportMilestoneStatus);
        safeSet(milestone::setDelayed, () -> report.getMeStatus().getLabel().equals("Delayed") ? true : false);
        safeSetEach(milestone.getDocuments()::add, report::getFormDocs, this::storeAsDocumentEvaluationReports);
        return milestone;
    }

    public Transaction createPaymentVoucherTransaction(PaymentVoucher voucher) {
        Transaction transaction = new Transaction();
        safeSet(transaction::setId, voucher::getId, this::longIdToString);
        safeSet(transaction::setDate, voucher::getApprovedDate);
        safeSet(transaction::setAmount, voucher::getTotalAmount, this::convertAmount);
        safeSet(transaction::setPayer, voucher::getDepartment, this::convertBuyer, this::addPayerRole);
        safeSet(transaction::setPayee, voucher::getContract,
                org.devgateway.toolkit.persistence.dao.form.Contract::getAwardee, this::convertOrganization,
                this::addPayeeRole);
        return transaction;
    }

    public MakueniBudgetBreakdown createPlanningBudgetBreakdown(TenderProcess tenderProcess) {
        FiscalYearBudget fiscalYearBudget = fiscalYearBudgetService.findByDepartmentAndFiscalYear(
                tenderProcess.getDepartment(), tenderProcess.getProcurementPlan().getFiscalYear());
        MakueniBudgetBreakdown budgetBreakdown = null;
        if (fiscalYearBudget != null) {
            budgetBreakdown = new MakueniBudgetBreakdown();
            safeSet(budgetBreakdown::setAmount, fiscalYearBudget::getAmountBudgeted, this::convertAmount);
            safeSet(budgetBreakdown::setSourceParty, tenderProcess::getDepartment, this::convertBuyer);
            safeSet(budgetBreakdown::setId, fiscalYearBudget::getId, this::longIdToString);
            safeSet(budgetBreakdown::setPeriod, fiscalYearBudget::getFiscalYear, this::createBudgetPeriod);
        }
        return budgetBreakdown;
    }

    public Period createBudgetPeriod(FiscalYear fiscalYear) {
        Period period = new Period();
        safeSet(period::setStartDate, fiscalYear::getStartDate);
        safeSet(period::setEndDate, fiscalYear::getEndDate);
        return period;
    }

    public Budget createPlanningBudget(TenderProcess tenderProcess) {
        MakueniBudget budget = new MakueniBudget();

        safeSet(budget::setProject, tenderProcessProjectTitle(tenderProcess));
        safeSet(budget::setProjectID, tenderProcess::getProject, this::entityIdToString);
        safeSet(budget::setAmount, () -> tenderProcess.getPurchaseRequisition().
                stream().filter(Statusable::isExportable).flatMap(p->p.getPurchRequisitions().stream())
                .flatMap(pr -> pr.getPurchaseItems().stream()).map(PurchaseItem::getAmount).reduce(
                BigDecimal.ZERO.setScale(MONGO_DECIMAL_SCALE), BigDecimal::add), this::convertAmount);

        safeSet(budget.getBudgetBreakdown()::add, () -> tenderProcess, this::createPlanningBudgetBreakdown);

        return budget;
    }

    public Supplier<String> tenderProcessProjectTitle(TenderProcess tenderProcess) {
        return tenderProcess == null || tenderProcess.getProject() == null ? null : tenderProcess.getProject()
                ::getProjectTitle;
    }


    private Document storeAsDocumentProcurementPlan(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.PROCUREMENT_PLAN);
    }

    private Document storeAsDocumentProjectPlan(AbstractMakueniEntity entity) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                entity.getFormDoc(),
                Document.DocumentType.PROJECT_PLAN
        );
    }

    private Document storeAsDocumentPhProgressReport(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.PHYSICAL_PROGRESS_REPORT
        );
    }

    private Document storeAsDocumentFinProgressReport(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.FINANCIAL_PROGRESS_REPORT
        );
    }

    private Document storeAsDocumentTenderNotice(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.TENDER_NOTICE);
    }


    private Document storeAsDocumentAwardNotice(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.AWARD_NOTICE);
    }

    private Document storeAsDocumentProfessionalOpinion(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.X_EVALUATION_PROFESSIONAL_OPINION
        );
    }

    private Document storeAsDocumentAwardAcceptance(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.X_AWARD_ACCEPTANCE
        );
    }


    private Document storeAsDocumentEvaluationReports(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm, Document.DocumentType.EVALUATION_REPORTS);
    }

    private Document storeAsDocumentApprovedPurchaseRequisition(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm,
                Document.DocumentType.X_APPROVED_PURCHASE_REQUISITION);
    }

    private Document storeAsDocumentContractNotice(ContractDocument contractDocument) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                PersistenceUtil.getNext(contractDocument.getFormDocs()),
                Document.DocumentType.CONTRACT_SIGNED);
    }


    private Document createDocumentFromUrlTenderNotice(String url) {
        return createDocumentFromUrl(url, Document.DocumentType.TENDER_NOTICE);
    }

    private Document createDocumentFromUrl(String url, Document.DocumentType documentType) {
        Document document = new Document();
        safeSet(document::setId, () -> url);
        safeSet(document::setUrl, () -> url, URI::create);
        safeSet(document::setDocumentType, documentType::toString);
        return document;
    }

    public MakueniPlanning createPlanning(TenderProcess tenderProcess) {
        MakueniPlanning planning = new MakueniPlanning();

        safeSet(planning::setBudget, () -> tenderProcess, this::createPlanningBudget);
        safeSet(planning::setFiscalYear, tenderProcess::getProcurementPlan, ProcurementPlan::getFiscalYear,
                FiscalYear::getLabel
        );

        safeSetEach(planning.getItems()::add, () -> tenderProcess
                .getPurchaseRequisition().stream().filter(Statusable::isExportable)
                .flatMap(pr->pr.getPurchaseItems().stream()).collect(Collectors.toList()), this::createPlanningItem);

        safeSet(planning.getDocuments()::add, tenderProcess.getProcurementPlan()::getFormDoc,
                this::storeAsDocumentProcurementPlan
        );

        safeSetEach(planning.getDocuments()::add, tenderProcessProjectCabinetPapers(tenderProcess),
                this::storeAsDocumentProjectPlan
        );

        safeSetEach(planning.getMilestones()::add, ()->
                        tenderProcess.getPurchaseRequisition().stream().filter(Statusable::isExportable)
                                .flatMap(pr->pr.getPurchRequisitions().stream()).collect(Collectors.toList()),
                this::createPlanningMilestone);

        return planning;
    }

    public Supplier<Collection<CabinetPaper>> tenderProcessProjectCabinetPapers(TenderProcess tenderProcess) {
        return tenderProcess == null || tenderProcess.getProject() == null ? null : tenderProcess.getProject()
                ::getCabinetPapers;
    }

    public MakueniOrganization convertOrganization(
            org.devgateway.toolkit.persistence.dao.categories.Supplier supplier) {
        MakueniOrganization ocdsOrg = new MakueniOrganization();
        safeSet(ocdsOrg::setName, supplier::getLabel, WordUtils::capitalizeFully);
        safeSet(ocdsOrg::setIdentifier, () -> supplier, this::convertCategoryCodeToIdentifier,
                this::convertToOrgIdentifier);
        safeSet(ocdsOrg::setId, ocdsOrg::getIdentifier, Identifier::getId);
        safeSet(ocdsOrg.getAdditionalIdentifiers()::add, () -> supplier, this::convertCategoryToIdentifier,
                this::convertToLocalIdentifier);
        safeSet(ocdsOrg::setAddress, () -> supplier, this::createSupplierAddress);
        safeSet(ocdsOrg::setTargetGroup, supplier::getTargetGroup, this::categoryLabel);
        return ocdsOrg;
    }

    public Address createSupplierAddress(org.devgateway.toolkit.persistence.dao.categories.Supplier supplier) {
        Address ocdsAddress = new Address();
        safeSet(ocdsAddress::setCountryName, this::getCountry);
        safeSet(ocdsAddress::setStreetAddress, supplier::getAddress);
        return ocdsAddress;
    }


    public Identifier convertCategoryToIdentifier(Category category) {
        Identifier identifier = new Identifier();
        safeSet(identifier::setId, category::getId, this::longIdToString);
        safeSet(identifier::setLegalName, category::getLabel);
        return identifier;
    }

    public Identifier convertCategoryCodeToIdentifier(Category category) {
        if (ObjectUtils.isEmpty(category.getCode())) {
            return null;
        }
        Identifier identifier = new Identifier();
        safeSet(identifier::setId, category::getCode);
        safeSet(identifier::setLegalName, category::getLabel);
        return identifier;
    }

    public Identifier convertToOrgIdentifier(Identifier identifier) {
        safeSet(identifier::setScheme, () -> X_KE_INTERNAL_SCHEMA);
        safeSet(identifier::setUri, () -> serverURL + "/api/ocds/organization/all?scheme=" + X_KE_INTERNAL_SCHEMA,
                URI::create);
        safeSet(identifier::setId, () -> identifier, this::convertIdentifierToId);
        return identifier;
    }

    public Identifier convertToLocalIdentifier(Identifier identifier) {
        safeSet(identifier::setScheme, () -> X_KE_INTERNAL_SCHEMA);
        safeSet(identifier::setUri, () -> serverURL + "/api/ocds/organization/all?scheme=" + X_KE_INTERNAL_SCHEMA,
                URI::create);
        safeSet(identifier::setId, () -> identifier, this::convertIdentifierToId);
        return identifier;
    }

    public Milestone createPlanningMilestone(PurchRequisition pr) {
        Milestone milestone = new Milestone();
        safeSet(milestone::setType, () -> Milestone.MilestoneType.PRE_PROCUREMENT, Milestone.MilestoneType::toValue);
        safeSet(milestone::setCode, () -> "approvedDate");
        safeSet(milestone::setId, pr::getId, this::longIdToString);
        safeSet(milestone::setDateMet, pr::getApprovedDate);
        safeSet(milestone::setStatus, () -> pr, this::createPlanningMilestoneStatus);
        return milestone;
    }

    public boolean areReleasesIdentical(Release newRelease, Release oldRelease) {
        if (oldRelease == null) {
            return false;
        }
        Date newDate = newRelease.getDate();
        String newId = newRelease.getId();
        Date oldDate = oldRelease.getDate();
        String oldId = oldRelease.getId();
        newRelease.setDate(null);
        oldRelease.setDate(null);
        newRelease.setId(null);
        oldRelease.setId(null);
        try {
            String newReleaseJson = objectMapper.writeValueAsString(newRelease);
            String oldReleaseJson = objectMapper.writeValueAsString(oldRelease);
            newRelease.setDate(newDate);
            newRelease.setId(newId);
            oldRelease.setDate(oldDate);
            oldRelease.setId(oldId);
            return newReleaseJson.equals(oldReleaseJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Release createAndPersistRelease(TenderProcess tenderProcess) {
        try {
            //never export releases from draft procurement plans or projects
            if (!tenderProcess.getProcurementPlan().getStatus().equals(DBConstants.Status.APPROVED)
                    || (tenderProcess.getProject() != null && !tenderProcess.getProject().getStatus()
                    .equals(DBConstants.Status.APPROVED))) {
                return null;
            }
            Release release = createRelease(tenderProcess);
            Release byOcid = releaseRepository.findByOcid(release.getOcid());
            if (!areReleasesIdentical(release, byOcid)) {
                if (byOcid != null) {
                    releaseRepository.delete(byOcid);
                }
                Release save = releaseRepository.save(release);
                logger.info("Saved " + save.getOcid());
                OcdsValidatorNodeRequest nodeRequest = new OcdsValidatorNodeRequest();
                nodeRequest.setSchemaType(OcdsValidatorConstants.Schemas.RELEASE);
                nodeRequest.setExtensions(new TreeSet<>(OcdsController.EXTENSIONS));
                nodeRequest.setVersion(OcdsValidatorConstants.Versions.OCDS_1_1_3);
                nodeRequest.setNode(objectMapper.valueToTree(save));

                ProcessingReport validate = ocdsValidatorService.validate(nodeRequest);
                if (!validate.isSuccess() && validationErrors != null) {
                    validationErrors.append("TenderProcess with id ").append(tenderProcess.getId()).append(" ")
                            .append(validate.toString());
                    logger.warn(validate.toString());
                }
                return save;
            } else {
                logger.info("Will not resave unchanged release " + release.getOcid());
                return null;
            }
        } catch (Exception e) {
            logger.info("Exception processing tender process with id " + tenderProcess.getId());
            throw e;
        }
    }

    @Override
    public void convertToOcdsAndSaveAllApprovedPurchaseRequisitions() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //releaseRepository.deleteAll();
        //organizationRepository.deleteAll();
        validationErrors = new StringBuffer();
        tenderProcessService.findAllStream().forEach(this::createAndPersistRelease);
        sendValidationFailureAlert(validationErrors.toString());
        stopWatch.stop();
        logger.info("OCDS export finished in: " + stopWatch.getTime() + "ms");
    }

    public Milestone.Status createPlanningMilestoneStatus(PurchRequisition pr) {
        return Milestone.Status.MET;
    }

    public <C, S, R extends Supplier<S>> Supplier<S> getSupplier(Supplier<C> parentSupplier,
                                                                 Function<C, R> childSupplier) {
        C c = parentSupplier.get();

        if (!ObjectUtils.isEmpty(c)) {
            return childSupplier.apply(c);
        }
        return null;
    }

    /**
     * Will set value only if not empty, but first it converts it using converter
     *
     * @param consumer
     * @param supplier
     * @param converter
     * @param <S>
     * @param <C>
     */
    public <S, C> void safeSet(Consumer<C> consumer, Supplier<S> supplier, Function<S, C> converter) {
        if (supplier == null || consumer == null || converter == null) {
            return;
        }

        Optional<C> c = safeConvert(supplier, converter);

        c.ifPresent(consumer);
    }

    public <S, C> Optional<C> safeConvert(Supplier<S> supplier, Function<S, C> converter) {
        if (supplier == null || converter == null) {
            return Optional.empty();
        }
        S o = supplier.get();
        if (o instanceof Statusable && !((Statusable) o).isExportable()) {
            return Optional.empty();
        }
        if (!ObjectUtils.isEmpty(o)) {
            C converted = converter.apply(o);
            if (!ObjectUtils.isEmpty(converted)) {
                return Optional.ofNullable(converted);
            }
        }
        return Optional.empty();
    }

    public <S, C, X> void safeSet(Consumer<C> consumer, Supplier<S> supplier,
                                  Function<S, X> converter1, Function<X, C> converter2) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null) {
            return;
        }
        Optional<X> x = safeConvert(supplier, converter1);
        safeConvert(() -> x.orElse(null), converter2).ifPresent(consumer);
    }

    public <S, C, X, Y> void safeSet(Consumer<C> consumer, Supplier<S> supplier,
                                     Function<S, X> converter1, Function<X, Y> converter2, Function<Y, C> converter3) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null || converter3 == null) {
            return;
        }
        Optional<X> x = safeConvert(supplier, converter1);
        Optional<Y> y = safeConvert(() -> x.orElse(null), converter2);
        safeConvert(() -> y.orElse(null), converter3).ifPresent(consumer);
    }

    public <S, C, X> void safeSetEach(Consumer<C> consumer, Supplier<Collection<S>> supplier,
                                      Function<S, X> converter1,
                                      Function<X, C> converter2) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter1).filter(Objects::nonNull).
                    map(converter2).filter(Objects::nonNull).
                    forEach(consumer);
        }
    }

    public <S, C, X, Y> void safeSetEach(Consumer<C> consumer,
                                         Supplier<Collection<S>> supplier,
                                         Function<S, X> converter1, Function<X, Y> converter2,
                                         Function<Y, C> converter3) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null || converter3 == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter1).filter(Objects::nonNull).
                    map(converter2).filter(Objects::nonNull).
                    map(converter3).filter(Objects::nonNull).
                    forEach(consumer);
        }
    }


    public <S, C> void safeSetEach(Consumer<C> consumer, Supplier<Collection<S>> supplier, Function<S, C> converter) {
        if (supplier == null || consumer == null || converter == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter).filter(Objects::nonNull).forEach(consumer);
        }
    }

    public <S> void safeSetEach(Consumer<S> consumer, Supplier<Collection<S>> supplier) {
        safeSetEach(consumer, supplier, Function.identity());
    }


    /**
     * same as #safeSet(Consumer, Supplier, Function), but with {@link Function#identity()} as converter
     *
     * @param consumer
     * @param supplier
     * @param <S>
     */
    public <S> void safeSet(Consumer<S> consumer, Supplier<S> supplier) {
        safeSet(consumer, supplier, Function.identity());
    }

    /**
     * Converter for ids into string ids. We do not use Long::toString because method signature is not unique at
     * compile
     * time
     *
     * @param id
     * @return
     */
    public String longIdToString(Long id) {
        return id.toString();
    }

    public Unit createPlanningItemUnit(PurchaseItem purchaseItem) {
        Unit unit = new Unit();
        safeSet(unit::setScheme, () -> MongoConstants.OCDSSchemes.UNCEFACT);
        safeSet(unit::setName, purchaseItem::getPlanItem, PlanItem::getUnitOfIssue, this::categoryLabel);
        safeSet(unit::setId, purchaseItem::getPlanItem, PlanItem::getUnitOfIssue, Category::getCode);
        safeSet(unit::setValue, purchaseItem::getAmount, this::convertAmount);
        return unit;
    }


    public Amount convertAmount(BigDecimal sourceAmount) {
        Amount amount = new Amount();
        safeSet(amount::setAmount, () -> sourceAmount);
        safeSet(amount::setCurrency, this::getCurrency);
        return amount;
    }

    public MakueniItem createPlanningItem(PurchaseItem purchaseItem) {
        MakueniItem ocdsItem = new MakueniItem();
        safeSet(ocdsItem::setId, purchaseItem::getId, this::longIdToString);
        safeSet(ocdsItem::setDescription, purchaseItem::getLabel);
        safeSet(ocdsItem::setUnit, () -> purchaseItem, this::createPlanningItemUnit);
        safeSet(ocdsItem::setQuantity, purchaseItem::getQuantity, BigDecimal::doubleValue);
        safeSet(ocdsItem::setClassification, purchaseItem::getPlanItem, this::createPlanItemClassification);
        safeSet(ocdsItem::setTargetGroup, purchaseItem::getPlanItem, PlanItem::getTargetGroup,
                this::categoryLabel
        );
        safeSet(ocdsItem::setTargetGroupValue, purchaseItem::getPlanItem, PlanItem::getTargetGroupValue,
                this::convertAmount
        );
        return ocdsItem;
    }


    public Amount.Currency getCurrency() {
        return Amount.Currency.KES;
    }


    public Bids createBids(TenderQuotationEvaluation quotationEvaluation) {
        Bids bids = new Bids();
        safeSetEach(bids.getDetails()::add, quotationEvaluation::getBids, this::createBidsDetail);
        return bids;

    }

    public Organization addRole(Organization o, Organization.OrganizationType role) {
        safeSet(o.getRoles()::add, () -> role,
                Organization.OrganizationType::toValue
        );
        return o;
    }

    public Organization addTendererRole(Organization o) {
        return addRole(o, Organization.OrganizationType.tenderer);
    }

    public Organization addSupplierRole(Organization o) {
        return addRole(o, Organization.OrganizationType.supplier);
    }

    public Organization addPayeeRole(Organization o) {
        return addRole(o, Organization.OrganizationType.payee);
    }

    public Organization addPayerRole(Organization o) {
        return addRole(o, Organization.OrganizationType.payer);
    }


    public Set<OrganizationReference> createTenderersFromBids(Bids bids) {
        return bids.getDetails().stream().flatMap(b -> b.getTenderers().stream()).collect(Collectors.toSet());
    }


    public Detail createBidsDetail(Bid bid) {
        Detail detail = new Detail();
        safeSet(detail::setId, bid::getId, this::longIdToString);
        safeSet(detail.getTenderers()::add, bid::getSupplier, this::convertOrganization, this::addTendererRole);
        safeSet(detail::setValue, bid::getQuotedAmount, this::convertAmount);
        safeSet(detail::setStatus, () -> bid, this::createBidStatus);
        return detail;
    }

    public String createBidStatus(Bid bid) {
        if (DBConstants.SupplierResponsiveness.PASS.equals(bid.getSupplierResponsiveness())) {
            return "valid";
        }

        return "disqualified";
    }

    public Award createAward(AwardNotificationItem item) {
        Award ocdsAward = new Award();
        safeSet(ocdsAward::setTitle, item.getParent()::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderTitle
        );
        safeSet(ocdsAward::setId, item::getId, this::longIdToString);

        safeSet(ocdsAward::setDate, () -> item, AwardNotificationItem::getAwardDate);
        safeSet(ocdsAward::setValue, () -> item, AwardNotificationItem::getAwardValue,
                this::convertAmount
        );

        safeSet(ocdsAward.getSuppliers()::add, () -> item,
                AwardNotificationItem::getAwardee, this::convertOrganization, this::addSupplierRole
        );

        safeSet(ocdsAward::setContractPeriod, () -> item,
                AwardNotificationItem::getAcknowledgementDays, this::convertDaysToPeriod
        );

        safeSetEach(ocdsAward.getDocuments()::add, item::getFormDocs, this::storeAsDocumentAwardNotice);
        safeSet(ocdsAward.getDocuments()::add,
                item.getParent().getTenderProcess().getSingleProfessionalOpinion()::getFormDoc,
                this::storeAsDocumentProfessionalOpinion
        );

        AwardAcceptanceItem acceptanceItem = item.getExportableAcceptanceItem();


        safeSetEach(ocdsAward.getDocuments()::add, () -> Optional.ofNullable(acceptanceItem)
                        .map(AwardAcceptanceItem::getFormDocs).orElse(null),
                this::storeAsDocumentAwardAcceptance
        );


        //this will overwrite the award value taken from award notification with a possible different award value
        //from award acceptance (if any)
        safeSet(ocdsAward::setValue,
                () -> Optional.ofNullable(acceptanceItem)
                        .map(AwardAcceptanceItem::getAcceptedAwardValue).orElse(null), this::convertAmount
        );

        safeSet(ocdsAward::setStatus, () -> item, this::createAwardStatus);
        return ocdsAward;
    }


    public Contract createContract(org.devgateway.toolkit.persistence.dao.form.Contract contract) {
        MakueniContract ocdsContract = new MakueniContract();
        safeSet(ocdsContract::setId, contract::getReferenceNumber);
        safeSet(ocdsContract::setTitle, contract::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderTitle
        );
        safeSet(ocdsContract::setDateSigned, contract::getContractDate);
        safeSet(ocdsContract::setPeriod, contract::getExpiryDate, this::convertContractEndDateToPeriod);
        safeSet(ocdsContract::setValue, contract::getContractValue, this::convertAmount);
        safeSetEach(ocdsContract.getDocuments()::add, contract::getContractDocs, this::storeAsDocumentContractNotice);
        safeSet(ocdsContract::setAwardID,
                contract.getTenderProcess()::getSingleAwardNotification, this::entityIdToString);
        safeSet(ocdsContract::setContractor, contract::getAwardee, this::convertOrganization);
        safeSet(ocdsContract::setStatus, () -> contract, this::createContractStatus);
        safeSet(ocdsContract::setImplementation, contract::getTenderProcess, this::createImplementation);

        return ocdsContract;
    }

    public Contract.Status createContractStatus(org.devgateway.toolkit.persistence.dao.form.Contract contract) {
        if (contract.isTerminatedWithImplementation()) {
            return Contract.Status.cancelled;
        }

        if (!ObjectUtils.isEmpty(contract.getExpiryDate()) && PersistenceUtil.convertDateToZonedDateTime(
                contract.getContractDate()).isBefore(
                PersistenceUtil.currentDate()) && PersistenceUtil.convertDateToZonedDateTime(contract.getExpiryDate())
                .isAfter(PersistenceUtil.currentDate())) {
            return Contract.Status.active;
        }

        if (PersistenceUtil.convertDateToZonedDateTime(contract.getContractDate()).isAfter(
                PersistenceUtil.currentDate())) {
            return Contract.Status.pending;
        }

        return Contract.Status.terminated;
    }


    public Award.Status createAwardStatus(AwardNotificationItem item) {

        if (item.getParent().getTenderProcess().isTerminated()) {
            return Award.Status.cancelled;
        }

        org.devgateway.toolkit.persistence.dao.form.Contract contract =
                Optional.ofNullable(item.getParent().getTenderProcess().getSingleContract())
                        .filter(Statusable::isExportable)
                        .orElse(null);
        if (contract == null) {
            return Award.Status.pending;
        }

        Optional<AwardNotificationItem> first = item.getParent().getItems().stream().sorted(
                Comparator.comparing(AwardNotificationItem::getAwardDate).reversed())
                .findFirst();
        if (first.get().equals(item)) {
            return Award.Status.active;
        } else {
            return Award.Status.unsuccessful;
        }
    }

    public Period convertDaysToPeriod(Integer days) {
        Period period = new Period();
        period.setDurationInDays(days);
        return period;
    }


    public Period convertContractEndDateToPeriod(Date endDate) {
        Period period = new Period();
        period.setEndDate(endDate);
        return period;
    }


    public static String getOcid(TenderProcess tenderProcess) {
        return OCID_PREFIX + tenderProcess.getId();
    }

    public String getReleaseId() {
        return DigestUtils.md5Hex(Instant.now().toString());
    }

    public List<Tag> createReleaseTag(Release release) {
        List<Tag> tags = new ArrayList<>();

        if (!ObjectUtils.isEmpty(release.getPlanning())) {
            tags.add(Tag.planning);
        }

        if (!ObjectUtils.isEmpty(release.getTender())) {
            tags.add(Tag.tender);
        }

        if (!ObjectUtils.isEmpty(release.getContracts())) {
            tags.add(Tag.contract);
        }

        if (!ObjectUtils.isEmpty(release.getAwards())) {
            tags.add(Tag.award);
        }
        return tags;
    }

    public int getTenderersFromTender(Tender tender) {
        if (tender.getTenderers() != null) {
            return tender.getTenderers().size();
        }
        return 0;
    }

    @Override
    public Release createRelease(TenderProcess tenderProcess) {
        Release release = new Release();
        safeSet(release::setId, this::getReleaseId);
        safeSet(release::setOcid, () -> tenderProcess, MakueniToOCDSConversionServiceImpl::getOcid);
        safeSet(release::setPlanning, () -> tenderProcess, this::createPlanning);
        safeSet(release::setBids, tenderProcess::getSingleTenderQuotationEvaluation, this::createBids);
        safeSet(release::setTender, tenderProcess::getSingleTender, this::createTender);
        safeSet(release::setBuyer, tenderProcess::getProcurementPlan, ProcurementPlan::getDepartment,
                this::convertBuyer);


        if (release.getTender() != null) {
            safeSet(release.getTender()::setTenderers, release::getBids, this::createTenderersFromBids);
            safeSet(release.getTender()::setNumberOfTenderers, release::getTender, this::getTenderersFromTender);
            safeSet(release.getTender()::setMainProcurementCategory, tenderProcess::getSingleContract,
                    this::createMainProcurementCategory);
            safeSet(release.getTender().getAdditionalProcurementCategories()::addAll,
                    tenderProcess::getSingleContract, this::createAdditionalProcurementCategories);
            addTenderersToOrganizationCollection(release.getTender().getTenderers());
        }

        safeSetEach(release.getAwards()::add, () -> Optional.ofNullable(tenderProcess.getSingleAwardNotification())
                .filter(Statusable::isExportable).map(Stream::of).orElseGet(Stream::empty)
                .flatMap(a -> a.getItems().stream()).collect(Collectors.toSet()), this::createAward);
        safeSet(release.getContracts()::add, tenderProcess::getSingleContract, this::createContract);
        safeSet(release::setDate, Instant::now, Date::from);
        safeSet(release.getParties()::addAll, () -> release, this::createParties);
        safeSet(release.getTag()::addAll, () -> release, this::createReleaseTag);
        safeSet(release::setInitiationType, () -> Release.InitiationType.tender);

        addPartiesToOrganizationCollection(release.getParties());

        return release;
    }

    private Tender.MainProcurementCategory createMainProcurementCategory(
            org.devgateway.toolkit.persistence.dao.form.Contract contract) {
        if (ObjectUtils.isEmpty(contract.getContractDocs())) {
            return null;
        }
        return contract.getContractDocs().stream().map(ContractDocument::getContractDocumentType)
                .map(ContractDocumentType::getLabel).map(String::toLowerCase).
                        collect(Collectors.groupingBy(e -> e, Collectors.counting())).
                        entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey).findFirst().map(Tender.MainProcurementCategory::fromValue)
                .orElse(null);
    }

    private List<String> createAdditionalProcurementCategories(
            org.devgateway.toolkit.persistence.dao.form.Contract contract) {
        if (ObjectUtils.isEmpty(contract.getContractDocs())) {
            return null;
        }
        List<String> collect = contract.getContractDocs().stream().map(ContractDocument::getContractDocumentType)
                .map(ContractDocumentType::getLabel).map(String::toLowerCase).distinct().
                        map(Tender.MainProcurementCategory::fromValue)
                .map(Tender.MainProcurementCategory::toString).collect(Collectors.toList());
        if (collect.size() == 1) {
            return null;
        } else {
            return collect;
        }
    }


    private Implementation createImplementation(TenderProcess tenderProcess) {
        Implementation impl = new Implementation();
        safeSetEach(impl.getMilestones()::add, tenderProcess::getPmcReports, this::createAuthImplMilestone);
        safeSetEach(impl.getMilestones()::add, tenderProcess::getInspectionReports, this::createAuthImplMilestone);
        safeSetEach(impl.getMilestones()::add, tenderProcess::getAdministratorReports, this::createAuthImplMilestone);
        safeSetEach(impl.getMilestones()::add, tenderProcess::getMeReports, this::createMEMilestone);
        safeSetEach(impl.getTransactions()::add, tenderProcess::getPaymentVouchers,
                this::createPaymentVoucherTransaction);

        safeSetEach(impl.getDocuments()::add, () -> convertImplToFileMetadata(tenderProcess.getPmcReports()),
                this::storeAsDocumentPhProgressReport
        );
        safeSetEach(impl.getDocuments()::add, () -> convertImplToFileMetadata(tenderProcess.getInspectionReports()),
                this::storeAsDocumentPhProgressReport
        );
        safeSetEach(impl.getDocuments()::add, () -> convertImplToFileMetadata(tenderProcess.getAdministratorReports()),
                this::storeAsDocumentPhProgressReport
        );
        safeSetEach(impl.getDocuments()::add, () -> convertImplToFileMetadata(tenderProcess.getPaymentVouchers()),
                this::storeAsDocumentFinProgressReport
        );
        safeSetEach(impl.getDocuments()::add, () -> convertImplToFileMetadata(tenderProcess.getMeReports()),
                this::storeAsDocumentEvaluationReports
        );
        return impl;
    }

    private <S extends AbstractImplTenderProcessMakueniEntity>
    Collection<FileMetadata> convertImplToFileMetadata(Collection<S> c) {
        return c.stream()
                .flatMap(r -> r.getFormDocs().stream()).collect(Collectors.toList());
    }


    private void addPartiesToOrganizationCollection(Set<Organization> parties) {
        parties.forEach(p -> {
            Optional<Organization> organization = organizationRepository.findById(p.getId());
            if (organization.isPresent()) {
                if (!organization.get().getRoles().containsAll(p.getRoles())) {
                    organization.get().getRoles().addAll(p.getRoles());
                    organizationRepository.save(organization.get());
                }
            } else {
                organizationRepository.save(p);
            }

        });
    }

    private void addTenderersToOrganizationCollection(Set<OrganizationReference> tenderers) {
        tenderers.forEach(p -> {
            Optional<Organization> organization = organizationRepository.findById(p.getId());
            if (!organization.isPresent()) {
                organizationRepository.save((Organization) p);
            }
        });
    }

}
