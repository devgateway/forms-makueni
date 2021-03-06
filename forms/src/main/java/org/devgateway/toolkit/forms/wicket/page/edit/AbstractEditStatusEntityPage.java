/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.forms.wicket.page.edit;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Alert;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.TextContentModal;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeIconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.ladda.LaddaAjaxButton;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.devgateway.ocds.forms.wicket.FormSecurityUtil;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapSubmitButton;
import org.devgateway.toolkit.forms.wicket.components.form.CheckBoxYesNoToggleBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.OptionallyRequiredTextAreaFieldComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextAreaFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.events.EditingDisabledEvent;
import org.devgateway.toolkit.forms.wicket.events.EditingEnabledEvent;
import org.devgateway.toolkit.persistence.dao.AbstractStatusAuditableEntity;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Person;
import org.devgateway.toolkit.persistence.dao.StatusChangedComment;
import org.devgateway.toolkit.persistence.dao.form.Lockable;
import org.devgateway.toolkit.persistence.dao.form.Terminatable;
import org.devgateway.toolkit.persistence.service.LockableService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.devgateway.toolkit.web.security.SecurityUtil;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;
import org.wicketstuff.select2.Select2Choice;

/**
 * @author mpostelnicu
 * Page used to make editing easy, extend to get easy access to one entity for editing
 */
