package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.form.AwardAcceptanceRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 */
@Service
@Transactional
public class AwardAcceptanceServiceImpl extends AbstractTenderProcessEntityServiceImpl<AwardAcceptance>
        implements AwardAcceptanceService {

    @Autowired
    private AwardAcceptanceRepository awardAcceptanceRepository;

    @Override
    protected BaseJpaRepository<AwardAcceptance, Long> repository() {
        return awardAcceptanceRepository;
    }

    @Override
    public AwardAcceptance newInstance() {
        return new AwardAcceptance();
    }

    @Override
    public AwardAcceptance findByTenderProcess(final TenderProcess tenderProcess) {
        return awardAcceptanceRepository.findByTenderProcess(tenderProcess);
    }

}
