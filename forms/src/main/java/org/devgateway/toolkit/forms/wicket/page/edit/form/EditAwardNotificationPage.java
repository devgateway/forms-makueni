package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.validators.AfterThanDateValidator;
import org.devgateway.toolkit.forms.validators.BigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.components.form.DateFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.BasePage;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.AbstractPurchaseReqMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.form.AwardNotificationService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.annotation.mount.MountPath;

import java.math.BigDecimal;

/**
 * @author gmutuhu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditAwardNotificationPage extends EditAbstractTenderReqMakueniEntity<AwardNotification> {
    @SpringBean
    protected AwardNotificationService awardNotificationService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    private Select2ChoiceBootstrapFormComponent<Supplier> awardeeSelector;

    private GenericSleepFormComponent supplierID;

    private GenericSleepFormComponent supplierAddress;

    public EditAwardNotificationPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = awardNotificationService;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        ComponentUtil.addBigDecimalField(editForm, "awardValue").required()
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());

        final DateFieldBootstrapFormComponent awardDate = ComponentUtil.addDateField(editForm, "awardDate");
        awardDate.required();
        final Tender tender = editForm.getModelObject().getTenderProcess().getSingleTender();
        if (tender != null && tender.getInvitationDate() != null) {
            awardDate.getField().add(new AfterThanDateValidator(tender.getInvitationDate()));
        }

        ComponentUtil.addIntegerTextField(editForm, "acknowledgementDays")
                .getField().add(RangeValidator.minimum(0));

        addSupplierInfo();

        final FileInputBootstrapFormComponent formDocs = new FileInputBootstrapFormComponent("formDocs");
        formDocs.required();
        editForm.add(formDocs);
    }

    @Override
    protected AbstractPurchaseReqMakueniEntity getNextForm() {
        return editForm.getModelObject().getPurchaseRequisition().getSingleAwardAcceptance();
    }

    @Override
    protected AwardNotification newInstance() {
        final AwardNotification awardNotification = super.newInstance();
        awardNotification.setTenderProcess(sessionMetadataService.getSessionTenderProcess());

        return awardNotification;
    }

    @Override
    protected void beforeSaveEntity(final AwardNotification awardNotification) {
        super.beforeSaveEntity(awardNotification);

        final TenderProcess tenderProcess = awardNotification.getTenderProcess();
        tenderProcess.addAwardNotification(awardNotification);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final AwardNotification awardNotification) {
        super.beforeDeleteEntity(awardNotification);

        final TenderProcess tenderProcess = awardNotification.getTenderProcess();
        tenderProcess.removeAwardNotification(awardNotification);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected Class<? extends BasePage> pageAfterSubmitAndNext() {
        return EditAwardAcceptancePage.class;
    }

    @Override
    protected PageParameters parametersAfterSubmitAndNext() {
        final PageParameters pp = new PageParameters();
        if (!ObjectUtils.isEmpty(editForm.getModelObject().getTenderProcess().getAwardAcceptance())) {
            pp.set(WebConstants.PARAM_ID,
                    PersistenceUtil.getNext(
                            editForm.getModelObject().getTenderProcess().getAwardAcceptance()).getId());
        }

        return pp;
    }

    private void addSupplierInfo() {
        awardeeSelector = new Select2ChoiceBootstrapFormComponent<>("awardee",
                new GenericChoiceProvider<>(getSuppliersInTenderQuotation()));
        awardeeSelector.required();
        awardeeSelector.getField().add(new AwardeeAjaxComponentUpdatingBehavior("change"));
        editForm.add(awardeeSelector);

        supplierID = new GenericSleepFormComponent<>("supplierID", (IModel<String>) () -> {
            if (awardeeSelector.getModelObject() != null) {
                return awardeeSelector.getModelObject().getCode();
            }
            return null;
        });
        supplierID.setOutputMarkupId(true);
        editForm.add(supplierID);

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
            target.add(supplierID);
            target.add(supplierAddress);
        }
    }
}
