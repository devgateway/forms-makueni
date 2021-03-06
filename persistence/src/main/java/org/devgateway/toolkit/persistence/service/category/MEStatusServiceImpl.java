/**
 *
 */
package org.devgateway.toolkit.persistence.service.category;

import org.devgateway.toolkit.persistence.dao.categories.MEStatus;
import org.devgateway.toolkit.persistence.repository.category.MEStatusRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mpostelnicu
 *
 */
@Service
@Transactional
public class MEStatusServiceImpl extends CategoryServiceImpl<MEStatus> implements MEStatusService {

    @Autowired
    private MEStatusRepository repository;

    @Override
    protected BaseJpaRepository<MEStatus, Long> repository() {
        return repository;
    }

    @Override
    public MEStatus newInstance() {
        return new MEStatus();
    }
}
