package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.service.PermissionEntityRenderableService;
import org.devgateway.toolkit.forms.service.SessionMetadataService;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.events.EditingDisabledEvent;
import org.devgateway.toolkit.forms.wicket.page.edit.AbstractEditPage;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.forms.wicket.page.overview.department.DepartmentOverviewPage;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.service.form.CabinetPaperService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author gmutuhu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditCabinetPaperPage extends AbstractEditPage<CabinetPaper> implements ProcurementRoleAssignable {

    @SpringBean
    protected CabinetPaperService cabinetPaperService;

    @SpringBean
    protected SessionMetadataService sessionMetadataService;

    @SpringBean
    private PermissionEntityRenderableService permissionEntityRenderableService;

    public EditCabinetPaperPage(final PageParameters parameters) {
        super(parameters);

        this.jpaService = cabinetPaperService;
        this.listPageClass = DepartmentOverviewPage.class;

    }

    @Override
    protected void onInitialize() {
        editForm.attachFm("cabinetPaperForm");

        // check if this is a new object and redirect user to dashboard page if we don't have all the needed info
        if (entityId == null && sessionMetadataService.getSessionPP() == null) {
            logger.warn("Something wrong happened since we are trying to add a new CabinetPaper Entity "
                    + "without having a ProcurementPlan!");
            setResponsePage(StatusOverviewPage.class);
        }

        super.onInitialize();
        if (permissionEntityRenderableService.getAllowedAccess(this, editForm.getModelObject()) == null) {
            setResponsePage(listPageClass);
        }

        final TextFieldBootstrapFormComponent<String> name = ComponentUtil.addTextField(editForm, "name");
        name.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        name.getField().add(uniqueName());

        final GenericSleepFormComponent number = new GenericSleepFormComponent<>("number");
        editForm.add(number);
        if (entityId == null) {
            number.setVisibilityAllowed(false);
        } else {
            number.setVisibilityAllowed(true);
        }

        final FileInputBootstrapFormComponent doc = new FileInputBootstrapFormComponent("formDocs");
        doc.maxFiles(1);
        editForm.add(doc);
    }

    @Override
    protected CabinetPaper newInstance() {
        final CabinetPaper cabinetPaper = super.newInstance();
        cabinetPaper.setProcurementPlan(sessionMetadataService.getSessionPP());

        return cabinetPaper;
    }

    private IValidator<String> uniqueName() {
        final StringValue id = getPageParameters().get(WebConstants.PARAM_ID);
        return new UniqueNameValidator(id.toLong(-1));
    }


    public class UniqueNameValidator implements IValidator<String> {
        private final Long id;

        public UniqueNameValidator(final Long id) {
            this.id = id;
        }

        @Override
        public void validate(final IValidatable<String> validatable) {
            final String name = validatable.getValue();
            final ProcurementPlan procurementPlan = editForm.getModelObject().getProcurementPlan();

            if (procurementPlan != null && name != null) {
                if (cabinetPaperService.countByProcurementPlanAndNameAndIdNot(procurementPlan, name, id) > 0) {
                    final ValidationError error = new ValidationError(getString("uniqueCabinetPaperName"));
                    validatable.error(error);
                }
            }
        }
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        if (isViewMode()) {
            send(getPage(), Broadcast.BREADTH, new EditingDisabledEvent());
        }

        saveButton.setVisibilityAllowed(!isViewMode());
        deleteButton.setVisibilityAllowed(!isViewMode());
        // no need to display the buttons on print view so we overwrite the above permissions
        if (ComponentUtil.isPrintMode()) {
            saveButton.setVisibilityAllowed(false);
            deleteButton.setVisibilityAllowed(false);
        }
    }

    @Override
    protected void afterSaveEntity(final CabinetPaper saveable) {
        super.afterSaveEntity(saveable);

        // autogenerate the number
        if (saveable.getNumber() == null) {
            saveable.setNumber(saveable.getProcurementPlan().getDepartment().getCode()
                    + "/" + saveable.getId());

            jpaService.saveAndFlush(saveable);
        }
    }

    private boolean isViewMode() {
        return SecurityConstants.Action.VIEW.equals(
                permissionEntityRenderableService.getAllowedAccess(this, editForm.getModelObject()));
    }
}
