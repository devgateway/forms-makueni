package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.form.AwardNotificationRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 */
@Service
@Transactional
public class AwardNotificationServiceImpl extends AbstractTenderProcessEntityServiceImpl<AwardNotification>
        implements AwardNotificationService {

    @Autowired
    private AwardNotificationRepository awardNotificationRepository;

    @Override
    protected BaseJpaRepository<AwardNotification, Long> repository() {
        return awardNotificationRepository;
    }

    @Override
    public AwardNotification newInstance() {
        return new AwardNotification();
    }

    @Override
    public AwardNotification findByTenderProcess(final TenderProcess tenderProcess) {
        return awardNotificationRepository.findByTenderProcess(tenderProcess);
    }

}
