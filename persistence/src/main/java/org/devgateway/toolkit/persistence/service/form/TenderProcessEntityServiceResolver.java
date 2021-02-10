package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;

/**
 * @author Octavian Ciubotaru
 */
public interface TenderProcessEntityServiceResolver {

    <T extends AbstractTenderProcessMakueniEntity> long countByTenderProcess(T entity);

    <T extends AbstractTenderProcessMakueniEntity> T saveAndFlush(T entity);
}
