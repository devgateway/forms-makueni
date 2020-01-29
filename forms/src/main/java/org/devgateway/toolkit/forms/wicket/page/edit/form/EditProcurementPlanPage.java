package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.PlanItemPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author idobre
 * @since 2019-04-02
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_PROCUREMENT_USER)
@MountPath
public class EditProcurementPlanPage extends EditAbstractMakueniEntityPage<ProcurementPlan>
        implements ProcurementRoleAssignable {
    @SpringBean
    protected ProcurementPlanService procurementPlanService;

    public EditProcurementPlanPage(final PageParameters parameters) {
        super(parameters);

        this.jpaService = procurementPlanService;

        final Department department = sessionMetadataService.getSessionDepartment();
        final FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();

        // check if this is a new object and redirect user to dashboard page if we don't have all the needed info
        if (entityId == null && (department == null || fiscalYear == null)) {
            logger.warn("Something wrong happened since we are trying to add a new ProcurementPlan Entity "
                    + "without having a department or a fiscalYear!");
            setResponsePage(StatusOverviewPage.class);
        }

        // safeguard that we are not creating multiple ProcurementPlans for the same department and fiscalYear
        if (entityId == null && procurementPlanService.countByDepartmentAndFiscalYear(department, fiscalYear) > 0) {
            logger.warn("We already have a ProcurementPlan for the Department: "
                    + department + " and fiscalYear: " + fiscalYear);
            setResponsePage(StatusOverviewPage.class);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        submitAndNext.setVisibilityAllowed(false);

        editForm.add(new GenericSleepFormComponent<>("department"));
        editForm.add(new GenericSleepFormComponent<>("fiscalYear"));
        editForm.add(new PlanItemPanel("planItems"));

        final FileInputBootstrapFormComponent formDocs =
                new FileInputBootstrapFormComponent("formDocs");
        formDocs.required();
        editForm.add(formDocs);

        ComponentUtil.addDateField(editForm, "approvedDate").required();
        saveTerminateButton.setVisibilityAllowed(false);
    }

    @Override
    protected ProcurementPlan newInstance() {
        final ProcurementPlan procurementPlan = super.newInstance();

        procurementPlan.setDepartment(sessionMetadataService.getSessionDepartment());
        procurementPlan.setFiscalYear(sessionMetadataService.getSessionFiscalYear());

        return procurementPlan;
    }
}
