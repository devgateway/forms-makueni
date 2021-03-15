/**
 * 
 */
package org.devgateway.toolkit.persistence.service.category;

import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.repository.category.SupplierRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 *
 */
@Service
@Transactional(readOnly = true)
public class SupplierServiceImpl  extends CategoryServiceImpl<Supplier> implements SupplierService {

    @Autowired
    private SupplierRepository repository;

    @Override
    protected BaseJpaRepository<Supplier, Long> repository() {
        return repository;
    }

    @Override
    public Supplier newInstance() {
        return new Supplier();
    }
}
