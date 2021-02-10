package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

/**
 * @author gmutuhu
 */
public interface AwardAcceptanceService extends AbstractTenderProcessEntityService<AwardAcceptance> {
    AwardAcceptance findByTenderProcess(TenderProcess tenderProcess);
}
