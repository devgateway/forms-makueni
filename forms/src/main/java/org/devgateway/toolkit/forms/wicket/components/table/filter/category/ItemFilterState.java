package org.devgateway.toolkit.forms.wicket.components.table.filter.category;

import org.devgateway.toolkit.persistence.dao.categories.Item;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


public class ItemFilterState extends AbstractCategoryFilterState<Item> {
    @Override
    public Specification<Item> getSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();   
            predicates.add(super.getSpecification().toPredicate(root, query, cb));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }  
}
