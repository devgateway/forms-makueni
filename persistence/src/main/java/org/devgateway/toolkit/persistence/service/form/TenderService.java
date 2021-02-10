package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.TextSearchableService;

/**
 * @author gmutuhu
 *
 */
public interface TenderService extends AbstractTenderProcessEntityService<Tender>, TextSearchableService<Tender> {
    Tender findByTenderProcess(TenderProcess tenderProcess);
}
