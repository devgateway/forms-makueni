package org.devgateway.toolkit.forms.wicket.page.overview;

import de.agilecoders.wicket.core.util.Attributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.service.SessionMetadataService;
import org.devgateway.toolkit.forms.wicket.page.overview.department.DepartmentOverviewPage;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;

import java.util.List;


public class SideBar extends Panel {
    @SpringBean
    private DepartmentService departmentService;

    @SpringBean
    private ProjectService projectService;


    @SpringBean
    private TenderProcessService tenderProcessService;

    @SpringBean
    private SessionMetadataService sessionMetadataService;

    private final Department department;

    private Label projectCount;
    private Label tenderProcessCount;

    public SideBar(final String id) {
        super(id);

        this.department = sessionMetadataService.getSessionDepartment();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Link<Void> statusLink = new Link<Void>("statusLink") {
            @Override
            public void onClick() {
                // clear all session data before going to the dashboard
                SessionMetadataService.clearSessionData();
                setResponsePage(StatusOverviewPage.class);
            }

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if (department != null) {
                    Attributes.removeClass(tag, "selected");
                }
            }
        };
        add(statusLink);

        projectCount = new Label("projectCount", calculateProjectCount());
        projectCount.setOutputMarkupId(true);
        add(projectCount);

        tenderProcessCount = new Label("tenderProcessCount", calculateTenderProcessCount());
        tenderProcessCount.setOutputMarkupId(true);
        add(tenderProcessCount);

        final List<Department> departments = departmentService.findAll();
        add(new PropertyListView<Department>("departmentOverviewLink", departments) {
            @Override
            protected void populateItem(final ListItem<Department> item) {
                final Link<Void> link = new Link<Void>("link") {
                    @Override
                    public void onClick() {
                        sessionMetadataService.setSessionDepartment(item.getModelObject());
                        setResponsePage(DepartmentOverviewPage.class);
                    }

                    @Override
                    protected void onComponentTag(final ComponentTag tag) {
                        super.onComponentTag(tag);

                        if (item.getModelObject().equals(department)) {
                            Attributes.addClass(tag, "selected");
                        }
                    }
                };
                link.add(new Label("label", item.getModelObject().getLabel())
                        .setRenderBodyOnly(true));
                item.add(link);
            }
        });
    }

    private Long calculateProjectCount() {
        final FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();
        if (department == null) {
            return projectService.countByFiscalYear(fiscalYear);
        }

        return projectService.countByDepartmentAndFiscalYear(department, fiscalYear);
    }

    private Long calculateTenderProcessCount() {
        final FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();
        if (department == null) {
            return tenderProcessService.countByFiscalYear(fiscalYear);
        }

        return tenderProcessService.countByDepartmentAndFiscalYear(department, fiscalYear);
    }

    public Label getProjectCount() {
        return projectCount;
    }

    public Label getTenderProcessCount() {
        return tenderProcessCount;
    }
}
