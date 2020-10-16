package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.AbstractChildExpandableAuditEntity;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.ListViewItem;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author gmutuhu
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
@Table(indexes = {@Index(columnList = "parent_id"),
        @Index(columnList = "purchase_item_id"),
        @Index(columnList = "description")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenderItem extends AbstractChildExpandableAuditEntity<Tender> implements ListViewItem {
    @ExcelExport(justExport = true, useTranslation = true, name = "Item")
    @ManyToOne
    private PurchaseItem purchaseItem;

    @ExcelExport(useTranslation = true, name = "Description")
    @Column(length = DBConstants.MAX_DEFAULT_TEXT_LENGTH_ONE_LINE)
    private String description;

    @ExcelExport(useTranslation = true, name = "Quantity")
    private BigDecimal quantity;

    @ExcelExport(useTranslation = true, name = "Unit Price")
    private BigDecimal unitPrice;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(final BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public PurchaseItem getPurchaseItem() {
        return purchaseItem;
    }

    public void setPurchaseItem(final PurchaseItem purchaseItem) {
        this.purchaseItem = purchaseItem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
