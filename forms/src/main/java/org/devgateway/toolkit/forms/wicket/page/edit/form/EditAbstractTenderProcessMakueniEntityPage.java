package org.devgateway.toolkit.forms.wicket.page.edit.form;

import com.google.common.collect.ImmutableMap;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapCancelButton;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.events.EditingDisabledEvent;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Map;

/**
 * @author mihai
 */
public abstract class EditAbstractTenderProcessMakueniEntityPage<T extends AbstractTenderProcessMakueniEntity>
        extends EditAbstractMakueniEntityPage<T> {
    protected static final Logger logger = LoggerFactory.getLogger(EditAbstractTenderProcessMakueniEntityPage.class);

    public static final Map<Class<?>, Class<? extends Page>> PAGE_CLASS_BY_FORM_CLASS =
            ImmutableMap.<Class<?>, Class<? extends Page>>builder()
                    .put(TenderProcess.class, EditPurchaseRequisitionGroupPage.class)
                    .put(Tender.class, EditTenderPage.class)
                    .put(TenderQuotationEvaluation.class, EditTenderQuotationEvaluationPage.class)
                    .put(ProfessionalOpinion.class, EditProfessionalOpinionPage.class)
                    .put(AwardNotification.class, EditAwardNotificationPage.class)
                    .put(AwardAcceptance.class, EditAwardAcceptancePage.class)
                    .put(Contract.class, EditContractPage.class)
                    .build();

    @SpringBean
    private TenderProcessService tenderProcessService;

    public EditAbstractTenderProcessMakueniEntityPage(final PageParameters parameters) {
        super(parameters);

        final Fragment fragment = new Fragment("extraReadOnlyFields", "noExtraReadOnlyFields", this);
        editForm.add(fragment);
    }

    @Override
    protected void checkInitParameters() {
        // check if this is a new object and redirect user to dashboard page if we don't have all the needed info
        if (entityId == null && sessionMetadataService.getSessionTenderProcess() == null) {
            logger.warn("Something wrong happened since we are trying to add a new AbstractPurchaseReqMakueni Entity "
                    + "without having a TenderProcess!");
            setResponsePage(StatusOverviewPage.class);
        }
    }

    protected class TenderProcessFormValidator implements IFormValidator {
        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[0];
        }

        @Override
        public void validate(Form<?> form) {
            T obj = (T) form.getModelObject();
            BindingResult validate = tenderProcessService.validate(obj.getTenderProcess(), obj);
            objectErrorToWicketError(form, validate.getAllErrors());
        }
    }

    protected void objectErrorToWicketError(Form<?> form, List<ObjectError> objectErrors) {
        objectErrors.forEach(oe -> form.error(oe.getCode()));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        editForm.add(new GenericSleepFormComponent<>("tenderProcess.procurementPlan.department"));
        editForm.add(new GenericSleepFormComponent<>("tenderProcess.procurementPlan.fiscalYear"));
        editForm.add(new TenderProcessFormValidator());
        if (isTerminated()) {
            alertTerminated.setVisibilityAllowed(true);
        }

        saveTerminateButton.setVisibilityAllowed(!ComponentUtil.isPrintMode() && !isTerminated()
                && editForm.getModelObject().getDirectChildrenEntitiesNotNull().isEmpty());

        if (editForm.getModelObject().hasDownstreamForms()
                && getPageParameters().get(WebConstants.PARAM_DELETE_ENABLED).isEmpty()) {
            deleteButton.setVisibilityAllowed(false);
        }
    }

    @Override
    protected void checkAndSendEventForDisableEditing() {
        super.checkAndSendEventForDisableEditing();
        if (isTerminated()) {
            send(getPage(), Broadcast.BREADTH, new EditingDisabledEvent());
        }
    }

    @Override
    protected void afterSaveEntity(T saveable) {
        super.afterSaveEntity(saveable);

        TenderProcess tp = editForm.getModelObject().getTenderProcess();
        if (tp != null) {
            sessionMetadataService.setSessionTenderProcess(tp);
        }
        Department department = editForm.getModelObject().getDepartment();
        if (department != null) {
            sessionMetadataService.setSessionDepartment(department);
        }

        Project project = editForm.getModelObject().getProject();
        if (project != null) {
            sessionMetadataService.setSessionProject(project);
        }
    }

    @Override
    protected BootstrapCancelButton getCancelButton() {
        return new BootstrapCancelButton("cancel", new StringResourceModel("cancelButton", this, null)) {
            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                final T saveable = editForm.getModelObject();
                afterSaveEntity(saveable);
                setResponsePage(listPageClass);
            }
        };
    }

    @Override
    protected SaveEditPageButton getRevertToDraftPageButton() {
        SaveEditPageButton revertToDraftPageButton = super.getRevertToDraftPageButton();
        if (DBConstants.Status.TERMINATED.equals(editForm.getModelObject().getStatus())) {
            revertToDraftPageButton.setLabel(new StringResourceModel("reactivate", this, null));
        }
        return revertToDraftPageButton;
    }

    @Override
    protected ButtonContentModal createRevertToDraftModal() {
        ButtonContentModal revertToDraftModal = super.createRevertToDraftModal();
        if (DBConstants.Status.TERMINATED.equals(editForm.getModelObject().getStatus())) {
            revertToDraftModal = new ButtonContentModal(
                    "revertToDraftModal",
                    new StringResourceModel("reactivateModal.content", this),
                    new StringResourceModel("reactivateModal.reactivate", this), Buttons.Type.Warning
            );
        }
        return revertToDraftModal;
    }

    @Override
    public boolean isTerminated() {
        final TenderProcess tenderProcess = editForm.getModelObject().getTenderProcess();
        return super.isTerminated() || (tenderProcess != null && tenderProcess.isTerminated());
    }

    @Override
    protected Class<? extends Page> pageAfterSubmitAndNext() {
        Class<?> formClass;
        T entity = editForm.getModelObject();
        formClass = entity.getNextForm();
        if (formClass == null) {
            return getPage().getClass();
        }
        Class<?> visibleFormClass = tenderProcessService.getFirstVisibleDownstreamForm(formClass);
        if (visibleFormClass != null) {
            return PAGE_CLASS_BY_FORM_CLASS.getOrDefault(visibleFormClass, getPage().getClass());
        } else {
            return getPage().getClass();
        }
    }

    @Override
    protected PageParameters parametersAfterSubmitAndNext() {
        final PageParameters pp = new PageParameters();
        AbstractMakueniEntity nextStatusable = tenderProcessService.getNextStatusable(
                editForm.getModelObject().getTenderProcess(), editForm.getModelObject().getClass());
        if (!ObjectUtils.isEmpty(nextStatusable)) {
            pp.set(WebConstants.PARAM_ID, nextStatusable.getId());
        }
        return pp;
    }
}
