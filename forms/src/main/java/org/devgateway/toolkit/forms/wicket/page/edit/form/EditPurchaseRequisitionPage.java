package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.validators.UniquePropertyEntryValidator;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.ItemDetailPanel;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListPurchaseRequisitionPage;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisition;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisition_;
import org.devgateway.toolkit.persistence.service.category.ChargeAccountService;
import org.devgateway.toolkit.persistence.service.category.StaffService;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.persistence.service.form.PurchaseRequisitionService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author idobre
 * @since 2019-04-17
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath("/purchaseRequisition")
public class EditPurchaseRequisitionPage extends EditAbstractMakueniFormPage<PurchaseRequisition> {
    @SpringBean
    private PurchaseRequisitionService purchaseRequisitionService;

    @SpringBean
    protected ProcurementPlanService procurementPlanService;

    @SpringBean
    protected StaffService staffService;

    @SpringBean
    protected ChargeAccountService chargeAccountService;

    public EditPurchaseRequisitionPage(final PageParameters parameters) {
        super(parameters);

        this.jpaService = purchaseRequisitionService;
        this.listPageClass = ListPurchaseRequisitionPage.class;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        ComponentUtil.addSelect2ChoiceField(editForm, "procurementPlan", procurementPlanService).required();

        final TextFieldBootstrapFormComponent<String> purchaseRequestNumber =
                ComponentUtil.addTextField(editForm, "purchaseRequestNumber");
        purchaseRequestNumber.required();
        purchaseRequestNumber.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        purchaseRequestNumber.getField().add(addUniquePurchaseRequestNumberValidator());

        final TextFieldBootstrapFormComponent<String> title = ComponentUtil.addTextField(editForm, "title");
        title.required();
        title.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        title.getField().add(uniqueTitle());

        ComponentUtil.addSelect2ChoiceField(editForm, "requestedBy", staffService).required();
        ComponentUtil.addSelect2ChoiceField(editForm, "chargeAccount", chargeAccountService).required();
        ComponentUtil.addDateField(editForm, "requestApprovalDate").required();

        editForm.add(new ItemDetailPanel("itemDetails"));

        ComponentUtil.addDateField(editForm, "approvedDate").required();

        final FileInputBootstrapFormComponent purchaseRequestDocs =
                new FileInputBootstrapFormComponent("purchaseRequestDocs");
        purchaseRequestDocs.required();
        editForm.add(purchaseRequestDocs);
    }

    private IValidator addUniquePurchaseRequestNumberValidator() {
        return new UniquePropertyEntryValidator<>(
                getString("uniqueNumber"),
                purchaseRequisitionService::findOne,
                (o, v) -> (root, query, cb) -> cb.equal(root.get(PurchaseRequisition_.purchaseRequestNumber), v),
                editForm.getModel());
    }

    @Override
    protected PurchaseRequisition newInstance() {
        final PurchaseRequisition purchaseRequisition = jpaService.newInstance();
        // purchaseRequisition.setProcurementPlan(procurementPlan);  // here we need to set the ProcurementPlan
        return purchaseRequisition;
    }

    private IValidator<String> uniqueTitle() {
        final StringValue id = getPageParameters().get(WebConstants.PARAM_ID);
        return new UniqueTitleValidator(id.toLong(-1));
    }

    public class UniqueTitleValidator implements IValidator<String> {
        private final Long id;

        public UniqueTitleValidator(final Long id) {
            this.id = id;
        }

        @Override
        public void validate(final IValidatable<String> validatable) {
            final String titleValue = validatable.getValue();
            final ProcurementPlan procurementPlan = editForm.getModelObject().getProcurementPlan();

            if (procurementPlan != null && titleValue != null) {
                if (purchaseRequisitionService
                        .countByProcurementPlanAndTitleAndIdNot(procurementPlan, titleValue, id) > 0) {
                    final ValidationError error = new ValidationError(getString("uniqueTitle"));
                    validatable.error(error);
                }
            }
        }
    }
}
