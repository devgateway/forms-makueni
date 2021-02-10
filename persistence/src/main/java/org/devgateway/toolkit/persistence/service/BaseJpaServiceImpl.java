package org.devgateway.toolkit.persistence.service;

import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.validator.groups.HighLevel;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author idobre
 * @since 2019-03-04
 */
public abstract class BaseJpaServiceImpl<T extends GenericPersistable & Serializable> implements BaseJpaService<T> {

    @Autowired
    private Validator validator;

    @Override
    public List<T> findAll() {
        return repository().findAll();
    }

    @Override
    public List<T> findAll(final Sort sort) {
        return repository().findAll(sort);
    }

    @Override
    public List<T> findAll(final Specification<T> spec) {
        return repository().findAll(spec);
    }

    @Override
    public List<T> findAllNoCache(final Specification<T> spec) {
        return repository().findAll(spec);
    }

    @Override
    public Page<T> findAll(final Specification<T> spec, final Pageable pageable) {
        return repository().findAll(spec, pageable);
    }

    @Override
    public Page<T> findAllNoCache(final Specification<T> spec, final Pageable pageable) {
        return repository().findAll(spec, pageable);
    }

    @Override
    public Optional<T> findOne(final Specification<T> spec) {
        return repository().findOne(spec);
    }

    @Override
    public Page<T> findAll(final Pageable pageable) {
        return repository().findAll(pageable);
    }

    @Override
    public List<T> findAll(final Specification<T> spec, final Sort sort) {
        return repository().findAll(spec, sort);
    }

    @Override
    public long count(final Specification<T> spec) {
        return repository().count(spec);
    }

    @Override
    public long countNoCache(final Specification<T> spec) {
        return repository().count(spec);
    }

    @Override
    @Transactional
    public Optional<T> findById(final Long id) {
        return repository().findById(id);
    }

    @Override
    public Set<T> findAllById(Iterable<Long> ids) {
        return new HashSet<>(repository().findAllById(ids));
    }

    @Override
    public long count() {
        return repository().count();
    }

    @Override
    @Transactional
    public <S extends T> S save(final S entity) {
        assertEntityIsValid(entity);
        return repository().save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public <S extends T> List<S> saveAll(final Iterable<S> entities) {
        for (S entity : entities) {
            assertEntityIsValid(entity);
        }
        return repository().saveAll(entities);
    }

    @Override
    @Transactional
    public <S extends T> S saveAndFlush(final S entity) {
        assertEntityIsValid(entity);
        return repository().saveAndFlush(entity);
    }

    private <S extends T> void assertEntityIsValid(S entity) {
        Set<ConstraintViolation<S>> violations = validator.validate(entity, Default.class, HighLevel.class);
        if (!violations.isEmpty()) {
            throw new InvalidObjectException(new HashSet<>(violations));
        }
    }

    @Override
    @Transactional
    public void delete(final T entity) {
        repository().delete(entity);
    }

    protected abstract BaseJpaRepository<T, Long> repository();

}
