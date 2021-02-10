/**
 * Copyright (c) 2015 Development Gateway, Inc and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 * <p>
 * Contributors:
 * Development Gateway - initial API and implementation
 */
/**
 *
 */
package org.devgateway.toolkit.forms.wicket.page.overview.department;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.ladda.LaddaAjaxButton;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.fm.DgFmBehavior;
import org.devgateway.toolkit.forms.service.PermissionEntityRenderableService;
import org.devgateway.toolkit.forms.wicket.components.PlaceholderBehavior;
import org.devgateway.toolkit.forms.wicket.components.form.AJAXDownload;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.components.util.EditViewResourceModel;
import org.devgateway.toolkit.forms.wicket.page.edit.AbstractEditPage;
import org.devgateway.toolkit.forms.wicket.page.edit.EditFiscalYearBudgetPage;
import org.devgateway.toolkit.forms.wicket.page.edit.ProcurementPlanInputSelectPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditCabinetPaperPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditProcurementPlanPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditProjectPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditPurchaseRequisitionGroupPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListCabinetPaperPage;
import org.devgateway.toolkit.forms.wicket.page.overview.AbstractListViewStatus;
import org.devgateway.toolkit.forms.wicket.page.overview.DataEntryBasePage;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.FiscalYearBudget;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess_;
import org.devgateway.toolkit.persistence.dao.form.Tender_;
import org.devgateway.toolkit.persistence.service.excel.DataExportService;
import org.devgateway.toolkit.persistence.service.filterstate.form.ProjectFilterState;
import org.devgateway.toolkit.persistence.service.form.FiscalYearBudgetService;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.Constants;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.annotation.mount.MountPath;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author gmutuhu
 *
 */
@MountPath("/departmentOverview")
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
public class DepartmentOverviewPage extends DataEntryBasePage {

    private final IModel<ProcurementPlan> procurementPlanModel;
    private final LoadableDetachableModel<FiscalYearBudget> fiscalYearBudgetModel;

    private String searchBox = "";

    private ListViewProjectsOverview listViewProjectsOverview;
    private ListViewTenderProcessOverview listViewTenderProcessOverview;
    private TransparentWebMarkupContainer listTenderProcessWrapper;

    @SpringBean
    private ProjectService projectService;

    @SpringBean
    private TenderProcessService tenderProcessService;

    @SpringBean
    private ProcurementPlanService procurementPlanService;

    @SpringBean
    private FiscalYearBudgetService fiscalYearBudgetService;

    @SpringBean
    private PermissionEntityRenderableService permissionEntityRenderableService;

    private Label newProcurementPlanLabel;

    protected final IModel<FiscalYear> fiscalYearModel;

    protected IModel<List<FiscalYear>> fiscalYearsModel;

    @SpringBean
    private DataExportService dataExportService;
    private WebMarkupContainer noData;


    private Department getDepartment() {
        return sessionMetadataService.getSessionDepartment();
    }

    private FiscalYear getFiscalYear() {
        return sessionMetadataService.getSessionFiscalYear();
    }

    private ProcurementPlan getProcurementPlan() {
        return procurementPlanModel.getObject();
    }

