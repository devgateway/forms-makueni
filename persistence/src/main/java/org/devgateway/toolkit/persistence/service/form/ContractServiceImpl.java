package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.form.ContractRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 */
@Service
@Transactional
public class ContractServiceImpl extends AbstractTenderProcessEntityServiceImpl<Contract>
        implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Override
    protected BaseJpaRepository<Contract, Long> repository() {
        return contractRepository;
    }

    @Override
    public Contract newInstance() {
        return new Contract();
    }

    @Override
    public Contract findByTenderProcess(final TenderProcess tenderProcess) {
        return contractRepository.findByTenderProcess(tenderProcess);
    }

}
