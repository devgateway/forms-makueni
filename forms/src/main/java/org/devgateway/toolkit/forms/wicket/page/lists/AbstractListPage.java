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
package org.devgateway.toolkit.forms.wicket.page.lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Size;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeIconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.ladda.LaddaAjaxButton;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.exceptions.NullEditPageClassException;
import org.devgateway.toolkit.forms.exceptions.NullJpaServiceException;
import org.devgateway.toolkit.forms.service.PermissionEntityRenderableService;
import org.devgateway.toolkit.forms.wicket.components.form.AJAXDownload;
import org.devgateway.toolkit.forms.wicket.page.RevisionsPage;
import org.devgateway.toolkit.forms.wicket.page.edit.AbstractEditPage;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditAbstractMakueniEntityPage;
import org.devgateway.toolkit.forms.wicket.providers.AbstractDataProvider;
import org.devgateway.toolkit.forms.wicket.providers.SortableJpaServiceDataProvider;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.excel.service.ExcelGeneratorService;
import org.devgateway.toolkit.persistence.service.BaseJpaService;
import org.devgateway.toolkit.web.Constants;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * Data table is initialized in onInitialize() to allow greater flexibility for the subclasses in its own
 * initialization. Subclasses may not reference dataTable unless super.onInitialize() was called.
 *
 * Columns must be added inside addColumns(). Data table and data provider are already instantiated at this point.
 *
 * @author mpostelnicu This class can be use to display a list of Categories
 * <p>
 */
public abstract class AbstractListPage<T extends GenericPersistable & Serializable> extends AbstractBaseListPage<T> {
    protected Class<? extends AbstractEditPage<T>> editPageClass;

    protected BaseJpaService<T> jpaService;

    protected boolean hasEditPage = true;
    protected boolean hasNewPage = true;

    protected Form excelForm;

    @SpringBean
    private ExcelGeneratorService excelGeneratorService;

    @SpringBean
    private PermissionEntityRenderableService permissionEntityRenderableService;

    protected void addFmColumn(String fmName, IColumn<T, String> column) {
        if (getFmService().isFeatureVisible(getParentCombinedFmName(this, fmName))) {
            columns.add(column);
        }
    }

    protected void addFmColumn(String fmName, int i, IColumn<T, String> column) {
        if (getFmService().isFeatureVisible(getParentCombinedFmName(this, fmName))) {
            columns.add(i, column);
        }
    }

    public AbstractListPage(final PageParameters parameters) {
        super(parameters);
    }

    protected AbstractDataProvider<T> createDataProvider() {
        return new SortableJpaServiceDataProvider<>(jpaService);
    }

    public ActionPanel getActionPanel(final String id, final IModel<T> model) {
        return new ActionPanel(id, model);
    }

    @Override
    protected void onInitialize() {
        if (jpaService == null) {
            throw new NullJpaServiceException();
        }
        if (hasEditPage && editPageClass == null) {
            throw new NullEditPageClassException();
        }

        super.onInitialize();

        // create the excel download form; by default this form is hidden and we should make it visible only to pages
        // where we want to export entities to excel file
        excelForm = new ExcelDownloadForm("excelForm");
        excelForm.setVisibilityAllowed(false);
        add(excelForm);
    }

    @Override
    protected Component createAddButton(String id) {
        if (hasNewPage) {
            BootstrapBookmarkablePageLink<T> button;
            button = new BootstrapBookmarkablePageLink<>(id, editPageClass, Buttons.Type.Success);
            button.setIconType(FontAwesomeIconType.plus_circle).setSize(Size.Large)
                    .setLabel(new StringResourceModel("new", AbstractListPage.this));
            return button;
        } else {
            return super.createAddButton(id);
        }
    }

    @Override
    protected void addActionColumn() {
        // add the 'Edit' button
        if (hasEditPage) {
            columns.add(new AbstractColumn<T, String>(new StringResourceModel("actionsColumn", this, null)) {
                private static final long serialVersionUID = -7447601118569862123L;

                @Override
                public void populateItem(final Item<ICellPopulator<T>> cellItem, final String componentId,
                        final IModel<T> model) {
                    cellItem.add(getActionPanel(componentId, model));
                }
            });
        }
    }

    public class ActionPanel extends Panel {
        private static final long serialVersionUID = 5821419128121941939L;

