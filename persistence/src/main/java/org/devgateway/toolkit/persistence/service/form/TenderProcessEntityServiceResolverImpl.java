package org.devgateway.toolkit.persistence.service.form;

import com.google.common.collect.ImmutableMap;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AdministratorReport;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.InspectionReport;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.dao.form.PaymentVoucher;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author Octavian Ciubotaru
 */
@Service
@Transactional
public class TenderProcessEntityServiceResolverImpl implements TenderProcessEntityServiceResolver {

    private Map<Class<?>, AbstractTenderProcessEntityService<?>> serviceMap;

    @Autowired
    private AwardAcceptanceService awardAcceptanceService;

    @Autowired
    private AwardNotificationService awardNotificationService;

    @Autowired
    private PurchaseRequisitionGroupService purchaseRequisitionGroupService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ProfessionalOpinionService professionalOpinionService;

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

    @PostConstruct
    public void init() {
        serviceMap = ImmutableMap.<Class<?>, AbstractTenderProcessEntityService<?>>builder()
                .put(Tender.class, tenderService)
                .put(TenderQuotationEvaluation.class, tenderQuotationEvaluationService)
                .put(PurchaseRequisitionGroup.class, purchaseRequisitionGroupService)
                .put(ProfessionalOpinion.class, professionalOpinionService)
                .put(Contract.class, contractService)
                .put(AwardNotification.class, awardNotificationService)
                .put(AwardAcceptance.class, awardAcceptanceService)
                .put(AdministratorReport.class, administratorReportService)
                .put(InspectionReport.class, inspectionReportService)
                .put(PMCReport.class, pmcReportService)
                .put(MEReport.class, meReportService)
                .put(PaymentVoucher.class, paymentVoucherService)
                .build();
    }

    @Override
    public <T extends AbstractTenderProcessMakueniEntity> long countByTenderProcess(T entity) {
        return getService(entity).countByTenderProcess(entity.getId(), entity.getTenderProcess());
    }

    @Override
    public <T extends AbstractTenderProcessMakueniEntity> T saveAndFlush(T entity) {
        return (T) getService(entity).saveAndFlush(entity);
    }

    private <T extends AbstractTenderProcessMakueniEntity> AbstractTenderProcessEntityService getService(T entity) {
        Class<? extends AbstractTenderProcessMakueniEntity> entityClass = entity.getClass();
        AbstractTenderProcessEntityService<?> service = serviceMap.get(entityClass);
        if (service == null) {
            throw new RuntimeException("No service configured for " + entityClass);
        }
        return service;
    }
}
