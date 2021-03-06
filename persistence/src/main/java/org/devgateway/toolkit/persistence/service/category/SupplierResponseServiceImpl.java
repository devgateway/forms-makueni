package org.devgateway.toolkit.persistence.service.category;

import org.devgateway.toolkit.persistence.dao.categories.SupplierResponse;
import org.devgateway.toolkit.persistence.repository.category.SupplierResponseRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mpostelnicu
 */
@Service
@Transactional(readOnly = true)
public class SupplierResponseServiceImpl extends CategoryServiceImpl<SupplierResponse>
        implements SupplierResponseService {
    @Autowired
    private SupplierResponseRepository repository;

    @Override
    protected BaseJpaRepository<SupplierResponse, Long> repository() {
        return repository;
    }
    
    @Override
    public SupplierResponse newInstance() {
        return new SupplierResponse();
    }
}

