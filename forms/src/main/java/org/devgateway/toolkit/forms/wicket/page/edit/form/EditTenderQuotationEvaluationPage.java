package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.BidPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.service.form.TenderQuotationEvaluationService;
import org.devgateway.toolkit.web.security.SecurityConstants;
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

        Fragment extraFields = new Fragment("extraReadOnlyFields", "extraReadOnlyFields", this);
        IModel<Tender> tenderModel = editForm.getModel()
                .map(AbstractTenderProcessMakueniEntity::getTenderProcess)
                .map(TenderProcess::getSingleTender);
        extraFields.add(new GenericSleepFormComponent<>("tenderNumber",
                tenderModel.map(Tender::getTenderNumber)));
        extraFields.add(new GenericSleepFormComponent<>("tenderTitle",
                tenderModel.map(Tender::getTenderTitle)));
        editForm.replace(extraFields);

        ComponentUtil.addDateField(editForm, "openingDate")
                .setShowTooltip(true);
        ComponentUtil.addDateField(editForm, "closingDate")
                .setShowTooltip(true);
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
}
