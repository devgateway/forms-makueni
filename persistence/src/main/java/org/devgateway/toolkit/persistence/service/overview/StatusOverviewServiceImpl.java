package org.devgateway.toolkit.persistence.service.overview;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractImplTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.PaymentVoucher;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan_;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.ProjectAttachable;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.Statusable;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess_;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.dao.form.Tender_;
import org.devgateway.toolkit.persistence.dto.StatusOverviewRowGroup;
import org.devgateway.toolkit.persistence.dto.StatusOverviewRowInfo;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.persistence.service.filterstate.form.ProjectFilterState;
import org.devgateway.toolkit.persistence.service.form.AbstractImplTenderProcessMakueniEntityService;
import org.devgateway.toolkit.persistence.service.form.AbstractMakueniEntityService;
import org.devgateway.toolkit.persistence.service.form.AdministratorReportService;
import org.devgateway.toolkit.persistence.service.form.AwardAcceptanceService;
import org.devgateway.toolkit.persistence.service.form.AwardNotificationService;
import org.devgateway.toolkit.persistence.service.form.ContractService;
import org.devgateway.toolkit.persistence.service.form.InspectionReportService;
import org.devgateway.toolkit.persistence.service.form.MEReportService;
import org.devgateway.toolkit.persistence.service.form.PMCReportService;
import org.devgateway.toolkit.persistence.service.form.PaymentVoucherService;
import org.devgateway.toolkit.persistence.service.form.ProfessionalOpinionService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.persistence.service.form.PurchaseRequisitionGroupService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessServiceImpl;
import org.devgateway.toolkit.persistence.service.form.TenderQuotationEvaluationService;
import org.devgateway.toolkit.persistence.service.form.TenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.devgateway.toolkit.persistence.dao.DBConstants.Status.APPROVED;
import static org.devgateway.toolkit.persistence.dao.DBConstants.Status.SUBMITTED;

/**
 * @author gmutuhu
 */
