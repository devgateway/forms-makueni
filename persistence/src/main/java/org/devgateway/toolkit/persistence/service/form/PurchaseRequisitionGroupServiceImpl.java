package org.devgateway.toolkit.persistence.service.form;


import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.form.PurchaseRequisitionGroupRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.repository.norepository.TextSearchableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 */
@Service
@Transactional
public class PurchaseRequisitionGroupServiceImpl
        extends AbstractTenderProcessEntityServiceImpl<PurchaseRequisitionGroup>
        implements PurchaseRequisitionGroupService {

    @Autowired
    private PurchaseRequisitionGroupRepository purchaseRequisitionGroupRepository;

    @Override
    protected BaseJpaRepository<PurchaseRequisitionGroup, Long> repository() {
        return purchaseRequisitionGroupRepository;
    }

    @Override
    public TextSearchableRepository<PurchaseRequisitionGroup, Long> textRepository() {
        return purchaseRequisitionGroupRepository;
    }

    @Override
    public PurchaseRequisitionGroup newInstance() {
        return new PurchaseRequisitionGroup();
    }

    @Override
    public PurchaseRequisitionGroup findByTenderProcess(final TenderProcess tenderProcess) {
        return purchaseRequisitionGroupRepository.findByTenderProcess(tenderProcess);
    }
}
