package org.devgateway.toolkit.forms.wicket.page.lists.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.ocds.forms.wicket.FormSecurityUtil;
import org.devgateway.toolkit.forms.service.SessionMetadataService;
import org.devgateway.toolkit.forms.wicket.components.table.SelectFilteredBootstrapPropertyColumn;
import org.devgateway.toolkit.forms.wicket.components.table.TextFilteredBootstrapPropertyColumn;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditCabinetPaperPage;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.service.filterstate.JpaFilterState;
import org.devgateway.toolkit.persistence.service.filterstate.form.CabinetPaperFilterState;
import org.devgateway.toolkit.persistence.service.form.CabinetPaperService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author gmutuhu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath("/cabinetPapers")
public class ListCabinetPaperPage extends ListAbstractMakueniEntityPage<CabinetPaper> {

    @SpringBean
    protected CabinetPaperService cabinetPaperService;

    @SpringBean
    private SessionMetadataService sessionMetadataService;

    public ListCabinetPaperPage(final PageParameters parameters) {
        super(parameters);

        this.jpaService = cabinetPaperService;
        this.editPageClass = EditCabinetPaperPage.class;
    }

    @Override
    protected void onInitialize() {
        attachFm("cabinetPapersList");

        super.onInitialize();
    }

    @Override
    protected void addColumns() {
        addFmColumn("department", new SelectFilteredBootstrapPropertyColumn<>(
                new StringResourceModel("department", this),
                "procurementPlan.department", "procurementPlan.department",
                new ListModel<>(departments), getDataTable(),
                isPreselected() && !FormSecurityUtil.isCurrentUserAdmin()
        ));

        addFmColumn("fiscalYear", new SelectFilteredBootstrapPropertyColumn<>(
                new StringResourceModel("fiscalYears", this),
                "procurementPlan.fiscalYear", "procurementPlan.fiscalYear",
                new ListModel<>(fiscalYears), getDataTable()
        ));

        addFmColumn("number", new TextFilteredBootstrapPropertyColumn<>(
                new StringResourceModel("number", this), "number",
                "number"
        ));

        addFileDownloadColumn();
    }

    @Override
    public JpaFilterState<CabinetPaper> newFilterState() {
        final CabinetPaperFilterState cabinetPaperFilterState = new CabinetPaperFilterState();
        if (isPreselected()) {
            cabinetPaperFilterState.setProcurementPlan(sessionMetadataService.getSessionPP());
        }
        return cabinetPaperFilterState;
    }

    private boolean isPreselected() {
        return sessionMetadataService.getSessionPP() != null;
    }

}
