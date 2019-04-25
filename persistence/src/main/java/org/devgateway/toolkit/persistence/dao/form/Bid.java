package org.devgateway.toolkit.persistence.dao.form;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.devgateway.toolkit.persistence.dao.AbstractChildAuditableEntity;
import org.devgateway.toolkit.persistence.dao.ListViewItem;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author gmutuhu
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
@Table(indexes = {@Index(columnList = "parent_id")})
public class Bid extends AbstractChildAuditableEntity<TenderQuotationEvaluation>  implements ListViewItem {
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Supplier supplier;
    private String supplierResponsiveness;
    private Integer supplierScore;
    private Integer supplierRanking;
    private Double quotedAmount;    

    public Supplier getSupplier() {
        return supplier;
    }

    public String getSupplierResponsiveness() {
        return supplierResponsiveness;
    }

    public Integer getSupplierScore() {
        return supplierScore;
    }

    public Integer getSupplierRanking() {
        return supplierRanking;
    }

    public Double getQuotedAmount() {
        return quotedAmount;
    }

    public void setSupplier(final Supplier supplier) {
        this.supplier = supplier;
    }

    public void setSupplierResponsiveness(final String supplierResponsiveness) {
        this.supplierResponsiveness = supplierResponsiveness;
    }

    public void setSupplierScore(final Integer supplierScore) {
        this.supplierScore = supplierScore;
    }

    public void setSupplierRanking(final Integer supplierRanking) {
        this.supplierRanking = supplierRanking;
    }

    public void setQuotedAmount(final Double quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    @Transient
    @JsonIgnore
    private Boolean expanded = false;

    @Override
    public Boolean getEditable() {
        return null;
    }

    @Override
    public void setEditable(final Boolean editable) {

    }

    @Override
    public Boolean getExpanded() {
        return expanded;
    }

    @Override
    public void setExpanded(final Boolean expanded) {
        this.expanded = expanded;
    }
}
