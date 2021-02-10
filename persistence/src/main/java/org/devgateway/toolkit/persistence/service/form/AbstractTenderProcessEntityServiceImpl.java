package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.form.AbstractTenderProcessMakueniEntityRepository;

/**
 * @author Octavian Ciubotaru
 */
public abstract class AbstractTenderProcessEntityServiceImpl<T extends AbstractTenderProcessMakueniEntity>
        extends AbstractMakueniEntityServiceImpl<T>
        implements AbstractTenderProcessEntityService<T> {

    @Override
    public long countByTenderProcess(Long entityId, TenderProcess tenderProcess) {
        AbstractTenderProcessMakueniEntityRepository repository =
                (AbstractTenderProcessMakueniEntityRepository) repository();
        return repository.countByTenderProcess(entityId == null ? -1 : entityId, tenderProcess);
    }
}
