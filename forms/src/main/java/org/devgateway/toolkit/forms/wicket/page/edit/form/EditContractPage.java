package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.validators.AfterThanDateValidator;
import org.devgateway.toolkit.forms.validators.BigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.components.form.DateFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.ContractDocumentPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptanceItem;
import org.devgateway.toolkit.persistence.dao.form.AwardNotificationItem;
import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.category.ProcuringEntityService;
import org.devgateway.toolkit.persistence.service.form.ContractService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author gmutuhu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditContractPage extends EditAbstractTenderReqMakueniEntityPage<Contract> implements
        ProcurementRoleAssignable {
    @SpringBean
    protected ContractService contractService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    @SpringBean
    protected ProcuringEntityService procuringEntityService;

    private Select2ChoiceBootstrapFormComponent<Supplier> awardeeSelector;

    private GenericSleepFormComponent supplierAddress;

    public EditContractPage() {
        this(new PageParameters());
    }

    public EditContractPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = contractService;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        ComponentUtil.addTextField(editForm, "referenceNumber").required();
        ComponentUtil.addBigDecimalField(editForm, "contractValue").required()
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());

        final DateFieldBootstrapFormComponent contractDate = ComponentUtil.addDateField(editForm, "contractDate");
        contractDate.required();

        AwardNotificationItem acceptedNotification = editForm.getModelObject().getTenderProcess()
                .getSingleAwardNotification().getAcceptedNotification();

        if (acceptedNotification != null && acceptedNotification.getAwardDate() != null) {
            contractDate.getField().add(new AfterThanDateValidator(acceptedNotification.getAwardDate()));
        }

        ComponentUtil.addDateField(editForm, "contractApprovalDate").required();
        ComponentUtil.addDateField(editForm, "expiryDate").required();
        ComponentUtil.addSelect2ChoiceField(editForm, "procuringEntity", procuringEntityService).required();

        addSupplierInfo();

        editForm.add(new ContractDocumentPanel("contractDocs"));
    }

    @Override
    protected void setButtonsPermissions() {
        super.setButtonsPermissions();

        submitAndNext.setVisibilityAllowed(false);
    }

    @Override
    protected AbstractTenderProcessMakueniEntity getNextForm() {
        return null;
    }

    @Override
    protected Contract newInstance() {
        final Contract contract = super.newInstance();
        contract.setTenderProcess(sessionMetadataService.getSessionTenderProcess());

        return contract;
    }

    @Override
    protected void beforeSaveEntity(final Contract contract) {
        super.beforeSaveEntity(contract);

        final TenderProcess tenderProcess = contract.getTenderProcess();
        tenderProcess.addContract(contract);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final Contract contract) {
        super.beforeDeleteEntity(contract);

        final TenderProcess tenderProcess = contract.getTenderProcess();
        tenderProcess.removeContract(contract);
        tenderProcessService.save(tenderProcess);
    }

    public static List<Supplier> getAcceptedSupplier(TenderProcess tenderProcess) {
        return tenderProcess.getSingleAwardAcceptance().getItems()
                .stream()
                .filter(AwardAcceptanceItem::isAccepted)
                .map(AwardAcceptanceItem::getAwardee)
                .filter(Objects::nonNull)
                .findFirst().map(Arrays::asList).orElseGet(Arrays::asList);
    }


    private void addSupplierInfo() {
        awardeeSelector = new Select2ChoiceBootstrapFormComponent<>("awardee",
                new GenericChoiceProvider<>(getAcceptedSupplier(editForm.getModelObject().getTenderProcess()))
        );
        awardeeSelector.required();
        awardeeSelector.setEnabled(!editForm.getModelObject().getTenderProcess().hasNonDraftImplForms());
        awardeeSelector.getField().add(new AwardeeAjaxComponentUpdatingBehavior("change"));
        editForm.add(awardeeSelector);

        supplierAddress = new GenericSleepFormComponent<>("supplierAddress", (IModel<String>) () -> {
            if (awardeeSelector.getModelObject() != null) {
                return awardeeSelector.getModelObject().getAddress();
            }
            return null;
        });
        supplierAddress.setOutputMarkupId(true);
        editForm.add(supplierAddress);

    }

    class AwardeeAjaxComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        AwardeeAjaxComponentUpdatingBehavior(final String event) {
            super(event);
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
            target.add(supplierAddress);
        }
    }
}