        /**
         * @param id
         * @param model
         */
        public ActionPanel(final String id, final IModel<T> model) {
            super(id, model);

            final PageParameters pageParameters = new PageParameters();

            @SuppressWarnings("unchecked")
            T entity = (T) ActionPanel.this.getDefaultModelObject();
            if (entity != null) {
                pageParameters.set(WebConstants.PARAM_ID, entity.getId());
            }

            PageProvider pageProvider = new PageProvider(editPageClass);
            IRequestablePage editPage = pageProvider.getPageInstance();

            final BootstrapBookmarkablePageLink<T> editPageLink =
                    new BootstrapBookmarkablePageLink<>("edit", editPageClass, pageParameters, Buttons.Type.Info);
            editPageLink.setIconType(FontAwesomeIconType.edit)
                    .setSize(Size.Small)
                    .setType(Buttons.Type.Primary)
                    .setLabel(new StringResourceModel("edit", AbstractListPage.this, null));
            if (editPage instanceof EditAbstractMakueniEntityPage && entity instanceof AbstractMakueniEntity
                    && SecurityConstants.Action.VIEW.equals(permissionEntityRenderableService.getAllowedAccess(
                    (EditAbstractMakueniEntityPage<?>) editPage, (AbstractMakueniEntity) entity))) {
                editPageLink.setIconType(FontAwesomeIconType.eye)
                        .setType(Buttons.Type.Warning)
                        .setLabel(new StringResourceModel("view", AbstractListPage.this, null));
            }
            add(editPageLink);

            add(getPrintButton(pageParameters));

            final PageParameters revisionsPageParameters = new PageParameters();
            revisionsPageParameters.set(WebConstants.PARAM_ID, entity.getId());
            revisionsPageParameters.set(WebConstants.PARAM_ENTITY_CLASS, entity.getClass().getName());

            final BootstrapBookmarkablePageLink<Void> revisionsPageLink = new BootstrapBookmarkablePageLink<>(
                    "revisions", RevisionsPage.class, revisionsPageParameters, Buttons.Type.Info);
            revisionsPageLink.setIconType(FontAwesomeIconType.clock_o).setSize(Size.Small)
                    .setLabel(new StringResourceModel("revisions", AbstractListPage.this, null));
            add(revisionsPageLink);
            MetaDataRoleAuthorizationStrategy.authorize(
                    revisionsPageLink, Component.RENDER, SecurityConstants.Roles.ROLE_ADMIN);

            revisionsPageLink.setVisibilityAllowed(false);
        }
    }

    /**
     * Get a stub print button that does nothing
     *
     * @param pageParameters
     * @return
     */
    protected Component getPrintButton(final PageParameters pageParameters) {
        return new WebMarkupContainer("printButton").setVisibilityAllowed(false);
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
                                final int batchSize = 10000;

                                final long count = excelGeneratorService.count(
                                        jpaService,
                                        getDataProvider().getFilterState().getSpecification());

                                // if we need to export just one file then we don't create an archive
                                if (count <= batchSize) {
                                    // set a maximum download of objects per excel file
                                    final PageRequest pageRequest = PageRequest.of(0, batchSize,
                                            Sort.Direction.ASC, "id");

                                    final byte[] bytes = excelGeneratorService.getExcelDownload(
                                            jpaService,
                                            getDataProvider().getFilterState().getSpecification(),
                                            pageRequest);

                                    response.setContentType(
                                            Constants.ContentType.XLSX);
                                    response.setHeader("Content-Disposition", "attachment; filename=excel-export.xlsx");
                                    response.getOutputStream().write(bytes);
                                } else {
                                    response.setContentType("application/zip");
                                    response.setHeader("Content-Disposition", "attachment; filename=excel-export.zip");
                                    response.flushBuffer();
                                    final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(
                                            response.getOutputStream()));
                                    zout.setMethod(ZipOutputStream.DEFLATED);
                                    zout.setLevel(Deflater.NO_COMPRESSION);
                                    final int numberOfPages = (int) Math.ceil((double) count / batchSize);
                                    for (int i = 0; i < numberOfPages; i++) {
                                        final PageRequest pageRequest = PageRequest.of(i, batchSize,
                                                Sort.Direction.ASC, "id");
                                        final byte[] bytes = excelGeneratorService.getExcelDownload(
                                                jpaService,
                                                getDataProvider().getFilterState().getSpecification(),
                                                pageRequest);
                                        final ZipEntry ze = new ZipEntry("excel-export-page " + (i + 1) + ".xlsx");
                                        zout.putNextEntry(ze);
                                        zout.write(bytes, 0, bytes.length);
                                        zout.closeEntry();
                                        response.flushBuffer();
                                    }
                                    zout.close();
                                    response.flushBuffer();
                                }
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

            final LaddaAjaxButton excelButton = new LaddaAjaxButton("excelButton",
                    new StringResourceModel("excelDownload", this),
                    Buttons.Type.Warning) {
                @Override
                protected void onSubmit(final AjaxRequestTarget target) {
                    super.onSubmit(target);

                    // initiate the file download
                    download.initiate(target);
                }
            };
            excelButton.setIconType(FontAwesomeIconType.file_excel_o);
            add(excelButton);
        }
    }
}
