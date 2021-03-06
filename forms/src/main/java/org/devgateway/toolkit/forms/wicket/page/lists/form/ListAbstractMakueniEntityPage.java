package org.devgateway.toolkit.forms.wicket.page.lists.form;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.devgateway.toolkit.forms.wicket.components.table.SimpleDateProperyColumn;
import org.devgateway.toolkit.forms.wicket.page.lists.AbstractListStatusEntityPage;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.SingleFileMetadatable;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.category.FiscalYearService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author idobre
 * @since 2019-04-02
 */
public abstract class ListAbstractMakueniEntityPage<T extends AbstractMakueniEntity>
        extends AbstractListStatusEntityPage<T> {
    @SpringBean
    private DepartmentService departmentService;

    @SpringBean
    private FiscalYearService fiscalYearService;

    protected final List<Department> departments;

    protected final List<FiscalYear> fiscalYears;

    public ListAbstractMakueniEntityPage(final PageParameters parameters) {
        super(parameters);

        filterGoReset = true;
        hasNewPage = false;
        this.departments = departmentService.findAll();
        this.fiscalYears = fiscalYearService.findAll();
    }

    public class DownloadPanel extends Panel {
        public DownloadPanel(final String id, final IModel<FileMetadata> model) {
            super(id, model);

            Link<FileMetadata> downloadLink = new Link<FileMetadata>("downloadLink", model) {
                @Override
                public void onClick() {
                    final AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
                        @Override
                        public void write(final OutputStream output) throws IOException {
                            output.write(getModelObject().getContent().getBytes());
                        }

                        @Override
                        public String getContentType() {
                            return getModelObject().getContentType();
                        }
                    };

                    ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream,
                            getModelObject().getName());
                    handler.setContentDisposition(ContentDisposition.ATTACHMENT);
                    getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                }
            };

            downloadLink.add(new Label("downloadText", model.getObject().getName()));
            downloadLink.add(new TooltipBehavior(
                    new StringResourceModel("downloadUploadedFileTooltip", ListAbstractMakueniEntityPage.this, null)));
            add(downloadLink);
        }
    }

    protected void addLastModifiedDateColumn() {
        addFmColumn("lastModifiedDate", new SimpleDateProperyColumn<>(new Model<>((
                new StringResourceModel("lastModifiedDate",
                        ListAbstractMakueniEntityPage.this)).getString()),
                "lastModifiedDate", "lastModifiedDate",
                t -> t.getLastModifiedDate().orElse(null)));
    }

    protected void addFileDownloadColumn() {
        Component trn = this;
        addFmColumn("downloadFile", new AbstractColumn<T, String>(
                new StringResourceModel("downloadFile", trn)) {
            @Override
            public void populateItem(final Item<ICellPopulator<T>> cellItem, final String componentId,
                                     final IModel<T> model) {
                final FileMetadata file = model.getObject().getFormDoc();
                if (file != null) {
                    cellItem.add(new DownloadPanel(componentId, model.map(SingleFileMetadatable::getFormDoc)));
                } else {
                    cellItem.add(new Label(componentId));
                }
            }
        });

    }

    protected void autoPageTitle() {
        addOrReplace(new Label("pageTitle",
                StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                        this.getClass().getSimpleName().replaceAll("List", "").replaceAll("Page", "")), ' ')
                        + " List"));
    }


    @Override
    protected void onInitialize() {
        // just replace the page title with the name of the class
        // instead of having .properties files only for the page title
        autoPageTitle();

        super.onInitialize();
    }
}