@Service
@Transactional(readOnly = true)
public class StatusOverviewServiceImpl implements StatusOverviewService {
    protected static final Logger logger = LoggerFactory.getLogger(StatusOverviewService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DgFmService fmService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private PurchaseRequisitionGroupService purchaseRequisitionGroupService;

    @Autowired
    private TenderQuotationEvaluationService tenderQuotationEvaluationService;

    @Autowired
    private ProfessionalOpinionService professionalOpinionService;

    @Autowired
    private AwardNotificationService awardNotificationService;

    @Autowired
    private AwardAcceptanceService awardAcceptanceService;

    @Autowired
    private AdministratorReportService administratorReportService;

    @Autowired
    private InspectionReportService inspectionReportService;

    @Autowired
    private PMCReportService pmcReportService;

    @Autowired
    private MEReportService meReportService;

    @Autowired
    private PaymentVoucherService paymentVoucherService;

    @Autowired
    private ContractService contractService;

    @Override
    public List<StatusOverviewRowGroup> getDisplayableTenderProcesses(FiscalYear fiscalYear, String title) {
        List<StatusOverviewRowGroup> groupList = new ArrayList<>();
        Map<ProcurementPlan, List<TenderProcess>> pptenderProcesses = tenderProcessService.findAll(
                getTenderProcessViewSpecification(null, fiscalYear, title)
        ).stream().collect(Collectors.groupingBy(TenderProcess::getProcurementPlan));

        pptenderProcesses.forEach((procurementPlan, tenderProcesses) -> {
            StatusOverviewRowGroup group = new StatusOverviewRowGroup();
            groupList.add(group);
            group.setProcurementPlan(procurementPlan);
            tenderProcesses.forEach(tp -> {
                StatusOverviewRowInfo rowInfo = new StatusOverviewRowInfo();
                group.getRows().add(rowInfo);
                rowInfo.setId(tp.getId());
                rowInfo.setTitle(tp.getSingleTender() == null ? tp.getClass().getSimpleName() + " " + tp.getId()
                        : tp.getSingleTender().getTitle());
                rowInfo.setTenderProcessStatus(
                        getProcessStatus(getMakueniEntitiesStatuses(
                                tp.getPurchaseRequisition(),
                                tp.getTender(),
                                tp.getTenderQuotationEvaluation(), tp.getProfessionalOpinion()),
                                getStatusSizeWithFm(PurchaseRequisitionGroup.class, Tender.class,
                                        TenderQuotationEvaluation.class, ProfessionalOpinion.class)));
                rowInfo.setAwardProcessStatus(
                        getProcessStatus(getMakueniEntitiesStatuses(tp.getAwardNotification(), tp.getAwardAcceptance(),
                                tp.getContract()),
                                getStatusSizeWithFm(AwardNotification.class, AwardAcceptance.class, Contract.class)));
                rowInfo.setImplementationStatus(
                        getProcessStatus(getMakueniEntitiesStatuses(tp.getAdministratorReports(),
                                tp.getInspectionReports(), tp.getPmcReports(), tp.getMeReports(),
                                tp.getPaymentVouchers()),
                                0));
            });

        });

        return groupList;
    }

    public int getStatusSizeWithFm(Class<? extends AbstractMakueniEntity>... classes) {
        return (int) Arrays.stream(classes).map(TenderProcessServiceImpl.FORM_FM_MAP::get)
                .map(fmService::isFeatureVisible)
                .filter(BooleanUtils::isTrue).count();
    }


    private <S extends AbstractMakueniEntity> List<String> getMakueniEntitiesStatuses(
            Collection<? extends S>... cols) {
        ArrayList<String> ret = new ArrayList<>();
        for (Collection<? extends S> col : cols) {
            col.forEach(e -> ret.add(getMakueniEntityVirtualStatus(e)));
        }
        return ret;
    }

    private String getMakueniEntityVirtualStatus(AbstractMakueniEntity e) {
        if (e instanceof PaymentVoucher) {
            return getPaymentVoucherVirtualStatus((PaymentVoucher) e);
        }
        return e.getStatus();
    }

    private String getPaymentVoucherVirtualStatus(PaymentVoucher pv) {
        if (pv.getStatus().equals(APPROVED) && !pv.getLastPayment()) {
            return SUBMITTED;
        }
        return pv.getStatus();
    }


    @Override
    @Transactional(readOnly = true)
    public List<StatusOverviewRowGroup> getAllProjects(final FiscalYear fiscalYear, final String projectTitle) {
        final List<Project> projects = projectService.findAll(
                new ProjectFilterState(fiscalYear, projectTitle).getSpecification());

        final Map<Project, List<String>> tenderStatusMap = addStatus(
                fiscalYear, purchaseRequisitionGroupService, tenderService,
                tenderQuotationEvaluationService, professionalOpinionService);
        final Map<Project, List<String>> awardStatusMap = addStatus(
                fiscalYear, awardNotificationService, awardAcceptanceService, contractService);

        final Map<Project, List<String>> implementationStatusMap = addImplementationStatus(
                fiscalYear, administratorReportService, inspectionReportService, pmcReportService, meReportService,
                paymentVoucherService);

        // get list of statuses of PurchaseRequisition forms grouped by Project
        final Map<Project, List<String>> purchaseStatusMap = groupStatusByProject(
                purchaseRequisitionGroupService.findByFiscalYear(fiscalYear));

        final List<StatusOverviewRowGroup> statusOverviewData = new ArrayList<>();
        for (final Project project : projects) {
            StatusOverviewRowGroup sod = statusOverviewData.stream()
                    .filter(item -> project.getProcurementPlan().getId().equals(item.getProcurementPlan().getId()))
                    .findFirst()
                    .orElse(null);
            if (sod == null) {
                sod = new StatusOverviewRowGroup();
                sod.setProcurementPlan(project.getProcurementPlan());
                statusOverviewData.add(sod);
            }

            final List<String> purchaseStatuses = purchaseStatusMap.get(project);

            final StatusOverviewRowInfo statusOverviewRowInfo = new StatusOverviewRowInfo();
            statusOverviewRowInfo.setId(project.getId());
            statusOverviewRowInfo.setTitle(project.getProjectTitle());
            statusOverviewRowInfo.setProjectStatus(project.getStatus());

            // check if we have at least one Purchase Requisition
            if (purchaseStatuses == null || purchaseStatuses.isEmpty()) {
                statusOverviewRowInfo.setTenderProcessStatus(DBConstants.Status.NOT_STARTED);
                statusOverviewRowInfo.setAwardProcessStatus(DBConstants.Status.NOT_STARTED);
                statusOverviewRowInfo.setImplementationStatus(DBConstants.Status.NOT_STARTED);
            } else {
                statusOverviewRowInfo.setTenderProcessStatus(
                        getProcessStatus(tenderStatusMap.get(project), purchaseStatuses.size() * 4));
                statusOverviewRowInfo.setAwardProcessStatus(
                        getProcessStatus(awardStatusMap.get(project), purchaseStatuses.size() * 3));
                statusOverviewRowInfo.setImplementationStatus(
                        getProcessStatus(implementationStatusMap.get(project), 0));
            }

            sod.getRows().add(statusOverviewRowInfo);
        }

        return statusOverviewData;
    }

    @Override
    public Long countProjects(FiscalYear fiscalYear, String projectTitle) {
        return projectService.count(new ProjectFilterState(fiscalYear, projectTitle).getSpecification());
    }

    @Override
    public Long countTenderProcesses(FiscalYear fiscalYear, String title) {
        return tenderProcessService.count();
    }

    public Specification<TenderProcess> getTenderProcessViewSpecification(Department department,
                                                                          FiscalYear fiscalYear, String title) {
            return (r, cq, cb) -> cb.and(
                    department == null ? cb.and()
                            : cb.equal(r.join(TenderProcess_.procurementPlan)
                                    .get(ProcurementPlan_.department), department),
                    cb.equal(r.join(TenderProcess_.procurementPlan).get(ProcurementPlan_.fiscalYear), fiscalYear),
                    cb.isNull(r.get(TenderProcess_.project)),
                    ObjectUtils.isEmpty(title) ? cb.and() : cb.like(
                            cb.lower(r.join(TenderProcess_.tender).get(Tender_.tenderTitle)),
                            "%" + title.toLowerCase() + "%"));
    }

    /**
     * Returns an aggregate status from a list of statuses.
     *
     * @param sizeCheck - is used to determine if the list of statuses has the right length
     */
    private String getProcessStatus(final List<String> statuses, final int sizeCheck) {
        if (statuses != null) {
            if (statuses.contains(DBConstants.Status.TERMINATED)) {
                return DBConstants.Status.TERMINATED;
            }

            if (statuses.size() == 0) {
                return DBConstants.Status.NOT_STARTED;
            }

            if (statuses.contains(DBConstants.Status.DRAFT) || (sizeCheck != 0 && statuses.size() != sizeCheck)) {
                return DBConstants.Status.DRAFT;
            }

            if (statuses.contains(DBConstants.Status.SUBMITTED)) {
                return DBConstants.Status.SUBMITTED;
            }

            if (statuses.contains(APPROVED)) {
                return APPROVED;
            }
        }

        return DBConstants.Status.NOT_STARTED;
    }

    /**
     * Group a list of {@link ProjectAttachable} objects by {@link Project} and collect all their status.
     */
    @Transactional(readOnly = true)
    public <S extends ProjectAttachable & Statusable>
    Map<Project, List<String>> groupStatusByProject(final Collection<S> list) {
        return list.stream().filter(pa -> !ObjectUtils.isEmpty(pa.getProject()))
                .collect(Collectors.groupingBy(ProjectAttachable::getProject,
                        Collectors.mapping(key -> key.getStatus(), Collectors.toList())));
    }

    @Transactional(readOnly = true)
    public Map<Project, List<String>> groupStatusByProjectPaymentVoucher(Map<Project, List<String>> statusMap,
                                                                          final List<PaymentVoucher> list) {
        Map<Project, List<String>> paymentStatus = list.parallelStream().filter(p -> !p.getStatus().equals(SUBMITTED)
                && !ObjectUtils.isEmpty(p.getProject()))
                .collect(Collectors.groupingBy(ProjectAttachable::getProject,
                        Collectors.mapping(key -> key.getStatus().equals(APPROVED)
                                        ? (key.getLastPayment() ? APPROVED : SUBMITTED) : key.getStatus(),
                                Collectors.toList())));

        //if there are projects with no payment vouchers, consider the project at most submitted, never approved
        SetUtils.SetView<Project> difference = SetUtils.difference(statusMap.keySet(), paymentStatus.keySet());
        difference.forEach(i -> paymentStatus.put(i, Collections.singletonList(SUBMITTED)));

        return paymentStatus;
    }

    /**
     * Merge 2 {@link Map} of statuses grouped by {@link S}.
     */
    @Transactional(readOnly = true)
    public <S> Map<S, List<String>> mergeMapOfStatuses(final Map<S, List<String>> map1,
                                                        final Map<S, List<String>> map2) {
        final Map<S, List<String>> mergedMap = new HashMap<>(map1);
        map2.forEach(
                (key, value) -> mergedMap.merge(key, value,
                        (v1, v2) -> Stream.of(v1, v2).flatMap(Collection::stream).collect(Collectors.toList())));

        return mergedMap;
    }

    @Transactional(readOnly = true)
    public  <S extends AbstractMakueniEntity & ProjectAttachable & Statusable>
    Map<Project, List<String>> addStatus(final FiscalYear fiscalYear,
                                         final AbstractMakueniEntityService<? extends S>... services) {
        Map<Project, List<String>> statusMap = new HashMap<>();

        for (AbstractMakueniEntityService<? extends S> service : services) {
            final List<? extends S> list = service.findByFiscalYear(fiscalYear);
            statusMap = mergeMapOfStatuses(statusMap, groupStatusByProject(list));
        }

        return statusMap;
    }


    @Transactional(readOnly = true)
    public  <S extends AbstractImplTenderProcessMakueniEntity & ProjectAttachable & Statusable>
    Map<Project, List<String>> addImplementationStatus(final FiscalYear fiscalYear,
                                                       final AbstractImplTenderProcessMakueniEntityService
                                                               <? extends S>... services) {
        Map<Project, List<String>> statusMap = new HashMap<>();

        for (AbstractMakueniEntityService<? extends S> service : services) {
            final List<? extends S> list = service.findByFiscalYear(fiscalYear);
            statusMap = mergeMapOfStatuses(statusMap,
                    service instanceof PaymentVoucherService
                            ? groupStatusByProjectPaymentVoucher(statusMap, (List<PaymentVoucher>) list)
                            : groupStatusByProject(list));
        }

        return statusMap;
    }
}
