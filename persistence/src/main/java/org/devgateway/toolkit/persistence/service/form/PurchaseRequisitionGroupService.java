package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.TextSearchableService;

/**
 * @author mpostelnicu
 *
 */
public interface PurchaseRequisitionGroupService extends AbstractTenderProcessEntityService<PurchaseRequisitionGroup>,
        TextSearchableService<PurchaseRequisitionGroup> {
    PurchaseRequisitionGroup findByTenderProcess(TenderProcess tenderProcess);
}
