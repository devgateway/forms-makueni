package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.validators.BigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.behaviors.CountyAjaxFormComponentUpdatingBehavior;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2MultiChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.service.category.SubcountyService;
import org.devgateway.toolkit.persistence.service.category.WardService;
import org.devgateway.toolkit.persistence.service.form.CabinetPaperService;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author idobre
 * @since 2019-04-02
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_PROCUREMENT_USER)
@MountPath
public class EditProjectPage extends EditAbstractMakueniEntityPage<Project>
        implements ProcurementRoleAssignable {
    @SpringBean
    protected ProjectService projectService;

    @SpringBean
    protected ProcurementPlanService procurementPlanService;

    @SpringBean
    private CabinetPaperService cabinetPaperService;

    @SpringBean
    private SubcountyService subcountyService;

    @SpringBean
    private WardService wardService;

    protected Select2MultiChoiceBootstrapFormComponent<Subcounty> subcounties;

    protected Select2MultiChoiceBootstrapFormComponent<Ward> wards;

    public EditProjectPage() {
        this(new PageParameters());
    }

    public EditProjectPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = projectService;

    }

    @Override
    protected void checkInitParameters() {
        // check if this is a new object and redirect user to dashboard page if we don't have all the needed info
        if (entityId == null && sessionMetadataService.getSessionPP() == null) {
            logger.warn("Something wrong happened since we are trying to add a new Project Entity "
                    + "without having a ProcurementPlan!");
            setResponsePage(StatusOverviewPage.class);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        submitAndNext.setVisibilityAllowed(false);

        editForm.add(new GenericSleepFormComponent<>("procurementPlan.department"));
        editForm.add(new GenericSleepFormComponent<>("procurementPlan.fiscalYear"));

        final TextFieldBootstrapFormComponent<String> projectTitle =
                ComponentUtil.addTextField(editForm, "projectTitle");
        projectTitle.required();
        projectTitle.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        projectTitle.getField().add(uniqueTitle());

        // filtered CabinetPapers based on form Procurement Plan
        final List<CabinetPaper> cabinetPapers = cabinetPaperService
                .findByProcurementPlan(editForm.getModelObject().getProcurementPlan());
        final Select2ChoiceBootstrapFormComponent cabinetPaper = new Select2ChoiceBootstrapFormComponent<>(
                "cabinetPaper", new GenericChoiceProvider<>(cabinetPapers));
        cabinetPaper.required();
        editForm.add(cabinetPaper);

        ComponentUtil.addBigDecimalField(editForm, "amountBudgeted")
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());
        ComponentUtil.addBigDecimalField(editForm, "amountRequested")
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new BigDecimalValidator());

        final IndicatingAjaxFallbackLink allSubcounties = new IndicatingAjaxFallbackLink<Void>("allSubcounties") {
            @Override
            public void onClick(final Optional<AjaxRequestTarget> target) {
                editForm.getModelObject().setSubcounties(new ArrayList<>(subcountyService.findAll()));
                editForm.getModelObject().setWards(new ArrayList<>());

                wards.provider(new GenericChoiceProvider<>(new ArrayList<>(wardService.findAll())));

                if (target.isPresent()) {
                    target.get().add(subcounties);
                    target.get().add(wards);
                }
            }

            @Override
            public void onEvent(final IEvent<?> event) {
                ComponentUtil.enableDisableEvent(this, event);
            }
        };
        editForm.add(allSubcounties);

        final IndicatingAjaxFallbackLink allWards = new IndicatingAjaxFallbackLink<Void>("allWards") {
            @Override
            public void onClick(final Optional<AjaxRequestTarget> target) {
                final Collection<Subcounty> subcountyList = subcounties.getModelObject();
                final List<Ward> wardList;
                if (subcountyList.isEmpty()) {
                    wardList = new ArrayList<>(wardService.findAll());
                } else {
                    wardList = wardService.findAll().stream()
                            .filter(ward -> subcountyList.contains(ward.getSubcounty()))
                            .collect(Collectors.toList());
                }
                editForm.getModelObject().setWards(wardList);

                if (target.isPresent()) {
                    target.get().add(wards);
                }
            }

            @Override
            public void onEvent(final IEvent<?> event) {
                ComponentUtil.enableDisableEvent(this, event);
            }
        };
        editForm.add(allWards);

        wards = ComponentUtil.addSelect2MultiChoiceField(editForm, "wards", wardService);

        subcounties = ComponentUtil.addSelect2MultiChoiceField(editForm, "subcounties", subcountyService);
        subcounties.getField().add(new CountyAjaxFormComponentUpdatingBehavior(subcounties, wards,
                LoadableDetachableModel.of(() -> wardService), editForm.getModel(), "change"
        ));

        ComponentUtil.addDateField(editForm, "approvedDate").required();

        saveTerminateButton.setVisibilityAllowed(false);
    }

    @Override
    protected Project newInstance() {
        final Project project = super.newInstance();
        project.setProcurementPlan(sessionMetadataService.getSessionPP());

        return project;
    }

    @Override
    protected void afterSaveEntity(final Project saveable) {
        super.afterSaveEntity(saveable);

        // add current Purchase Requisition in session
        sessionMetadataService.setSessionProject(editForm.getModelObject());
        sessionMetadataService.setSessionTenderProcess(null);
    }

    @Override
    protected void beforeSaveEntity(final Project project) {
        super.beforeSaveEntity(project);

        final ProcurementPlan procurementPlan = project.getProcurementPlan();
        procurementPlan.addProject(project);
        procurementPlanService.save(procurementPlan);
    }

    @Override
    protected void beforeDeleteEntity(final Project project) {
        super.beforeDeleteEntity(project);

        final ProcurementPlan procurementPlan = project.getProcurementPlan();
        procurementPlan.removeProject(project);
        procurementPlanService.save(procurementPlan);
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
                if (projectService.countByProcurementPlanAndProjectTitleAndIdNot(procurementPlan, titleValue, id) > 0) {
                    final ValidationError error = new ValidationError(getString("uniqueTitle"));
                    validatable.error(error);
                }
            }
        }
    }

}
