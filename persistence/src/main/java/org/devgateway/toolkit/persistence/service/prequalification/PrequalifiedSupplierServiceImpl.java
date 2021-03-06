package org.devgateway.toolkit.persistence.service.prequalification;

import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.Bid;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.prequalification.AbstractContact;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalificationYearRange;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalifiedSupplier;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalifiedSupplierItem;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalifiedSupplier_;
import org.devgateway.toolkit.persistence.dao.prequalification.SupplierContact;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.repository.prequalification.PrequalifiedSupplierRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.devgateway.toolkit.persistence.service.category.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Octavian Ciubotaru
 */
@Service
public class PrequalifiedSupplierServiceImpl
        extends BaseJpaServiceImpl<PrequalifiedSupplier>
        implements PrequalifiedSupplierService {

    @Autowired
    private PrequalifiedSupplierRepository repository;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PrequalificationYearRangeService prequalificationYearRangeService;

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

    @Override
    public List<String> findItemsForBid(Bid bid, Tender tender) {
        Supplier supplier = bid.getSupplier();

        if (supplier == null) {
            return Collections.emptyList();
        }

        return find(supplier, tender)
                .map(this::formatItems)
                .orElse(Collections.emptyList());
    }

    private List<String> formatItems(PrequalifiedSupplier prequalifiedSupplier) {
        PrequalificationYearRange yearRange = prequalifiedSupplier.getYearRange();

        return prequalifiedSupplier.getItems().stream()
                .map(i -> i.getItem().toString(yearRange))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PrequalifiedSupplier> find(Supplier supplier, Tender tender) {
        if (supplier == null) {
            return Optional.empty();
        }

        Date tenderInvitationDate = tender.getInvitationDate();
        PrequalificationYearRange yearRange = prequalificationYearRangeService.findByDate(tenderInvitationDate);

        if (yearRange == null) {
            return Optional.empty();
        }

        return find(supplier, yearRange);
    }

    @Override
    public Optional<PrequalifiedSupplier> find(Supplier supplier, PrequalificationYearRange yearRange) {
        return repository
                .findOne((Specification<PrequalifiedSupplier>) (root, cq, cb) -> cb.and(
                        cb.equal(root.get(PrequalifiedSupplier_.supplier), supplier),
                        cb.equal(root.get(PrequalifiedSupplier_.yearRange), yearRange)));
    }
}
