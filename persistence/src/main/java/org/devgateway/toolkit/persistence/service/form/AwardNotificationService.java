package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

/**
 * @author gmutuhu
 */
public interface AwardNotificationService extends AbstractTenderProcessEntityService<AwardNotification> {
    AwardNotification findByTenderProcess(TenderProcess tenderProcess);
}
