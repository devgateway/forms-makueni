package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.BidPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.service.form.TenderQuotationEvaluationService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author gmutuhu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditTenderQuotationEvaluationPage extends EditAbstractTenderProcessMakueniEntityPage
        <TenderQuotationEvaluation> implements ProcurementRoleAssignable {

    @SpringBean
    protected TenderQuotationEvaluationService tenderQuotationEvaluationService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    public EditTenderQuotationEvaluationPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = tenderQuotationEvaluationService;
    }

    public EditTenderQuotationEvaluationPage() {
        this(new PageParameters());
    }

    @Override
    protected void onInitialize() {
        editForm.attachFm("tenderQuotationEvaluationForm");
        super.onInitialize();

        ComponentUtil.addDateField(editForm, "closingDate");
        editForm.add(new BidPanel("bids"));

        final FileInputBootstrapFormComponent formDocs = new FileInputBootstrapFormComponent("formDocs");
        editForm.add(formDocs);
    }

    @Override
    protected TenderQuotationEvaluation newInstance() {
        final TenderQuotationEvaluation tenderQuotationEvaluation = super.newInstance();
        tenderQuotationEvaluation.setTenderProcess(sessionMetadataService.getSessionTenderProcess());

        return tenderQuotationEvaluation;
    }

    @Override
    protected void beforeSaveEntity(final TenderQuotationEvaluation tenderQuotationEvaluation) {
        super.beforeSaveEntity(tenderQuotationEvaluation);

        final TenderProcess tenderProcess = tenderQuotationEvaluation.getTenderProcess();
        tenderProcess.addTenderQuotationEvaluation(tenderQuotationEvaluation);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final TenderQuotationEvaluation tenderQuotationEvaluation) {
        super.beforeDeleteEntity(tenderQuotationEvaluation);

        final TenderProcess tenderProcess = tenderQuotationEvaluation.getTenderProcess();
        tenderProcess.removeTenderQuotationEvaluation(tenderQuotationEvaluation);
        tenderProcessService.save(tenderProcess);
    }
}
