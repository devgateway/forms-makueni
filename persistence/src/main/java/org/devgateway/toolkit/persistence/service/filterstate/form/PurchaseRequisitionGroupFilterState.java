package org.devgateway.toolkit.persistence.service.filterstate.form;

import org.devgateway.toolkit.persistence.dao.form.PurchaseRequisitionGroup;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author idobre
 * @since 2019-04-17
 */
public class PurchaseRequisitionGroupFilterState extends AbstractTenderProcessMakueniFilterState
        <PurchaseRequisitionGroup> {


    @Override
    public Specification<PurchaseRequisitionGroup> getSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            predicates.add(super.getSpecification().toPredicate(root, query, cb));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