public abstract class AbstractEditStatusEntityPage<T extends AbstractStatusAuditableEntity>
        extends AbstractEditPage<T> implements Terminatable {

    protected Fragment entityButtonsFragment;

    private SaveEditPageButton saveSubmitButton;

    protected SaveEditPageButton submitAndNext;

    private SaveEditPageButton saveApproveButton;

    private SaveEditPageButton saveDraftContinueButton;

    protected SaveEditPageButton revertToDraftPageButton;

    protected ModalSaveEditPageButton saveTerminateButton;

    protected ButtonContentModal terminateModal;

    private CheckBoxYesNoToggleBootstrapFormComponent visibleStatusComments;

    private TransparentWebMarkupContainer statusCommentsWrapper;

    private ListView<StatusChangedComment> statusComments;

    protected TextAreaFieldBootstrapFormComponent<String> newStatusComment;

    private Label statusLabel;

    private String previousStatus;

    private Label autoSaveLabel;

    private HiddenField<Double> verticalPosition;

    private HiddenField<Double> maxHeight;

    protected PageParameters afterSubmitNextParameters;

    protected Fragment extraStatusEntityButtonsFragment;

    public AbstractEditStatusEntityPage(final PageParameters parameters) {
        super(parameters);

    }


    public class ButtonContentModal extends TextContentModal {
        private final Buttons.Type buttonType;
        private LaddaAjaxButton button;
        private IModel<String> buttonModel;
        private ModalSaveEditPageButton modalSavePageButton;

        public ButtonContentModal(String markupId, IModel<String> model, IModel<String> buttonModel,
                                  Buttons.Type buttonType) {
            super(markupId, model);
            addCloseButton();
            this.buttonModel = buttonModel;
            this.buttonType = buttonType;
        }

        public ButtonContentModal modalSavePageButton(ModalSaveEditPageButton modalSavePageButton) {
            this.modalSavePageButton = modalSavePageButton;
            return this;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            button = new LaddaAjaxButton("button", buttonType) {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    modalSavePageButton.continueSubmit(target);
                }
            };
            addButton(button);
            button.setDefaultFormProcessing(false);
            button.setLabel(buttonModel);
            enableDisableAutosaveFields();
        }
    }

    protected ButtonContentModal createTerminateModal() {
        ButtonContentModal buttonContentModal = new ButtonContentModal(
                "terminateModal",
                new StringResourceModel("confirmTerminateModal.content", this),
                new StringResourceModel("confirmTerminateModal.terminate", this), Buttons.Type.Danger);
        return buttonContentModal;
    }

    public class ModalSaveEditPageButton extends SaveEditPageButton {
        private TextContentModal modal;

        public ModalSaveEditPageButton(String id, IModel<String> model, TextContentModal modal) {
            super(id, model);
            this.modal = modal;
        }


        @Override
        protected String getOnClickScript() {
            return WebConstants.DISABLE_FORM_LEAVING_JS;
        }

        @Override
        protected void onSubmit(AjaxRequestTarget target) {
            modal.show(true);
            target.add(modal);
        }

        public void continueSubmit(AjaxRequestTarget target) {
            super.onSubmit(target);
        }
    }

    @Override
    protected void checkIn(T saveable) {
        super.checkIn(saveable);

        T object = editForm.getModelObject();
        if (object instanceof Lockable) {
            ((Lockable) object).setOwner(null);
        }
    }

    @Override
    protected void beforeSaveEntity(T saveable) {
        super.beforeSaveEntity(saveable);

        afterSubmitNextParameters = parametersAfterSubmitAndNext();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addAutosaveLabel();
        addVerticalMaxPositionFields();

        statusLabel = addStatusLabel();
        editForm.add(statusLabel);

        visibleStatusComments = getVisibleStatusComments();
        editForm.add(visibleStatusComments);
        visibleStatusComments.setVisibilityAllowed(!ComponentUtil.isPrintMode());

        statusCommentsWrapper = new TransparentWebMarkupContainer("statusCommentsWrapper");
        statusCommentsWrapper.setOutputMarkupId(true);
        statusCommentsWrapper.setOutputMarkupPlaceholderTag(true);
        statusCommentsWrapper.setVisibilityAllowed(false);
        editForm.add(statusCommentsWrapper);
        statusCommentsWrapper.setVisibilityAllowed(!ComponentUtil.isPrintMode());

        statusComments = getStatusCommentsListView();
        newStatusComment = getNewStatusCommentField();
        newStatusComment.setVisibilityAllowed(!ComponentUtil.isPrintMode());
        statusCommentsWrapper.add(statusComments);
        editForm.add(newStatusComment);

        entityButtonsFragment = new Fragment("extraButtons", "entityButtons", this);
        editForm.replace(entityButtonsFragment);

        Fragment fragment = new Fragment("extraStatusEntityButtons", "noButtons", this);
        entityButtonsFragment.add(fragment);

        saveSubmitButton = getSaveSubmitPageButton();
        entityButtonsFragment.add(saveSubmitButton);

        submitAndNext = getSubmitAndNextPageButton();
        entityButtonsFragment.add(submitAndNext);

        saveApproveButton = getSaveApprovePageButton();
        entityButtonsFragment.add(saveApproveButton);

        saveDraftContinueButton = getSaveDraftAndContinueButton();
        entityButtonsFragment.add(saveDraftContinueButton);

        revertToDraftPageButton = getRevertToDraftPageButton();
        entityButtonsFragment.add(revertToDraftPageButton);


        saveTerminateButton = getSaveTerminateButton();
        entityButtonsFragment.add(saveTerminateButton);
        entityButtonsFragment.add(terminateModal);


        applyDraftSaveBehavior(saveButton);
        applyDraftSaveBehavior(saveDraftContinueButton);
        applyDraftSaveBehavior(revertToDraftPageButton);

        editForm.add(createCheckoutModal());
        editForm.add(createCheckedOutByOtherAlert());
    }

    protected Component createCheckedOutByOtherAlert() {
        Person principal = SecurityUtil.getCurrentAuthenticatedPerson();

        T object = editForm.getModelObject();
        Person owner = object instanceof Lockable ? ((Lockable) object).getOwner() : null;

        StringResourceModel message = new StringResourceModel("createCheckedOutByOtherAlert", this)
                .setParameters(owner == null ? "" : owner.getUsername());

        return new Alert("createCheckedOutByOtherAlert", message)
                .type(Alert.Type.Danger)
                .setVisibilityAllowed(owner != null && !owner.equals(principal));
    }

    protected Modal<String> createCheckoutModal() {
        TextContentModal modal = new TextContentModal("checkoutModal", new ResourceModel("checkoutModal.text")) {

            @Override
            protected void onConfigure() {
                super.onConfigure();

                show(isCheckoutModalVisible());
            }
        };
        modal.addCloseButton(new ResourceModel("checkoutModal.view"));

        final LaddaAjaxButton checkoutButton = new LaddaAjaxButton("button", Buttons.Type.Primary) {

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                super.onSubmit(target);
                checkout(target);
                modal.close(target);
            }
        };
        checkoutButton.setDefaultFormProcessing(false);
        checkoutButton.setLabel(new ResourceModel("checkoutModal.edit"));

        modal.addButton(checkoutButton);

        modal.setFadeIn(false);

        return modal;
    }

    protected boolean isCheckoutModalVisible() {
        T object = editForm.getModelObject();
        return object instanceof Lockable
                && !object.isNew()
                && ((Lockable) object).getOwner() == null
                && DBConstants.Status.DRAFT.equals(object.getStatus())
                && !isViewMode();
    }

    private void checkout(AjaxRequestTarget target) {
        try {
            T object = editForm.getModelObject();
            ((Lockable) object).setOwner(SecurityUtil.getCurrentAuthenticatedPerson());

            T savedObject = jpaService.save(object);
            editForm.setModelObject(savedObject);

            target.add(editForm);
        } catch (ObjectOptimisticLockingFailureException e) {
            showSaveFailedModal(target, "optimistic_lock_error_message");
        }
    }

    @Override
    protected void onCancel(AjaxRequestTarget target) {
        if (jpaService instanceof LockableService && !editForm.getModelObject().isNew()) {
            ((LockableService<?>) jpaService).unlock(editForm.getModelObject().getId());
        }
    }

    @Override
    protected T newInstance() {
        T newEntity = super.newInstance();
        if (newEntity instanceof Lockable) {
            ((Lockable) newEntity).setOwner(SecurityUtil.getCurrentAuthenticatedPerson());
        }
        return newEntity;
    }

    @Override
    protected void afterSaveEntity(final T saveable) {
        super.afterSaveEntity(saveable);

        getPageParameters().set(WebConstants.V_POSITION, verticalPosition.getValue())
                .set(WebConstants.MAX_HEIGHT, maxHeight.getValue());
    }

    @Override
    protected void afterLoad(final IModel<T> entityModel) {
        super.afterLoad(entityModel);
        previousStatus = entityModel.getObject().getStatus();
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        this.statusLabel.setVisibilityAllowed(editForm.getModelObject().getVisibleStatusLabel());
    }

    protected void checkAndSendEventForDisableEditing() {
        Object payload;
        if (isDisableEditingEvent()) {
            payload = new EditingDisabledEvent();
        } else {
            payload = new EditingEnabledEvent();
        }
        send(getPage(), Broadcast.BREADTH, payload);
    }

    public boolean isDisableEditingEvent() {
        if (editForm.getModelObject() instanceof Lockable && !isCheckedOutByPrincipal()) {
            return true;
        }
        return !Strings.isEqual(editForm.getModelObject().getStatus(), DBConstants.Status.DRAFT) || isViewMode();
    }

    private boolean isCheckedOutByPrincipal() {
        Person principal = SecurityUtil.getCurrentAuthenticatedPerson();
        return principal != null && principal.equals(((Lockable) editForm.getModelObject()).getOwner());
    }

    protected abstract boolean isViewMode();

    private void addAutosaveLabel() {
        autoSaveLabel = new Label("autoSaveLabel",
                new StringResourceModel("autoSaveLabelMessage", this).setParameters(settingsUtils.getAutosaveTime()));
        autoSaveLabel.setVisibilityAllowed(false);
        autoSaveLabel.setOutputMarkupPlaceholderTag(true);
        autoSaveLabel.setOutputMarkupId(true);
        editForm.add(autoSaveLabel);
    }

    private void addVerticalMaxPositionFields() {
        verticalPosition = new HiddenField<>("verticalPosition", new Model<>(), Double.class);
        verticalPosition.setOutputMarkupId(true);
        editForm.add(verticalPosition);

        maxHeight = new HiddenField<>("maxHeight", new Model<>(), Double.class);
        maxHeight.setOutputMarkupId(true);
        editForm.add(maxHeight);
    }

    protected ModalSaveEditPageButton getSaveTerminateButton() {
        terminateModal = createTerminateModal();
        final ModalSaveEditPageButton saveEditPageButton = new ModalSaveEditPageButton("terminate",
                new StringResourceModel("terminate", this, null), terminateModal) {
            @Override
            public void continueSubmit(AjaxRequestTarget target) {
                setStatusAppendComment(DBConstants.Status.TERMINATED);
                super.continueSubmit(target);
            }
        };
        saveEditPageButton.setIconType(FontAwesomeIconType.archive);
        saveEditPageButton.setDefaultFormProcessing(true);
        terminateModal.modalSavePageButton(saveEditPageButton);
        return saveEditPageButton;
    }


    protected void enableDisableAutosaveFields() {
        addAutosaveBehavior();

        saveButton.setEnabled(true);
        saveDraftContinueButton.setEnabled(true);
        submitAndNext.setEnabled(true);
        saveSubmitButton.setEnabled(true);
    }

    private void addAutosaveBehavior() {
        // enable autosave
        if (!ComponentUtil.isPrintMode()
                && Strings.isEqual(editForm.getModelObject().getStatus(), DBConstants.Status.DRAFT)) {
            saveDraftContinueButton.add(getAutosaveBehavior());
            autoSaveLabel.setVisibilityAllowed(true);
        }
    }

    private AbstractAjaxTimerBehavior getAutosaveBehavior() {
        final AbstractAjaxTimerBehavior ajaxTimerBehavior = new AbstractAjaxTimerBehavior(
                Duration.minutes(settingsUtils.getAutosaveTime())) {
            @Override
            protected void onTimer(final AjaxRequestTarget target) {
                if (hasNonRecoverableError()) {
                    return;
                }

                // display block UI message until the page is reloaded
                target.prependJavaScript(getShowBlockUICode());

                // disable all fields from js and lose focus (execute this javascript code before components processed)
                target.prependJavaScript("$(document.activeElement).blur();");

                // invoke autosave from js (execute this javascript code before components processed)
                target.prependJavaScript("$('#" + maxHeight.getMarkupId() + "').val($(document).height()); "
                        + "$('#" + verticalPosition.getMarkupId() + "').val($(window).scrollTop()); "
                        + "$('#" + saveDraftContinueButton.getMarkupId() + "').click();");

                // disable all buttons from js
                target.prependJavaScript("$('#" + editForm.getMarkupId() + " button').prop('disabled', true);");
            }
        };

        return ajaxTimerBehavior;
    }

    private Label addStatusLabel() {
        statusLabel = new Label("statusLabel", editForm.getModelObject().getStatus());
        statusLabel.add(new AttributeModifier("class", new Model<>("label " + getStatusLabelClass())));
        statusLabel.setVisibilityAllowed(editForm.getModelObject().getVisibleStatusLabel());
        return statusLabel;
    }

    private String getStatusLabelClass() {
        if (editForm.getModelObject().getStatus() == null) {
            return "";
        }

        switch (editForm.getModelObject().getStatus()) {
            case DBConstants.Status.APPROVED:
                return "label-success";
            case DBConstants.Status.DRAFT:
                return "label-danger";
            case DBConstants.Status.SUBMITTED:
                return "label-warning";
            case DBConstants.Status.TERMINATED:
                return "label-term";
            default:
                return "";
        }
    }

    private CheckBoxYesNoToggleBootstrapFormComponent getVisibleStatusComments() {
        final CheckBoxYesNoToggleBootstrapFormComponent checkBoxBootstrapFormComponent =
                new CheckBoxYesNoToggleBootstrapFormComponent("visibleStatusComments") {
                    @Override
                    protected void onUpdate(final AjaxRequestTarget target) {
                        statusCommentsWrapper.setVisibilityAllowed(editForm.getModelObject()
                                .getVisibleStatusComments());
                        target.add(statusCommentsWrapper);
                    }

                    @Override
                    public void onEvent(final IEvent<?> event) {
                        // do nothing - keep this field enabled
                    }
                };
        checkBoxBootstrapFormComponent.setVisibilityAllowed(!isViewMode());
        return checkBoxBootstrapFormComponent;
    }

    private TextAreaFieldBootstrapFormComponent<String> getNewStatusCommentField() {
        final TextAreaFieldBootstrapFormComponent<String> comment =
                new OptionallyRequiredTextAreaFieldComponent<String>("newStatusComment") {

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();

                        T object = editForm.getModelObject();
                        setVisibilityAllowed(!(object instanceof Lockable)
                                || (object.getStatus().equals(DBConstants.Status.DRAFT) && isCheckedOutByPrincipal())
                                || !object.getStatus().equals(DBConstants.Status.DRAFT));
                    }

                    @Override
                    public boolean isRequired() {
                        return editForm.getRootForm().findSubmittingButton() == saveTerminateButton;
                    }

                    @Override
                    public void onEvent(final IEvent<?> event) {
                        // do nothing - keep this field enabled
                    }
                };
        comment.setShowTooltip(true);
        return comment;
    }

    private ListView<StatusChangedComment> getStatusCommentsListView() {
        final ListView<StatusChangedComment> statusComments = new ListView<StatusChangedComment>("statusComments") {
            @Override
            protected void populateItem(final ListItem<StatusChangedComment> item) {
                item.setModel(new CompoundPropertyModel<>(item.getModel()));
                item.add(new Label("commentIdx", item.getIndex()));
                item.add(new Label("status"));
                item.add(new Label("comment"));
                item.add(new Label("createdBy", item.getModelObject().getCreatedBy().get()));
                item.add(DateLabel.forDateStyle("created",
                        Model.of(ComponentUtil
                                .getDateFromLocalDate(item.getModelObject().getCreatedDate().get().toLocalDate())),
                        "SS"));
            }
        };
        statusComments.setReuseItems(true);
        statusComments.setOutputMarkupId(true);

        return statusComments;
    }

    /**
     * Use this function to get the block UI message while the form is saved.
     */
    private String getShowBlockUICode() {
        return "blockUI('"
                + new StringResourceModel("autosave_message", AbstractEditStatusEntityPage.this, null).getString()
                + "')";
    }

    /*******************************************************************************
     * Buttons Behavior
     *******************************************************************************/
    private void applyDraftSaveBehavior(final BootstrapSubmitButton button) {
        // disable form validation
        button.setDefaultFormProcessing(false);
    }

    @Override
    protected SaveEditPageButton getSaveEditPageButton() {
        final SaveEditPageButton button = new SaveEditPageButton("save",
                new StringResourceModel("saveButton", this, null)) {

            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                editForm.visitChildren(GenericBootstrapFormComponent.class,
                        new AllowNullForCertainInvalidFieldsVisitor());
                setStatusAppendComment(DBConstants.Status.DRAFT);
                super.onSubmit(target);
            }
        };

        return button;
    }

    private SaveEditPageButton getSaveSubmitPageButton() {
        final SaveEditPageButton button = new SaveEditPageButton("saveSubmit",
                new StringResourceModel("saveSubmit", this, null)) {
            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setStatusAppendComment(DBConstants.Status.SUBMITTED);
                super.onSubmit(target);
            }
        };

        button.setIconType(FontAwesomeIconType.send);
        return button;
    }

    private SaveEditPageButton getSubmitAndNextPageButton() {
        final SaveEditPageButton button = new SaveEditPageButton("submitAndNext",
                new StringResourceModel("submitAndNext", this, null)) {
            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setStatusAppendComment(DBConstants.Status.SUBMITTED);
                super.onSubmit(target);
            }

            @Override
            protected Class<? extends Page> getResponsePage() {
                return pageAfterSubmitAndNext();
            }

            @Override
            protected PageParameters getParameterPage() {
                return afterSubmitNextParameters;
            }
        };

        button.setIconType(FontAwesomeIconType.tasks);
        return button;
    }

    /**
     * Override this function in order to redirect the user to the next page after clicking on submitAndNext button.
     */
    protected Class<? extends Page> pageAfterSubmitAndNext() {
        return getPage().getClass();
    }

    /**
     * Override this function in order to redirect the user to the next page with parameters
     * after clicking on submitAndNext button.
     */
    protected PageParameters parametersAfterSubmitAndNext() {
        return getPageParameters();
    }

    private SaveEditPageButton getSaveDraftAndContinueButton() {
        final SaveEditPageButton button = new SaveEditPageButton("saveContinue",
                new StringResourceModel("saveContinue", this, null)) {

            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                editForm.visitChildren(GenericBootstrapFormComponent.class,
                        new AllowNullForCertainInvalidFieldsVisitor());
                setStatusAppendComment(DBConstants.Status.DRAFT);
                super.onSubmit(target);
            }

            @Override
            protected Class<? extends Page> getResponsePage() {
                return getPage().getClass();
            }

            @Override
            protected PageParameters getParameterPage() {
                return getPageParameters();
            }

            @Override
            protected boolean checkInBeforeSave() {
                return false;
            }
        };

        button.setIconType(FontAwesomeIconType.tasks);
        return button;
    }

    private SaveEditPageButton getSaveApprovePageButton() {
        final SaveEditPageButton saveEditPageButton = new SaveEditPageButton("approve",
                new StringResourceModel("approve", this, null)) {

            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                onBeforeSaveApprove(target);
                setStatusAppendComment(DBConstants.Status.APPROVED);
                super.onSubmit(target);
                onApproved();
            }
        };
        saveEditPageButton.setIconType(FontAwesomeIconType.thumbs_up);
        return saveEditPageButton;
    }

    protected void onBeforeSaveApprove(AjaxRequestTarget target) {

    }

    protected void onApproved() {
    }

    protected SaveEditPageButton getRevertToDraftPageButton() {
        final SaveEditPageButton saveEditPageButton = new SaveEditPageButton("revertToDraft",
                new StringResourceModel("revertToDraft", this, null)) {
            @Override
            protected String getOnClickScript() {
                return WebConstants.DISABLE_FORM_LEAVING_JS;
            }

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setStatusAppendComment(DBConstants.Status.DRAFT);
                super.onSubmit(target);
                target.add(editForm);
                onAfterRevertToDraft(target);
            }
        };
        saveEditPageButton.setIconType(FontAwesomeIconType.thumbs_down);
        return saveEditPageButton;
    }

    protected void onAfterRevertToDraft(AjaxRequestTarget target) {

    }

    protected void setStatusAppendComment(final String status) {
        final T saveable = editForm.getModelObject();

        // do not save an empty comment if previous status is same as current status and comment box is empty
        if (status.equals(saveable.getStatus()) && ObjectUtils.isEmpty(saveable.getNewStatusComment())) {
            saveable.setStatus(status);
            return;
        }
        saveable.setStatus(status);

        final StatusChangedComment comment = new StatusChangedComment();
        comment.setStatus(status);
        comment.setComment(editForm.getModelObject().getNewStatusComment());
        saveable.getStatusComments().add(comment);
    }

    protected void setButtonsPermissions() {
        addSaveButtonsPermissions(saveButton);
        addSaveButtonsPermissions(saveDraftContinueButton);
        addSaveButtonsPermissions(submitAndNext);
        addSaveButtonsPermissions(saveSubmitButton);
        addApproveButtonPermissions(saveApproveButton);
        addSaveRevertButtonPermissions(revertToDraftPageButton);
        addDeleteButtonPermissions(deleteButton);
        addSaveNextButtonPermissions(submitAndNext);
        addTerminateButtonPermissions(saveTerminateButton);

        // no need to display the buttons on print view so we overwrite the above permissions
        if (ComponentUtil.isPrintMode()) {
            saveDraftContinueButton.setVisibilityAllowed(false);
            submitAndNext.setVisibilityAllowed(false);
            saveSubmitButton.setVisibilityAllowed(false);
            saveApproveButton.setVisibilityAllowed(false);
            revertToDraftPageButton.setVisibilityAllowed(false);
            saveTerminateButton.setVisibilityAllowed(false);
        }
    }

    protected void addSaveNextButtonPermissions(Component button) {
        addSaveButtonsPermissions(button);
    }

    protected void addTerminateButtonPermissions(Component button) {
        addDefaultAllButtonsPermissions(button);
        if (editForm.getModelObject().isNew()) {
            button.setVisibilityAllowed(false);
        }
    }

    protected void addDeleteButtonPermissions(Component button) {
        addDefaultAllButtonsPermissions(button);
        MetaDataRoleAuthorizationStrategy.authorize(button, Component.RENDER, SecurityConstants.Roles.ROLE_ADMIN);
        button.setVisibilityAllowed(entityId != null && !isTerminated() && !isViewMode());
    }

    protected void addSaveRevertButtonPermissions(Component button) {
        addDefaultAllButtonsPermissions(button);

        button.setVisibilityAllowed(button.isVisibilityAllowed()
                && !DBConstants.Status.DRAFT.equals(editForm.getModelObject().getStatus()));

        //admins can revert anything, including terminated, but only on the terminated form, not elsewhere!
        if (FormSecurityUtil.isCurrentUserAdmin()
                && ((!isTerminated() && DBConstants.Status.APPROVED.equals(editForm.getModelObject().getStatus()))
                || DBConstants.Status.TERMINATED.equals(editForm.getModelObject().getStatus()))) {
            button.setVisibilityAllowed(true);
        }
    }

    protected void addApproveButtonPermissions(Component button) {
        addDefaultAllButtonsPermissions(button);
        button.setVisibilityAllowed(button.isVisibilityAllowed()
                && DBConstants.Status.SUBMITTED.equals(editForm.getModelObject().getStatus()));
    }

    protected void addSaveButtonsPermissions(Component button) {
        addDefaultAllButtonsPermissions(button);
        button.setVisibilityAllowed(button.isVisibilityAllowed()
                && !isDisableEditingEvent());
    }

    protected void addDefaultAllButtonsPermissions(final Component button) {
        MetaDataRoleAuthorizationStrategy.authorize(button, Component.RENDER, SecurityConstants.Roles.ROLE_ADMIN);
        button.setVisibilityAllowed(!isTerminated() && !isViewMode());
    }


    @Override
    public boolean isTerminated() {
        return DBConstants.Status.TERMINATED.equals(editForm.getModelObject().getStatus());
    }


    private void scrollToPreviousPosition(final IHeaderResponse response) {
        response.render(OnDomReadyHeaderItem.forScript(String.format(
                "var vPosition= +%s, mHeight = +%s, cmHeight=$(document).height();"
                        + "if(mHeight!=0) $(window).scrollTop(vPosition*cmHeight/mHeight)",
                getPageParameters().get(WebConstants.V_POSITION).toDouble(0),
                getPageParameters().get(WebConstants.MAX_HEIGHT).toDouble(0)
        )));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        scrollToPreviousPosition(response);
    }

    /**
     * Allow null saving for draft entities even if the field is required.
     * Bypass validation for this purpose.
     *
     * @author mpostelnicu
     */
    public class AllowNullForCertainInvalidFieldsVisitor
            implements IVisitor<GenericBootstrapFormComponent<?, ?>, Void> {
        @Override
        public void component(final GenericBootstrapFormComponent<?, ?> object, final IVisit<Void> visit) {
            // we found the GenericBootstrapFormComponent, stop doing useless
            // things like traversing inside the GenericBootstrapFormComponent itself
            visit.dontGoDeeper();

            // do not process disabled fields
            if (!object.isEnabledInHierarchy()) {
                return;
            }
            object.getField().processInput();

            // clear errors added during validate
            object.getField().getFeedbackMessages().clear();

            // still, if the field is invalid, its input is null, and field is
            // of a certain type, we turn
            // the input into a null. This helps us to save empty REQUIRED
            // fields when saving as draft
            if (!object.getField().isValid() && Strings.isEmpty(object.getField().getInput())) {
                // for text/select fields we just make the object model null
                if (object.getField() instanceof TextField<?> || object.getField() instanceof TextArea<?>
                        || object.getField() instanceof Select2Choice<?>) {
                    object.getField().getModel().setObject(null);
                }

            }
        }
    }
}