    public BootstrapAjaxLink<Void> createNewTenderProcessButton() {
        final BootstrapAjaxLink<Void> addTenderProcess = new BootstrapAjaxLink<Void>("addTenderProcess",
                Buttons.Type.Success) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                TenderProcess tenderProcess = tenderProcessService.newInstance();
                tenderProcess.setProcurementPlan(sessionMetadataService.getSessionPP());
                TenderProcess savedTenderProcess = tenderProcessService.save(tenderProcess);
                sessionMetadataService.setSessionTenderProcess(savedTenderProcess);
                sessionMetadataService.setSessionProject(null);
                setResponsePage(DepartmentOverviewPage.class);
            }
        };
        addTenderProcess.setLabel(
                new StringResourceModel("addTenderProcess", DepartmentOverviewPage.this, null));
        addTenderProcess.setEnabled(getProcurementPlan() != null);
        addTenderProcess.setVisibilityAllowed(canAccessAddNewButtons(EditProjectPage.class));
        return addTenderProcess;
    }

    public class TenderProcessOverviewPanel extends GenericPanel<Void> {

        public TenderProcessOverviewPanel(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(createYearDropdown());

            final Form<?> excelForm = new ExcelDownloadForm("excelForm");
            add(excelForm);

            add(createTenderSearchBox());

            listViewTenderProcessOverview = createTenderProcessOverview();
            listTenderProcessWrapper =
                    new TransparentWebMarkupContainer("listTenderProcessWrapper") {
                        @Override
                        public boolean isVisible() {
                            return !listViewTenderProcessOverview.getModelObject().isEmpty();
                        }
                    };
            listTenderProcessWrapper.setOutputMarkupId(true);
            listTenderProcessWrapper.setOutputMarkupPlaceholderTag(true);


            add(listTenderProcessWrapper);


            listTenderProcessWrapper.add(listViewTenderProcessOverview);

            add(createNewTenderProcessButton());

            add(createNoData(listViewTenderProcessOverview));
            add(new DgFmBehavior("deptOverview.tenderProcess"));
        }
    }

    public ListViewTenderProcessOverview createTenderProcessOverview() {
       return new ListViewTenderProcessOverview("tenderProcessOverview", true,
                new ListModel<>(fetchTenderProcessData()), sessionMetadataService.getSessionTenderProcess());
    }

    public class ProjectOverviewPanel extends GenericPanel<Void> {

        public ProjectOverviewPanel(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(createProjectButton());
            add(createYearDropdown());

            final Form<?> excelForm = new ExcelDownloadForm("excelForm");
            add(excelForm);

            add(createProjectSearchBox());

            ListViewProjectsOverview projectList = createProjectList();
            add(projectList);

            add(createNoData(projectList));
            add(new DgFmBehavior("deptOverview.project"));
        }
    }

    public DepartmentOverviewPage(final PageParameters parameters) {
        super(parameters);

        fiscalYearsModel = new LoadableDetachableModel<List<FiscalYear>>() {
            @Override
            protected List<FiscalYear> load() {
                return fiscalYearService.findAll();
            }
        };

        fiscalYearModel = new LoadableDetachableModel<FiscalYear>() {
            @Override
            protected FiscalYear load() {
                FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();
                if (fiscalYear != null) {
                    return fiscalYear;
                }
                fiscalYear = fiscalYearsModel.getObject().isEmpty() ? null : fiscalYearsModel.getObject().get(0);
                sessionMetadataService.setSessionFiscalYear(fiscalYear);
                return fiscalYear;
            }
        };


        fiscalYearBudgetModel = new LoadableDetachableModel<FiscalYearBudget>() {
            @Override
            protected FiscalYearBudget load() {
                FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();
                Department department = sessionMetadataService.getSessionDepartment();
                if (fiscalYear != null && department != null) {
                    return fiscalYearBudgetService.findByDepartmentAndFiscalYear(department, fiscalYear);
                }
                return null;
            }
        };

        // redirect user to status dashboard page if we don't have all the needed info
        if (getDepartment() == null) {
            logger.warn("User landed on DepartmentOverviewPage page without having any department in Session");
            setResponsePage(StatusOverviewPage.class);
        }

        procurementPlanModel = new LoadableDetachableModel<ProcurementPlan>() {
            @Override
            protected ProcurementPlan load() {
                return procurementPlanService.findByDepartmentAndFiscalYear(getDepartment(), getFiscalYear());
            }
        };
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new Label("departmentLabel", getDepartment() == null ? "" : getDepartment().getLabel()));

        WebMarkupContainer cabinetMinisterialPaperRow = new WebMarkupContainer("cabinetMinisterialPaperRow");
        cabinetMinisterialPaperRow.add(new DgFmBehavior("deptOverview.cabinetPaper"));
        add(cabinetMinisterialPaperRow);

        cabinetMinisterialPaperRow.add(new Label("cabinetMinisterialPaper",
                new StringResourceModel("cabinetMinisterialPaper", this)
                        .setParameters(new PropertyModel<>(fiscalYearModel, "label"))));

        cabinetMinisterialPaperRow.add(createEditCabinetPaperButton());
        cabinetMinisterialPaperRow.add(createListCabinetPaperButton());

        add(new Label("budget",
                new StringResourceModel("budget", this).setParameters(
                        new PropertyModel<>(fiscalYearModel, "label"),
                        fiscalYearBudgetModel
                                .map(FiscalYearBudget::getAmountBudgeted)
                                .map(Objects::toString)
                                .orElse("")))
                .add(new DgFmBehavior("deptOverview.fiscalYearBudget")));

        addNewProcurementPlanButton();
        addEditProcurementPlanButton();
        addFiscalYearBudgetButton();
        addEditFiscalYearBudgetButton();
        addLabelOrInvisibleContainer("procurementPlanLabel", getProcurementPlan());

        add(new ProjectOverviewPanel("projectOverview"));

        add(new TenderProcessOverviewPanel("tenderProcessOverview"));
    }

    /**
     * A wrapper form that is used to fire the excel download action
     */
    public class ExcelDownloadForm extends Form<Void> {
        public ExcelDownloadForm(final String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            final AJAXDownload download = new AJAXDownload() {
                @Override
                protected IRequestHandler getHandler() {
                    return new IRequestHandler() {
                        @Override
                        public void respond(final IRequestCycle requestCycle) {
                            final HttpServletResponse response = (HttpServletResponse) requestCycle
                                    .getResponse().getContainerResponse();

                            try {
                                final byte[] bytes = dataExportService.generateProcurementPlanExcel(
                                        getProcurementPlan().getId());

                                response.setContentType(
                                        Constants.ContentType.XLSX);
                                response.setHeader("Content-Disposition", "attachment; filename=excel-export.xlsx");
                                response.getOutputStream().write(bytes);
                            } catch (IOException e) {
                                logger.error("Download error", e);
                            }

                            RequestCycle.get().scheduleRequestHandlerAfterCurrent(null);
                        }

                        @Override
                        public void detach(final IRequestCycle requestCycle) {
                            // do nothing;
                        }
                    };
                }
            };
            add(download);

            final LaddaAjaxButton excelButton = new LaddaAjaxButton("excelButton", Buttons.Type.Warning) {
                @Override
                protected void onSubmit(final AjaxRequestTarget target) {
                    super.onSubmit(target);

                    // initiate the file download
                    download.initiate(target);
                }
            };
            excelButton.setEnabled(getProcurementPlan() != null);
            add(excelButton);
        }
    }

    private void addLabelOrInvisibleContainer(final String id, final Object o) {
        if (o != null) {
            add(new Label(id, o.toString()));
        } else {
            add(new WebMarkupContainer(id).setVisibilityAllowed(false));
        }
    }

    public boolean canAccessAddNewButtons(Class<? extends AbstractEditPage<?>> clazz) {
        return ComponentUtil.canAccessAddNewButtons(clazz, permissionEntityRenderableService,
                sessionMetadataService
        );
    }

    private void addFiscalYearBudgetButton() {
        final BootstrapAjaxLink<Void> link = new BootstrapAjaxLink<Void>(
                "addFiscalYearBudget",
                Buttons.Type.Success
        ) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                sessionMetadataService.setSessionPP(getProcurementPlan());
                setResponsePage(EditFiscalYearBudgetPage.class);
            }
        };
        link.setEnabled(fiscalYearBudgetModel.getObject() == null);
        link.add(new TooltipBehavior(new StringResourceModel("addFiscalYearBudget.tooltip", this)));
        add(link);
        link.setVisibilityAllowed(canAccessAddNewButtons(EditFiscalYearBudgetPage.class));
    }

    private void addNewProcurementPlanButton() {
        final BootstrapBookmarkablePageLink<Void> newProcurementPlanButton = new BootstrapBookmarkablePageLink<>(
                "newProcurementPlan", ProcurementPlanInputSelectPage.class, Buttons.Type.Success);
        add(newProcurementPlanButton);
        newProcurementPlanButton.setEnabled(getProcurementPlan() == null && getFiscalYear() != null);
        newProcurementPlanButton.setVisibilityAllowed(canAccessAddNewButtons(EditProcurementPlanPage.class));

        newProcurementPlanLabel = new Label("newProcurementPlanLabel",
                new StringResourceModel("newProcurementPlanLabel", this));
        newProcurementPlanLabel.setVisibilityAllowed(newProcurementPlanButton.isVisibilityAllowed());
        add(newProcurementPlanLabel);
    }

    private void addEditFiscalYearBudgetButton() {
        FiscalYearBudget fiscalYearBudget = fiscalYearBudgetModel.getObject();
        final PageParameters pp = new PageParameters();
        if (fiscalYearBudget != null) {
            pp.set(WebConstants.PARAM_ID, fiscalYearBudget.getId());
        }
        final BootstrapBookmarkablePageLink<Void> button = new BootstrapBookmarkablePageLink<Void>(
                "editFiscalYearBudget", EditFiscalYearBudgetPage.class, pp, Buttons.Type.Info) {
            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if (!canAccessAddNewButtons(EditFiscalYearBudgetPage.class)) {
                    Attributes.removeClass(tag, "btn-edit");
                    Attributes.addClass(tag, "btn-view");
                }
            }
        };
        button.setEnabled(fiscalYearBudget != null);
        button.add(new TooltipBehavior(EditViewResourceModel.of(
                canAccessAddNewButtons(EditFiscalYearBudgetPage.class), "fiscalYearBudget", this)));

        add(button);

    }

    private void addEditProcurementPlanButton() {
        final PageParameters pp = new PageParameters();
        if (getProcurementPlan() != null) {
            pp.set(WebConstants.PARAM_ID, getProcurementPlan().getId());
        }
        final BootstrapBookmarkablePageLink<Void> button = new BootstrapBookmarkablePageLink<Void>(
                "editProcurementPlan", EditProcurementPlanPage.class, pp, Buttons.Type.Info) {
            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if (!canAccessAddNewButtons(EditProcurementPlanPage.class)) {
                    Attributes.removeClass(tag, "btn-edit");
                    Attributes.addClass(tag, "btn-view");
                }
            }
        };
        button.setEnabled(getProcurementPlan() != null);
        button.add(new TooltipBehavior(EditViewResourceModel.of(
                canAccessAddNewButtons(EditProcurementPlanPage.class), "procurementPlan", this)));

        add(button);

        DeptOverviewStatusLabel procurementPlanStatus = new DeptOverviewStatusLabel(
                "procurementPlanStatus", getProcurementPlan() == null ? null : getProcurementPlan().getStatus());
        add(procurementPlanStatus);
    }

    private BootstrapAjaxLink<Void> createEditCabinetPaperButton() {
        final BootstrapAjaxLink<Void> editCabinetPaper = new BootstrapAjaxLink<Void>("editCabinetPaper",
                Buttons.Type.Success) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                sessionMetadataService.setSessionPP(getProcurementPlan());
                setResponsePage(EditCabinetPaperPage.class);
            }
        };
        editCabinetPaper.setEnabled(getProcurementPlan() != null);
        editCabinetPaper.add(new TooltipBehavior(new StringResourceModel("editCabinetPaper.tooltip", this)));
        editCabinetPaper.setVisibilityAllowed(canAccessAddNewButtons(EditCabinetPaperPage.class));
        return editCabinetPaper;
    }

    private BootstrapAjaxLink<Void> createListCabinetPaperButton() {
        final BootstrapAjaxLink<Void> editCabinetPaper = new BootstrapAjaxLink<Void>("listCabinetPaper",
                Buttons.Type.Success) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                sessionMetadataService.setSessionPP(getProcurementPlan());
                setResponsePage(ListCabinetPaperPage.class);
            }

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if (!canAccessAddNewButtons(EditCabinetPaperPage.class)) {
                    Attributes.removeClass(tag, "btn-edit");
                    Attributes.addClass(tag, "btn-view");
                }
            }
        };
        editCabinetPaper.setEnabled(getProcurementPlan() != null);
        editCabinetPaper.add(new TooltipBehavior(new StringResourceModel("listCabinetPaper.tooltip", this)));
        return editCabinetPaper;
    }

    private BootstrapBookmarkablePageLink<Void> createProjectButton() {
        sessionMetadataService.setSessionPP(getProcurementPlan());
        final BootstrapBookmarkablePageLink<Void> addNewProject = new BootstrapBookmarkablePageLink<>(
                "addNewProject", EditProjectPage.class, Buttons.Type.Success);
        addNewProject.setLabel(new StringResourceModel("addNewProject", DepartmentOverviewPage.this, null));
        addNewProject.setEnabled(getProcurementPlan() != null);
        addNewProject.setVisibilityAllowed(canAccessAddNewButtons(EditProjectPage.class));
        return addNewProject;
    }

    private DropDownChoice<FiscalYear> createYearDropdown() {
        final ChoiceRenderer<FiscalYear> choiceRenderer = new ChoiceRenderer<>("label", "id");
        final DropDownChoice<FiscalYear> yearsDropdown = new DropDownChoice<>("years",
                fiscalYearModel, fiscalYearsModel, choiceRenderer);

        yearsDropdown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                sessionMetadataService.setSessionFiscalYear(yearsDropdown.getModelObject());
                setResponsePage(DepartmentOverviewPage.class);
            }
        });
        return yearsDropdown;
    }

    private TextField<String> createProjectSearchBox() {
        TextField<String> searchBoxField = new TextField<>("searchBox", new PropertyModel<>(this, "searchBox"));
        searchBoxField.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                updateProjectDashboard(target);
            }
        });
        searchBoxField.add(new PlaceholderBehavior(new StringResourceModel("project.searchBox.placeholder",
                DepartmentOverviewPage.this)));
        return searchBoxField;
    }


    private TextField<String> createTenderSearchBox() {
        TextField<String> searchBoxField = new TextField<>("searchBox", new PropertyModel<>(this, "searchBox"));
        searchBoxField.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                updateTenderProcessDashboard(target);
            }
        });
        searchBoxField.add(new PlaceholderBehavior(new StringResourceModel("tender.searchBox.placeholder",
                DepartmentOverviewPage.this)));
        return searchBoxField;
    }

    @Transactional(readOnly = true)
    public ListViewProjectsOverview createProjectList() {

        IModel<List<Project>> projectListModel = new LoadableDetachableModel<List<Project>>() {
            @Override
            protected List<Project> load() {
                return fetchProjectData();
            }
        };

        listViewProjectsOverview = new ListViewProjectsOverview("projectsList", projectListModel,
                procurementPlanModel
        );
        add(listViewProjectsOverview);
        return listViewProjectsOverview;
    }



    public WebMarkupContainer createNoData(AbstractListViewStatus<?> lvs) {
        noData = new WebMarkupContainer("noData");
        noData.setOutputMarkupId(true);
        noData.setOutputMarkupPlaceholderTag(true);
        noData.setVisibilityAllowed(lvs.getModelObject().isEmpty());
        return noData;
    }

    private void updateProjectDashboard(final AjaxRequestTarget target) {
        listViewProjectsOverview.setModelObject(fetchProjectData());
        listViewProjectsOverview.removeListItems();

        target.add(listViewProjectsOverview);
    }

    private void updateTenderProcessDashboard(final AjaxRequestTarget target) {
        listViewTenderProcessOverview.setModelObject(fetchTenderProcessData());
        listViewTenderProcessOverview.removeListItems();

        target.add(listViewTenderProcessOverview, listTenderProcessWrapper);
    }

    @Transactional(readOnly = true)
    public List<Project> fetchProjectData() {
        return getProcurementPlan() == null
                ? new ArrayList<>()
                : projectService.findAll(new ProjectFilterState(getProcurementPlan(), searchBox).getSpecification());
    }

    @Transactional(readOnly = true)
    public List<TenderProcess> fetchTenderProcessData() {
        return getProcurementPlan() == null
                ? new ArrayList<>()
                : tenderProcessService.findAll((r, cq, cb) -> cb.and(
                cb.equal(r.get(TenderProcess_.procurementPlan), getProcurementPlan()),
                cb.isNull(r.get(TenderProcess_.project)),
                ObjectUtils.isEmpty(searchBox) ? cb.and() : cb.like(
                        cb.lower(r.join(TenderProcess_.tender).get(Tender_.tenderTitle)),
                        "%" + searchBox.toLowerCase() + "%")
        ));
    }
}
