package org.devgateway.toolkit.persistence.service.category;

import org.devgateway.toolkit.persistence.dao.categories.ContractDocumentType;
import org.devgateway.toolkit.persistence.repository.category.ContractDocumentTypeRepository;
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
public class ContractDocumentTypeServiceImpl extends CategoryServiceImpl<ContractDocumentType>
        implements ContractDocumentTypeService {

    @Autowired
    private ContractDocumentTypeRepository repository;

    @Override
    public ContractDocumentType newInstance() {
        return new ContractDocumentType();
    }

    @Override
    protected BaseJpaRepository<ContractDocumentType, Long> repository() {
        return repository;
    }
}
