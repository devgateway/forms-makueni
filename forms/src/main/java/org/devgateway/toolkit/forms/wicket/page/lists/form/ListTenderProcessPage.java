package org.devgateway.toolkit.forms.wicket.page.lists.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.devgateway.ocds.persistence.mongo.Release;
import org.devgateway.ocds.persistence.mongo.ReleasePackage;
import org.devgateway.ocds.web.convert.MakueniToOCDSConversionService;
import org.devgateway.ocds.web.rest.controller.OcdsController;
import org.devgateway.toolkit.forms.wicket.components.table.SelectFilteredBootstrapPropertyColumn;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditTenderProcessPage;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.filterstate.JpaFilterState;
import org.devgateway.toolkit.persistence.service.filterstate.form.TenderProcessFilterState;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author idobre
 * @since 2019-04-17
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath("/tenderProcesses")
public class ListTenderProcessPage extends ListAbstractMakueniEntityPage<TenderProcess> {

    @SpringBean
    private MakueniToOCDSConversionService ocdsConversionService;

    @SpringBean
    private TenderProcessService tenderProcessService;

    @SpringBean
    private ObjectMapper objectMapper;

    @SpringBean
    private OcdsController ocdsController;

    public ListTenderProcessPage(final PageParameters parameters) {
        super(parameters);

        this.jpaService = tenderProcessService;
        this.editPageClass = EditTenderProcessPage.class;
    }

    public class OcdsPanel extends Panel {
        public OcdsPanel(final String id, final IModel<Long> model) {
            super(id, model);

            Link<Long> downloadLink = new Link<Long>("ocdsLink", model) {
                @Override
                public void onClick() {
                    final AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
                        @Override
                        public void write(final OutputStream output) throws IOException {
                            Optional<TenderProcess> byId = tenderProcessService.findById(model.getObject());

                            Release release = ocdsConversionService.createAndPersistRelease(byId.get());
                            ReleasePackage releasePackage = ocdsController.createReleasePackage(release);
                            output.write(objectMapper.writeValueAsBytes(releasePackage));
                        }

                        @Override
                        public String getContentType() {
                            return "application/json";
                        }
                    };

                    ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream,
                            "ocds-purchase-requisition-" + model.getObject() + ".json");
                    handler.setContentDisposition(ContentDisposition.ATTACHMENT);
                    getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                }
            };

            downloadLink.add(new Label("ocdsText", "ocds-purchase-requisition-" + model.getObject()));
            downloadLink.add(new TooltipBehavior(
                    new StringResourceModel("downloadOcdsTooltip", ListTenderProcessPage.this, null)));
            add(downloadLink);
        }
    }


    protected void addOcdsDownloadColumn() {
        Component trn = this;
        columns.add(new AbstractColumn<TenderProcess, String>(
                new StringResourceModel("downloadOcds", trn)) {
            @Override
            public void populateItem(final Item<ICellPopulator<TenderProcess>> cellItem, final String componentId,
                                     final IModel<TenderProcess> model) {
                if (DBConstants.Status.EXPORTABLE.contains(model.getObject().getStatus())) {
                    cellItem.add(new ListTenderProcessPage.OcdsPanel(componentId,
                            new Model<>(model.getObject().getId())));
                } else {
                    cellItem.add(new Label(componentId));
                }
            }
        });

    }

    @Override
    protected void onInitialize() {
        columns.add(new SelectFilteredBootstrapPropertyColumn<>(
                new Model<>((new StringResourceModel("department", ListTenderProcessPage.this)).getString()),
                "project.procurementPlan.department", "project.procurementPlan.department",
                new ListModel(departments), dataTable));

        columns.add(new SelectFilteredBootstrapPropertyColumn<>(
                new Model<>((new StringResourceModel("fiscalYear", ListTenderProcessPage.this)).getString()),
                "project.procurementPlan.fiscalYear", "project.procurementPlan.fiscalYear",
                new ListModel(fiscalYears), dataTable));

        columns.add(new PropertyColumn<TenderProcess, String>(
                new Model<>((new StringResourceModel("lastModifiedDate",
                        ListTenderProcessPage.this)).getString()),
                "lastModifiedDate", "lastModifiedDate") {
            @Override
            public void populateItem(final Item<ICellPopulator<TenderProcess>> item,
                                     final String componentId,
                                     final IModel<TenderProcess> rowModel) {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
                final Optional<ZonedDateTime> lastModifiedDate = rowModel.getObject().getLastModifiedDate();

                if (lastModifiedDate.isPresent()) {
                    item.add(new Label(componentId, lastModifiedDate.get().format(formatter)));
                } else {
                    item.add(new Label(componentId, ""));
                }

            }
        });

        addOcdsDownloadColumn();

        super.onInitialize();
    }


    @Override
    public JpaFilterState<TenderProcess> newFilterState() {
        return new TenderProcessFilterState();
    }
}
