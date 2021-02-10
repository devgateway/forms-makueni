package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.events.EditingDisabledEvent;
import org.devgateway.toolkit.forms.wicket.page.edit.panel.PurchRequisitionPanel;
import org.devgateway.toolkit.forms.wicket.page.edit.roleassignable.ProcurementRoleAssignable;
import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.category.ChargeAccountService;
import org.devgateway.toolkit.persistence.service.category.StaffService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.persistence.service.form.PurchaseRequisitionGroupService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.springframework.util.ObjectUtils;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author idobre
 * @since 2019-04-17
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath("/purchaseRequisition")
public class EditPurchaseRequisitionGroupPage extends EditAbstractMakueniEntityPage<PurchaseRequisitionGroup>
        implements ProcurementRoleAssignable {
    @SpringBean
    private PurchaseRequisitionGroupService purchaseRequisitionGroupService;

    @SpringBean
    protected TenderProcessService tenderProcessService;

    @SpringBean
    private ProjectService projectService;

    @SpringBean
    protected StaffService staffService;

    @SpringBean
    protected ChargeAccountService chargeAccountService;

    @Override
    protected void checkAndSendEventForDisableEditing() {
        super.checkAndSendEventForDisableEditing();
        if (isTerminated()) {
            send(getPage(), Broadcast.BREADTH, new EditingDisabledEvent());
        }
    }

    public EditPurchaseRequisitionGroupPage() {
        this(new PageParameters());
    }

    public EditPurchaseRequisitionGroupPage(final PageParameters parameters) {
        super(parameters);
        this.jpaService = purchaseRequisitionGroupService;
    }

    @Override
    protected void onInitialize() {
        editForm.attachFm("purchaseRequisitionForm");
        super.onInitialize();

        editForm.add(new GenericSleepFormComponent<>("project.procurementPlan.department"));
        editForm.add(new GenericSleepFormComponent<>("project.procurementPlan.fiscalYear"));

        if (isTerminated()) {
            alertTerminated.setVisibilityAllowed(true);
        }

        final GenericSleepFormComponent purchaseRequestNumber =
                new GenericSleepFormComponent<>("purchaseRequestNumber");
        editForm.add(purchaseRequestNumber);
        if (entityId == null) {
            purchaseRequestNumber.setVisibilityAllowed(false);
        } else {
            purchaseRequestNumber.setVisibilityAllowed(true);
        }

//        ComponentUtil.addSelect2ChoiceField(editForm, "requestedBy", staffService).required();
//        ComponentUtil.addSelect2ChoiceField(editForm, "chargeAccount", chargeAccountService).required();
//        ComponentUtil.addDateField(editForm, "requestApprovalDate").required();

//        editForm.add(new PurchaseItemPanel("purchaseItems"));

        editForm.add(new PurchRequisitionPanel("purchRequisitions"));

        saveTerminateButton.setVisibilityAllowed(false);
    }

    @Override
    protected PurchaseRequisitionGroup newInstance() {
        final PurchaseRequisitionGroup pr = super.newInstance();
        pr.setTenderProcess(sessionMetadataService.getSessionTenderProcess());
        return pr;
    }

    @Override
    public boolean isTerminated() {
        PurchaseRequisitionGroup pr = editForm.getModelObject();
        return pr.isTerminated();
    }

//    @Override
//    protected void beforeSaveEntity(final TenderProcess tenderProcess) {
//        super.beforeSaveEntity(tenderProcess);
//
//        final Project project = tenderProcess.getProject();
//        project.addTenderProcess(tenderProcess);
//        projectService.save(project);
//    }

    @Override
    protected void afterSaveEntity(final PurchaseRequisitionGroup saveable) {
        super.afterSaveEntity(saveable);

        // autogenerate the number
        if (saveable.getPurchaseRequestNumber() == null) {
            saveable.setPurchaseRequestNumber(saveable.getProcurementPlan().getDepartment().getCode()
                    + "/" + saveable.getId());

            jpaService.save(saveable);
        }
    }


//    @Override
//    protected void beforeDeleteEntity(final TenderProcess tenderProcess) {
//        super.beforeDeleteEntity(tenderProcess);
//
//        final Project project = tenderProcess.getProject();
//        project.remoteTenderProcess(tenderProcess);
//        projectService.save(project);
//    }

    @Override
    protected void beforeSaveEntity(final PurchaseRequisitionGroup entity) {
        super.beforeSaveEntity(entity);
        final TenderProcess tenderProcess = entity.getTenderProcess();
        tenderProcess.addPurchaseRequisition(entity);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected void beforeDeleteEntity(final PurchaseRequisitionGroup entity) {
        super.beforeDeleteEntity(entity);

        final TenderProcess tenderProcess = entity.getTenderProcess();
        tenderProcess.removePurchaseRequisition(entity);
        tenderProcessService.save(tenderProcess);
    }

    @Override
    protected Class<? extends Page> pageAfterSubmitAndNext() {
        return EditTenderPage.class;
    }
}
