package org.devgateway.toolkit.persistence.service.prequalification;

import com.google.common.collect.ImmutableSet;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.prequalification.AbstractContact;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalificationYearRange;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalifiedSupplier;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalifiedSupplierItem;
import org.devgateway.toolkit.persistence.dao.prequalification.SupplierContact;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.repository.prequalification.PrequalifiedSupplierRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.devgateway.toolkit.persistence.service.category.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Octavian Ciubotaru
 */
@Service
public class PrequalifiedSupplierServiceImpl
        extends BaseJpaServiceImpl<PrequalifiedSupplier>
        implements PrequalifiedSupplierService {

    private static final Collection<String> RELATED_COLLECTION_CACHES = ImmutableSet.of(
            PrequalificationYearRange.class.getName() + ".prequalifiedSuppliers");

    @Autowired
    private PrequalifiedSupplierRepository repository;

    @Autowired
    private SupplierService supplierService;

    @Override
    public PrequalifiedSupplier newInstance() {
        return new PrequalifiedSupplier();
    }

    @Override
    protected BaseJpaRepository<PrequalifiedSupplier, Long> repository() {
        return repository;
    }

    @Override
    public boolean isSupplierPrequalified(Supplier supplier, PrequalificationYearRange yearRange, Long exceptId) {
        return repository.countPrequalifiedSuppliers(supplier, yearRange, exceptId) > 0;
    }

    @Override
    public <S extends PrequalifiedSupplier> S save(S entity) {
        createSupplierContacts(entity);
        return super.save(entity);
    }

    @Override
    public <S extends PrequalifiedSupplier> List<S> saveAll(Iterable<S> entities) {
        entities.forEach(this::createSupplierContacts);
        return super.saveAll(entities);
    }

    @Override
    public <S extends PrequalifiedSupplier> S saveAndFlush(S entity) {
        createSupplierContacts(entity);
        return super.saveAndFlush(entity);
    }

    @Override
    public Collection<String> getRelatedCollectionCaches() {
        return RELATED_COLLECTION_CACHES;
    }

    private <S extends PrequalifiedSupplier> void createSupplierContacts(S entity) {
        Comparator<String> cio = String.CASE_INSENSITIVE_ORDER;
        Set<AbstractContact<?>> contactSet = new TreeSet<>(
                Comparator.<AbstractContact<?>, String>comparing(AbstractContact::getDirectors, cio)
                        .thenComparing(AbstractContact::getEmail, cio)
                        .thenComparing(AbstractContact::getPhoneNumber, cio)
                        .thenComparing(AbstractContact::getMailingAddress, cio));

        contactSet.add(entity.getContact());

        entity.getItems().stream()
                .map(PrequalifiedSupplierItem::getNonNullContact)
                .forEach(contactSet::add);

        Supplier supplier = entity.getSupplier();

        supplier.getContacts().forEach(contactSet::remove);

        contactSet.stream()
                .map(SupplierContact::new)
                .forEach(supplier::addContact);

        supplierService.save(supplier);
    }
}