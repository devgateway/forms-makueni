package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.ProfessionalOpinionItemPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.form.ProfessionalOpinionService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author idobre
 * @since 2019-04-24
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath
public class EditProfessionalOpinionPage extends EditAbstractTenderProcessMakueniEntityPage<ProfessionalOpinion>
        implements ProcurementRoleAssignable {
    @SpringBean
    protected ProfessionalOpinionService professionalOpinionService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    public EditProfessionalOpinionPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = professionalOpinionService;
    }

    public EditProfessionalOpinionPage() {
        this(new PageParameters());
    }


    @Override
    protected void onInitialize() {
        editForm.attachFm("professionalOpinionForm");
        super.onInitialize();

        ProfessionalOpinionItemPanel items = new ProfessionalOpinionItemPanel("items");
        editForm.add(items);
    }

    @Override
    protected ProfessionalOpinion newInstance() {
        final ProfessionalOpinion professionalOpinion = super.newInstance();
        professionalOpinion.setTenderProcess(sessionMetadataService.getSessionTenderProcess());

        return professionalOpinion;
    }

    @Override
    protected void beforeSaveEntity(final ProfessionalOpinion professionalOpinion) {
        super.beforeSaveEntity(professionalOpinion);

        final TenderProcess tenderProcess = professionalOpinion.getTenderProcess();
        tenderProcess.addProfessionalOpinion(professionalOpinion);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final ProfessionalOpinion professionalOpinion) {
        super.beforeDeleteEntity(professionalOpinion);

        final TenderProcess tenderProcess = professionalOpinion.getTenderProcess();
        tenderProcess.removeProfessionalOpinion(professionalOpinion);
        tenderProcessService.save(tenderProcess);
    }


}
