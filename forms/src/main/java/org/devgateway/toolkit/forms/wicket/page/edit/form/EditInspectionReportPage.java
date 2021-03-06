package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.TextAreaFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.PrivateSectorRequestPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.TechAdminRoleAssignable;
import org.devgateway.toolkit.forms.wicket.providers.GenericPersistableJpaTextChoiceProvider;
import org.devgateway.toolkit.persistence.dao.form.InspectionReport;
import org.devgateway.toolkit.persistence.service.category.InspectionReportOutcomeService;
import org.devgateway.toolkit.persistence.service.form.InspectionReportService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author mpostelnicu
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditInspectionReportPage extends EditAbstractImplTenderProcessEntityPage<InspectionReport>
        implements TechAdminRoleAssignable {

    @SpringBean
    protected InspectionReportService inspectionReportService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    @SpringBean
    protected InspectionReportOutcomeService inspectionReportOutcomeService;

    public EditInspectionReportPage(PageParameters parameters) {
        super(parameters);
        this.jpaService = inspectionReportService;
    }

    public EditInspectionReportPage() {
        this(new PageParameters());
    }

    @Override
    protected InspectionReport newInstance() {
        final InspectionReport ar = super.newInstance();
        ar.setTenderProcess(sessionMetadataService.getSessionTenderProcess());
        return ar;
    }

    @Override
    protected void onInitialize() {
        editForm.attachFm("inspectionReportForm");
        super.onInitialize();

        Fragment inspectionExtraFields = new Fragment("childExtraFields", "inspectionExtraFields", this);
        abstractImplExtraFields.replace(inspectionExtraFields);
        inspectionExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.singleContract.contractValue"));

        ComponentUtil.addYesNoToggle(editForm, "authorizePayment", true).required();

        TextAreaFieldBootstrapFormComponent<String> comment = ComponentUtil.addTextAreaField(editForm, "comments");
        editForm.add(comment);

        ComponentUtil.addDateField(editForm, "approvedDate").required();

        ComponentUtil.addSelect2ChoiceField(editForm, "outcome",
                new GenericPersistableJpaTextChoiceProvider<>(inspectionReportOutcomeService));

        editForm.add(new PrivateSectorRequestPanel("privateSectorRequests"));

        editForm.add(new FileInputBootstrapFormComponent("picture"));
    }
}
