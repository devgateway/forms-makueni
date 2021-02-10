package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AbstractImplTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

import java.util.List;

/**
 * @author mpostelnicu
 * @since 2019-05-21
 */
public interface AbstractImplTenderProcessMakueniEntityService<T extends AbstractImplTenderProcessMakueniEntity>
        extends AbstractTenderProcessEntityService<T> {
    List<T> findByTenderProcess(TenderProcess tenderProcess);
}
