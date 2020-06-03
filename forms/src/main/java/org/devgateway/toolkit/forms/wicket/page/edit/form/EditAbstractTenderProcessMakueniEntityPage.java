package org.devgateway.toolkit.forms.wicket.page.edit.form;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapCancelButton;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.events.EditingDisabledEvent;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

/**
 * @author mihai
 */
public abstract class EditAbstractTenderProcessMakueniEntityPage<T extends AbstractTenderProcessMakueniEntity>
        extends EditAbstractMakueniEntityPage<T> {
    protected static final Logger logger = LoggerFactory.getLogger(EditAbstractTenderProcessMakueniEntityPage.class);


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

    @Override
    protected void onInitialize() {
        super.onInitialize();

        editForm.add(new GenericSleepFormComponent<>("tenderProcess.project.procurementPlan.department"));
        editForm.add(new GenericSleepFormComponent<>("tenderProcess.project.procurementPlan.fiscalYear"));

        if (isTerminated()) {
            alertTerminated.setVisibilityAllowed(true);
        }

        saveTerminateButton.setVisibilityAllowed(!isTerminated()
                && editForm.getModelObject().getDirectChildrenEntitiesNotNull().isEmpty());

        if (!ObjectUtils.isEmpty(getNextForm())) {
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

    protected abstract AbstractTenderProcessMakueniEntity getNextForm();

    @Override
    protected ModalSaveEditPageButton getRevertToDraftPageButton() {
        ModalSaveEditPageButton revertToDraftPageButton = super.getRevertToDraftPageButton();
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
                    new StringResourceModel("reactivateModal", this, null),
                    Model.of("REACTIVATE"), Buttons.Type.Warning
            );
        }
        return revertToDraftModal;
    }

    @Override
    public boolean isTerminated() {
        final TenderProcess tenderProcess = editForm.getModelObject().getTenderProcess();
        return super.isTerminated() || (tenderProcess != null && tenderProcess.isTerminated());
    }
}
