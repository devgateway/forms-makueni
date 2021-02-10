package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

/**
 * @author Octavian Ciubotaru
 */
public interface AbstractTenderProcessEntityService<T extends AbstractTenderProcessMakueniEntity>
        extends AbstractMakueniEntityService<T> {

    long countByTenderProcess(Long entityId, TenderProcess tenderProcess);
}
