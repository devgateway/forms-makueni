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
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.TextContentModal;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.ladda.LaddaAjaxButton;
import nl.dries.wicket.hibernate.dozer.DozerModel;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.ValidationError;
import org.devgateway.ocds.web.util.SettingsUtils;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.exceptions.NullJpaServiceException;
import org.devgateway.toolkit.forms.exceptions.NullListPageClassException;
import org.devgateway.toolkit.forms.fm.DgFmComponentSubject;
import org.devgateway.toolkit.forms.util.MarkupCacheService;
import org.devgateway.toolkit.forms.wicket.components.ListViewSectionPanel;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapCancelButton;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapDeleteButton;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapSubmitButton;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.SummernoteBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.BasePage;
import org.devgateway.toolkit.forms.wicket.styles.BlockUiJavaScript;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.ListViewItem;
import org.devgateway.toolkit.persistence.validator.Severity;
import org.devgateway.toolkit.persistence.service.BaseJpaService;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.persistence.service.InvalidObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * @author mpostelnicu Page used to make editing easy, extend to get easy access
 * to one entity for editing
 */
public abstract class AbstractEditPage<T extends GenericPersistable & Serializable> extends BasePage {
    private static final long serialVersionUID = -5928614890244382103L;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractEditPage.class);

    /**
     * Factory method for the new instance of the entity being editing. This
     * will be invoked only when the parameter PARAM_ID is null
     *
     * @return
     */
    protected T newInstance() {
        return jpaService.newInstance();
    }

    /**
     * The repository used to fetch and save the entity, this is initialized in
     * subclasses
     */
    protected BaseJpaService<T> jpaService;

    /**
     * The page that is responsible for listing the entities (used here as a
     * return reference after successful save)
     */
    protected Class<? extends BasePage> listPageClass;

    /**
     * The form used by all subclasses
     */
    protected EditForm editForm;

    protected void afterSaveEntity(final T saveable) {
    }

    /**
     * Invoked immediately after entity is loaded
     *
     * @param entityModel
     */
    protected void afterLoad(final IModel<T> entityModel) {
    }

    /**
     * the entity id, or null if a new entity is requested
     */
    protected Long entityId;

    /**
     * This is a wrapper model that ensures we can easily edit the properties of
     * the entity
     */
    protected CompoundPropertyModel<T> compoundModel;

    /**
     * generic submit button for the form
     */
    protected BootstrapSubmitButton saveButton;

    /**
     * generic delete button for the form
     */
    protected BootstrapDeleteButton deleteButton;

    protected TextContentModal deleteModal;

    protected TextContentModal deleteFailedModal;

    protected TextContentModal errorModal;

    /**
     * Set when the current object cannot be saved or created.
     */
    private boolean hasNonRecoverableError;

    @SpringBean
    private EntityManager entityManager;

    @SpringBean(required = false)
    private MarkupCacheService markupCacheService;

    @SpringBean
    protected SettingsUtils settingsUtils;

    public EditForm getEditForm() {
        return editForm;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    private void flushReportingCaches() {
        if (markupCacheService != null) {
            markupCacheService.flushMarkupCache();
            markupCacheService.clearPentahoReportsCache();
            markupCacheService.clearAllCaches();
        }
    }

    public Class<?> getNewInstanceClass() {
        return newInstance().getClass();
    }

    public GenericBootstrapValidationVisitor getBootstrapValidationVisitor(final AjaxRequestTarget target) {
        return new GenericBootstrapValidationVisitor(target);
    }

    protected TextContentModal createDeleteModal() {
        final TextContentModal modal = new TextContentModal("deleteModal",
                new StringResourceModel("confirmDeleteModal.content", this));
        modal.addCloseButton();

        final LaddaAjaxButton deleteButton = new LaddaAjaxButton("button", Buttons.Type.Danger) {
            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                super.onSubmit(target);

                // close the modal
                deleteModal.appendCloseDialogJavaScript(target);
                onDelete(target);
            }
        };
        deleteButton.setDefaultFormProcessing(false);
        deleteButton.setLabel(new StringResourceModel("confirmDeleteModal.delete", this));
        modal.addButton(deleteButton);

        return modal;
    }

    protected TextContentModal createErrorModal() {
        final TextContentModal modal = new TextContentModal("errorModal", Model.of()) {
            @Override
            protected void onClose(IPartialPageRequestHandler target) {
                super.onClose(target);
                setResponsePage(listPageClass);
            }
        };
        modal.addCloseButton();
        modal.setUseCloseHandler(true);
        return modal;
    }

    protected TextContentModal createDeleteFailedModal() {
        final TextContentModal modal = new TextContentModal("deleteFailedModal",
                new StringResourceModel("deleteFailedModal.content", this));
        final LaddaAjaxButton okButton = new LaddaAjaxButton("button", Buttons.Type.Info) {
            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setResponsePage(listPageClass);
            }
        };
        okButton.setDefaultFormProcessing(false);
        okButton.setLabel(new StringResourceModel("deleteFailedModal.ok", this));
        modal.addButton(okButton);

        modal.add(new AjaxEventBehavior("hidden.bs.modal") {
            @Override
            protected void onEvent(final AjaxRequestTarget target) {
                setResponsePage(listPageClass);
            }
        });

        return modal;
    }

    /**
     * Traverses all fields and refreshes the ones that are not valid, so that
     * we can see the errors
     *
     * @author mpostelnicu
     */
    public static class GenericBootstrapValidationVisitor
            implements IVisitor<GenericBootstrapFormComponent<?, ?>, Void> {

        private AjaxRequestTarget target;

        private GenericBootstrapFormComponent<?, ?> lastInvalidVisitedObject;

        public GenericBootstrapValidationVisitor(final AjaxRequestTarget target) {
            this.target = target;
        }

        @Override
        public void component(final GenericBootstrapFormComponent<?, ?> object, final IVisit<Void> visit) {
            visit.dontGoDeeper();
            if (object instanceof SummernoteBootstrapFormComponent) {
                object.getField().processInput();
            }
            // refresh file component
            if (object instanceof FileInputBootstrapFormComponent && object.getField().isValid()) {
                target.add(object);
            }
            if (!(object instanceof SummernoteBootstrapFormComponent) && object.getField().isValid()) {
                return;
            }
            target.add(object.getBorder());

            // remember last invalid visited object, we used this later to
            // trigger the visibility of its parent container, if it is folded
            lastInvalidVisitedObject = object;

            // there's no point in visiting anything else, we already have a
            // section with error. This hugely improves speed of large forms
            // visit.stop();
        }

        public GenericBootstrapFormComponent<?, ?> getLastInvalidVisitedObject() {
            return lastInvalidVisitedObject;
        }

    }


    public class EditForm extends BootstrapForm<T> implements DgFmComponentSubject {
        private static final long serialVersionUID = -9127043819229346784L;

        @SpringBean
        protected DgFmService fmService;

        @Override
        public DgFmService getFmService() {
            return fmService;
        }

        @Override
        public boolean isEnabled() {
            return isFmEnabled(super::isEnabled);
        }

        @Override
        public boolean isVisible() {
            return isFmVisible(super::isVisible);
        }

        /**
         * wrap the model with a {@link CompoundPropertyModel} to ease editing
         * of fields
         *
         * @param model
         */
        public void setCompoundPropertyModel(final IModel<T> model) {
            compoundModel = new CompoundPropertyModel<T>(model);
            setModel(compoundModel);
        }

        public EditForm(final String id, final IModel<T> model) {
            this(id);
            setCompoundPropertyModel(model);
        }

        public EditForm(final String id) {
            super(id);

            setOutputMarkupId(true);

            saveButton = getSaveEditPageButton();
            add(saveButton);

            deleteButton = getDeleteEditPageButton();
            add(deleteButton);

            deleteModal = createDeleteModal();
            add(deleteModal);

            deleteFailedModal = createDeleteFailedModal();
            add(deleteFailedModal);

            // don't display the delete button if we just create a new entity
            if (entityId == null) {
                deleteButton.setVisibilityAllowed(false);
            }

            add(getCancelButton());
        }
    }

    protected BootstrapCancelButton getCancelButton() {
        return new BootstrapCancelButton("cancel", new StringResourceModel("cancelButton", this, null)) {
            private static final long serialVersionUID = -249084359200507749L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setResponsePage(listPageClass);
            }
        };
    }


    /**
     * Generic functionality for the save page button, this can be extended further by subclasses.
     *
     * @author mpostelnicu
     */
    public class SaveEditPageButton extends BootstrapSubmitButton {
        private static final long serialVersionUID = 9075809391795974349L;

        private boolean redirect = true;

        private boolean redirectToSelf = false;

        public SaveEditPageButton(final String id, final IModel<String> model) {
            super(id, model);
        }


        @Override
        protected void onSubmit(final AjaxRequestTarget target) {
            try {
                // save the object and go back to the list page
                final T saveable = editForm.getModelObject();

                beforeSaveEntity(saveable);

                // saves the entity and flushes the changes
                jpaService.saveAndFlush(saveable);
                getPageParameters().set(WebConstants.PARAM_ID, saveable.getId());

                // clears session and detaches all entities that are currently
                // attached
                entityManager.clear();

                // we flush the mondrian/wicket/reports cache to ensure it gets
                // rebuilt
                flushReportingCaches();

                afterSaveEntity(saveable);

                // only redirect if redirect is true
                if (redirectToSelf) {
                    // we need to close the blockUI if it's opened and enable all the buttons
                    target.appendJavaScript("$.unblockUI();");
                    target.appendJavaScript("$('#" + editForm.getMarkupId() + " button').prop('disabled', false);");
                } else if (redirect) {
                    setResponsePage(getResponsePage(), getParameterPage());
                }

                // redirect is set back to true, which is the default behavior
                redirect = true;
                redirectToSelf = false;
            } catch (InvalidObjectException e) {
                hasNonRecoverableError = e.getViolations().stream().anyMatch(this::nonRecoverableError);
                if (hasNonRecoverableError) {
                    String errors = e.getViolations().stream()
                            .filter(this::nonRecoverableError)
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining("\n"));

                    errorModal.setModelObject(errors);
                    target.add(errorModal);

                    errorModal.show(target);
                } else {
                    e.getViolations().forEach(v -> editForm.error(v.getMessage()));
                    target.add(feedbackPanel);
                }
            }
        }

        private boolean nonRecoverableError(ConstraintViolation<?> violation) {
            return violation.getConstraintDescriptor().getPayload().contains(Severity.NonRecoverable.class);
        }

        /**
         * by default, submit button returns back to listPage
         *
         * @return
         */
        protected Class<? extends Page> getResponsePage() {
            return listPageClass;
        }

        /**
         * no params by default
         *
         * @return
         */
        protected PageParameters getParameterPage() {
            return null;
        }

        @Override
        protected void onError(final AjaxRequestTarget target) {
            // make all errors visible
            final GenericBootstrapValidationVisitor genericBootstrapValidationVisitor =
                    getBootstrapValidationVisitor(target);
            editForm.visitChildren(GenericBootstrapFormComponent.class, genericBootstrapValidationVisitor);

            // only trigger the parent section of the last invalid visited object
            triggerShowParentSections(genericBootstrapValidationVisitor.getLastInvalidVisitedObject(), target);

            final ValidationError error = new ValidationError();
            error.addKey("formHasErrors");
            error(error);

            target.add(feedbackPanel);

            // autoscroll down to the feedback panel
            target.appendJavaScript("$('html, body').animate({scrollTop: $(\".feedbackPanel\").offset().top}, 500);");
        }

        /**
         * @return the redirect
         */
        public boolean isRedirect() {
            return redirect;
        }

        /**
         * @param redirect the redirect to set
         */
        public void setRedirect(final boolean redirect) {
            this.redirect = redirect;
        }

        /**
         * @param redirectToSelf the redirectToSelf to set
         */
        public void setRedirectToSelf(final boolean redirectToSelf) {
            this.redirectToSelf = redirectToSelf;
        }

        /**
         * @return the redirectToSelf
         */
        public boolean isRedirectToSelf() {
            return redirectToSelf;
        }
    }

    protected void beforeSaveEntity(T saveable) {
    }

    protected void beforeDeleteEntity(T deleteable) {
    }

    /**
     * Trigger all parents of type {@link ListViewSectionPanel} to become visible
     * by invoking {@link ListViewSectionPanel#showSection}
     *
     * @param component
     * @param target
     */
    public void triggerShowParentSections(final Component component, final AjaxRequestTarget target) {
        // we exit if the component is null
        if (component == null || component instanceof Form) {
            return;
        }

        if (component instanceof ListItem
                && component.getParent().getParent().getParent() instanceof ListViewSectionPanel) {
            final ListViewSectionPanel<?, ?> sectionPanel = (ListViewSectionPanel<?, ?>)
                    component.getParent().getParent().getParent();

            final TransparentWebMarkupContainer accordion =
                    (TransparentWebMarkupContainer) component.get(ListViewSectionPanel.ID_ACCORDION);
            final Label showDetailsLink =
                    (Label) accordion.get(ListViewSectionPanel.ID_ACCORDION_TOGGLE).get("showDetailsLink");

            final ListViewItem listViewItem = (ListViewItem) ((ListItem) component).getModelObject();
            sectionPanel.showSection(listViewItem, target,
                    accordion.get(ListViewSectionPanel.ID_HIDEABLE_CONTAINER), showDetailsLink);
        }

        // recursive call
        triggerShowParentSections(component.getParent(), target);
    }

    public class DeleteEditPageButton extends BootstrapDeleteButton {
        private static final long serialVersionUID = 865330306716770506L;

        public DeleteEditPageButton(final String id, final IModel<String> model) {
            super(id, model);
            setDefaultFormProcessing(false);
        }

        @Override
        protected void onSubmit(final AjaxRequestTarget target) {
            deleteModal.show(true);
            target.add(deleteModal);
        }

        @Override
        protected void onError(final AjaxRequestTarget target) {
            super.onError(target);
            target.add(feedbackPanel);
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {

        }
    }

    protected void onDelete(final AjaxRequestTarget target) {
        final T deleteable = editForm.getModelObject();
        try {
            beforeDeleteEntity(deleteable);

            jpaService.delete(deleteable);

            // we flush the mondrian/wicket/reports cache to ensure it gets rebuilt
            flushReportingCaches();
        } catch (DataIntegrityViolationException e) {
            deleteFailedModal.show(true);
            target.add(deleteFailedModal);
            return;
        }
        setResponsePage(listPageClass);
    }

    /**
     * Override this to create new save buttons with additional behaviors
     *
     * @return
     */
    protected SaveEditPageButton getSaveEditPageButton() {
        return new SaveEditPageButton("save", new StringResourceModel("saveButton", this, null));
    }

    /**
     * Override this to create new delete buttons if you need additional
     * behavior
     *
     * @return
     */
    protected DeleteEditPageButton getDeleteEditPageButton() {
        return new DeleteEditPageButton("delete", new StringResourceModel("deleteButton", this, null));
    }

    public AbstractEditPage(final PageParameters parameters) {
        super(parameters);

        if (!parameters.get(WebConstants.PARAM_ID).isNull()) {
            entityId = parameters.get(WebConstants.PARAM_ID).toLongObject();
        }

        editForm = new EditForm("editForm") {
            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if (ComponentUtil.isPrintMode()) {
                    Attributes.addClass(tag, "print-view");
                }
            }
        };

        // use this in order to avoid "ServletRequest does not contain multipart content" error
        // this error appears when we have a file upload component that is
        // hidden or not present in the page when the form is created
        editForm.setMultiPart(true);

        add(editForm);

        // this fragment ensures extra buttons are added below the wicket:child section in child
        final Fragment fragment = new Fragment("extraButtons", "noButtons", this);
        editForm.add(fragment);

        errorModal = createErrorModal();
        add(errorModal);
    }

    public boolean hasNonRecoverableError() {
        return hasNonRecoverableError;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        createPrintLink();

        // we cant do anything if we dont have a jpaService here
        if (jpaService == null) {
            throw new NullJpaServiceException();
        }

        // we dont like receiving null list pages
        if (listPageClass == null) {
            throw new NullListPageClassException();
        }

        IModel<T> model = null;

        if (entityId != null) {
            model = new DozerModel<>(jpaService.findById(entityId).orElse(null));
        } else {
            final T instance = newInstance();
            if (instance != null) {
                model = new DozerModel<>(instance);
            }
        }

        if (model != null) {
            editForm.setCompoundPropertyModel(model);
        }

        afterLoad(model);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // block UI
        if (!ComponentUtil.isPrintMode()) {
            response.render(JavaScriptHeaderItem.forReference(BlockUiJavaScript.INSTANCE));
        }
    }
}
