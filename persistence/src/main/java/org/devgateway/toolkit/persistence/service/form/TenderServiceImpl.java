package org.devgateway.toolkit.persistence.service.form;


import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.Tender_;
import org.devgateway.toolkit.persistence.repository.form.TenderRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.metamodel.SingularAttribute;

/**
 * @author gmutuhu
 */
@Service
@Transactional
public class TenderServiceImpl extends AbstractTenderProcessEntityServiceImpl<Tender> implements TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    @Override
    protected BaseJpaRepository<Tender, Long> repository() {
        return tenderRepository;
    }

    @Override
    public Tender newInstance() {
        return new Tender();
    }

    @Override
    public Tender findByTenderProcess(final TenderProcess tenderProcess) {
        return tenderRepository.findByTenderProcess(tenderProcess);
    }

    @Override
    public SingularAttribute<? super Tender, String> getTextAttribute() {
        return Tender_.tenderTitle;
    }
}
