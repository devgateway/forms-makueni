package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.AbstractImplTenderProcessMakueniEntity;

import java.util.List;

/**
 * @author mpostelnicu
 */
public abstract class AbstractImplTenderProcessMakueniEntityServiceImpl
        <T extends AbstractImplTenderProcessMakueniEntity>
        extends AbstractTenderProcessEntityServiceImpl<T> implements AbstractImplTenderProcessMakueniEntityService<T> {

    @Override
    public <S extends T> S save(S entity) {
        beforeSave(entity);
        return super.save(entity);
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        beforeSave(entity);
        return super.saveAndFlush(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        entities.forEach(this::beforeSave);
        return super.saveAll(entities);
    }

    public void beforeSave(T entity) {
        entity.setContract(entity.getTenderProcess().getSingleContract());
    }
}
