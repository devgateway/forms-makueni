package org.devgateway.toolkit.persistence.service.form;


import com.google.common.collect.ImmutableMap;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AdministratorReport;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.InspectionReport;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.dao.form.PaymentVoucher;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Transactional
public class MakeniEntityServiceResolverImpl implements MakueniEntityServiceResolver {

    @Autowired
    private AwardAcceptanceService awardAcceptanceService;

    @Autowired
    private AwardNotificationService awardNotificationService;

    @Autowired
    private CabinetPaperService cabinetPaperService;

    @Autowired
    private PurchaseRequisitionGroupService purchaseRequisitionGroupService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ProcurementPlanService procurementPlanService;

    @Autowired
    private ProfessionalOpinionService professionalOpinionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private TenderQuotationEvaluationService tenderQuotationEvaluationService;

    @Autowired
    private TenderService tenderService;

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

    private Map<Class<? extends AbstractMakueniEntity>, AbstractMakueniEntityService<? extends AbstractMakueniEntity>>
            serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = ImmutableMap.<Class<? extends AbstractMakueniEntity>,
                AbstractMakueniEntityService<? extends AbstractMakueniEntity>>builder()
                .put(Tender.class, tenderService)
                .put(TenderQuotationEvaluation.class, tenderQuotationEvaluationService)
                .put(PurchaseRequisitionGroup.class, purchaseRequisitionGroupService)
                .put(Project.class, projectService)
                .put(ProfessionalOpinion.class, professionalOpinionService)
                .put(ProcurementPlan.class, procurementPlanService)
                .put(Contract.class, contractService)
                .put(CabinetPaper.class, cabinetPaperService)
                .put(AwardNotification.class, awardNotificationService)
                .put(AwardAcceptance.class, awardAcceptanceService)
                .put(AdministratorReport.class, administratorReportService)
                .put(InspectionReport.class, inspectionReportService)
                .put(PMCReport.class, pmcReportService)
                .put(MEReport.class, meReportService)
                .put(PaymentVoucher.class, paymentVoucherService)
                .build();
    }

    public <S extends AbstractMakueniEntity> S saveAndFlushMakueniEntity(S entity) {
        AbstractMakueniEntityService<S> service = (AbstractMakueniEntityService<S>) serviceMap.get(entity.getClass());
        return service.saveAndFlush(entity);
    }
}
