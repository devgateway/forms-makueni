package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.Contract;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

/**
 * @author gmutuhu
 */
public interface ContractService extends AbstractTenderProcessEntityService<Contract> {
    Contract findByTenderProcess(TenderProcess tenderProcess);
}
