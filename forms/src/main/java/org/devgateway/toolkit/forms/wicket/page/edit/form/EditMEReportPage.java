package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.behaviors.CountyAjaxFormComponentUpdatingBehavior;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2MultiChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.MEPaymentRoleAssignable;
import org.devgateway.toolkit.persistence.dao.categories.SubWard;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.category.MEStatusService;
import org.devgateway.toolkit.persistence.service.category.SubWardService;
import org.devgateway.toolkit.persistence.service.category.SubcountyService;
import org.devgateway.toolkit.persistence.service.category.WardService;
import org.devgateway.toolkit.persistence.service.form.MEReportService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author mpostelnicu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditMEReportPage extends EditAbstractImplTenderProcessEntityPage<MEReport>
        implements MEPaymentRoleAssignable {

    @SpringBean
    protected MEReportService service;

    @SpringBean
    protected MEStatusService meStatusService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    @SpringBean
    protected SubcountyService subcountyService;

    @SpringBean
    protected WardService wardService;

    @SpringBean
    protected SubWardService subWardService;

    private Select2MultiChoiceBootstrapFormComponent<Ward> wards;
    private Select2MultiChoiceBootstrapFormComponent<Subcounty> subcounties;
    private Select2MultiChoiceBootstrapFormComponent<SubWard> subwards;

    public EditMEReportPage(PageParameters parameters) {
        super(parameters);
        this.jpaService = service;
    }

    public EditMEReportPage() {
        this(new PageParameters());
    }

    @Override
    protected MEReport newInstance() {
        final MEReport ar = super.newInstance();
        ar.setTenderProcess(sessionMetadataService.getSessionTenderProcess());
        return ar;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Fragment inspectionExtraFields = new Fragment("childExtraFields", "meExtraFields", this);
        abstractImplExtraFields.replace(inspectionExtraFields);
        inspectionExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.singleContract.contractDate"));
        inspectionExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.singleContract.expiryDate"));
        inspectionExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.project.amountBudgeted"));

        ComponentUtil.addIntegerTextField(editForm, "sno");
        ComponentUtil.addBigDecimalBudgetAmountField(editForm, "lpoAmount");
        ComponentUtil.addTextField(editForm, "lpoNumber");
        ComponentUtil.addBigDecimalBudgetAmountField(editForm, "expenditure").required();
        ComponentUtil.addBigDecimalBudgetAmountField(editForm, "uncommitted");
        ComponentUtil.addTextAreaField(editForm, "projectScope");
        ComponentUtil.addTextAreaField(editForm, "output");
        ComponentUtil.addTextAreaField(editForm, "outcome");
        ComponentUtil.addTextAreaField(editForm, "projectProgress").required();
        ComponentUtil.addIntegerTextField(editForm, "directBeneficiariesTarget").required();
        ComponentUtil.addTextAreaField(editForm, "wayForward").required();
        ComponentUtil.addDateField(editForm, "byWhen");
        ComponentUtil.addYesNoToggle(editForm, "inspected", true).required();
        ComponentUtil.addYesNoToggle(editForm, "invoiced", true).required();
        ComponentUtil.addTextField(editForm, "officerResponsible").required();
        ComponentUtil.addSelect2ChoiceField(editForm, "meStatus", meStatusService).required();
        ComponentUtil.addTextAreaField(editForm, "remarks").required();
        ComponentUtil.addTextField(editForm, "contractorContact");


        ComponentUtil.addDateField(editForm, "approvedDate").required();

        subwards = ComponentUtil.addSelect2MultiChoiceField(editForm, "subwards", subWardService);
        subwards.required();

        wards = ComponentUtil.addSelect2MultiChoiceField(editForm, "wards", wardService);
        wards.getField()
                .add(new CountyAjaxFormComponentUpdatingBehavior<>(wards, subwards,
                        LoadableDetachableModel.of(() -> subWardService), editForm.getModelObject()::setSubwards,
                        "change"
                ));
        wards.required();

        subcounties = ComponentUtil.addSelect2MultiChoiceField(editForm, "subcounties", subcountyService);
        subcounties.getField()
                .add(new CountyAjaxFormComponentUpdatingBehavior<>(subcounties, wards,
                        LoadableDetachableModel.of(() -> wardService), editForm.getModelObject()::setWards,
                        "change"
                ));
        subcounties.getField().add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                subwards.getField().getModelObject().clear();
                target.add(subwards);
            }
        });
        subcounties.required();
    }

    @Override
    protected void beforeSaveEntity(final MEReport report) {
        super.beforeSaveEntity(report);

        final TenderProcess tenderProcess = report.getTenderProcess();
        tenderProcess.addMEReport(report);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final MEReport report) {
        super.beforeDeleteEntity(report);

        final TenderProcess tenderProcess = report.getTenderProcess();
        tenderProcess.removeMEReport(report);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected AbstractTenderProcessMakueniEntity getNextForm() {
        return null;
    }

    @Override
    protected void onApproved() {
        super.onApproved();
        service.onApproved(editForm.getModelObject());
    }
}
