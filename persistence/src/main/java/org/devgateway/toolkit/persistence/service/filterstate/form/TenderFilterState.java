package org.devgateway.toolkit.persistence.service.filterstate.form;

import org.apache.commons.lang3.StringUtils;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.Tender_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author idobre
 * @since 2019-04-02
 */
public class TenderFilterState extends AbstractTenderProcessMakueniFilterState<Tender> {

    private String tenderTitle;

    @Override
    public Specification<Tender> getSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(tenderTitle)) {
                predicates.add(cb.like(cb.lower(root.get(Tender_.tenderTitle)), "%" + tenderTitle.toLowerCase() + "%"));
            }

            predicates.add(super.getSpecification().toPredicate(root, query, cb));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public String getTenderTitle() {
        return tenderTitle;
    }

    public void setTenderTitle(final String tenderTitle) {
        this.tenderTitle = tenderTitle;
    }
}
