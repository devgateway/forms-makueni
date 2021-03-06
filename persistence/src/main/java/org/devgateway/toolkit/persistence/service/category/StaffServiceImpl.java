/**
 * 
 */
package org.devgateway.toolkit.persistence.service.category;

import org.devgateway.toolkit.persistence.dao.categories.Staff;
import org.devgateway.toolkit.persistence.repository.category.StaffRepository;
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
public class StaffServiceImpl  extends CategoryServiceImpl<Staff> implements StaffService {

    @Autowired
    private StaffRepository repository;

    @Override
    protected BaseJpaRepository<Staff, Long> repository() {
        return repository;
    }

    @Override
    public Staff newInstance() {
        return new Staff();
    }
}
