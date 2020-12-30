package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.service.BaseJpaService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author idobre
 * @since 2019-05-21
 */
public interface AbstractMakueniEntityService<T extends AbstractMakueniEntity> extends BaseJpaService<T> {
    List<T> findByFiscalYear(FiscalYear fiscalYear);

    /**
     * Gets all the downstream forms/children of given entity in an Set collection.
     * Will invoke {@link AbstractMakueniEntity#getDirectChildrenEntitiesNotNull()} ()} on each
     *
     * @param entity
     * @return
     */
    Collection<? extends AbstractMakueniEntity> getAllChildrenInHierarchy(T entity);

    Stream<? extends AbstractMakueniEntity> getAllSubmitted();

    //Collection<Map<String, String>> validate(TenderProcess tenderProcess, T entity);
}
