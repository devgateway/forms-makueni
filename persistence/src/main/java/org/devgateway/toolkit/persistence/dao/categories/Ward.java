package org.devgateway.toolkit.persistence.dao.categories;

import org.devgateway.toolkit.persistence.dao.AbstractAuditableEntity;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author idobre
 * @since 2019-06-26
 */
@Entity
@Audited
public class Ward extends LocationPointCategory {
    @ManyToOne
    @JoinColumn(name = "subcounty_id")
    private Subcounty subcounty;

    public Subcounty getSubcounty() {
        return subcounty;
    }

    public void setSubcounty(final Subcounty subcounty) {
        this.subcounty = subcounty;
    }

    @Override
    public AbstractAuditableEntity getParent() {
        return subcounty;
    }
}
