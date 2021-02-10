package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.Form;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethod;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethodRationale;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.TargetGroup;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.devgateway.toolkit.persistence.validator.Severity;
import org.devgateway.toolkit.persistence.validator.groups.HighLevel;
import org.devgateway.toolkit.persistence.validator.validators.UniqueTenderProcessEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "tender_process_id"),
        @Index(columnList = "tenderTitle"),
        @Index(columnList = "tenderNumber")}, uniqueConstraints =
@UniqueConstraint(columnNames = "tender_process_id"))
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form(featureName = "tenderForm")
@UniqueTenderProcessEntity(groups = HighLevel.class, payload = Severity.NonRecoverable.class,
        message = "{org.devgateway.toolkit.persistence.dao.form.UniqueTender.message}")
public class Tender extends AbstractTenderProcessMakueniEntity implements TitleAutogeneratable {
    @ExcelExport(useTranslation = true, name = "Tender ID")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String tenderNumber;

    @ExcelExport(useTranslation = true, name = "Tender Name")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String tenderTitle;

    @ExcelExport(useTranslation = true, name = "Invitation to Tender Date")
    private Date invitationDate;

    @ExcelExport(useTranslation = true, name = "Closing Date")
    private Date closingDate;

    @ExcelExport(justExport = true, useTranslation = true, name = "Procurement Method")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcurementMethod procurementMethod;


    @ExcelExport(justExport = true, useTranslation = true, name = "Procurement Method Rationale")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcurementMethodRationale procurementMethodRationale;

    @ExcelExport(useTranslation = true, name = "Tender Objective")
    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    private String objective;

    @ExcelExport(useTranslation = true, name = "Tender Issued By")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcuringEntity issuedBy;

    @ExcelExport(useTranslation = true, name = "Tender Value")
    private BigDecimal tenderValue;

    @ExcelExport(justExport = true, useTranslation = true, name = "Target Group")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private TargetGroup targetGroup;

    @ExcelExport(useTranslation = true, name = "Tender Link")
    @Column(length = DBConstants.MAX_DEFAULT_TEXT_LENGTH_ONE_LINE)
    private String tenderLink;

    @ExcelExport(separateSheet = true, useTranslation = true, name = "Tender Items")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<TenderItem> tenderItems = new ArrayList<>();

    @ExcelExport(justExport = true, useTranslation = true, name = "Bill of Quantities")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FileMetadata> billOfQuantities = new HashSet<>();

    @Override
    public void setLabel(final String label) {

    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return tenderNumber + " " + tenderTitle;
    }

    public String getTenderNumber() {
        return tenderNumber;
    }

    public void setTenderNumber(final String tenderNumber) {
        this.tenderNumber = tenderNumber;
    }

    public String getTenderTitle() {
        return tenderTitle;
    }

    public void setTenderTitle(final String tenderTitle) {
        this.tenderTitle = tenderTitle;
    }

    public Date getInvitationDate() {
        return invitationDate;
    }

    public void setInvitationDate(final Date invitationDate) {
        this.invitationDate = invitationDate;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(final Date closingDate) {
        this.closingDate = closingDate;
    }

    public ProcurementMethod getProcurementMethod() {
        return procurementMethod;
    }

    public void setProcurementMethod(final ProcurementMethod procurementMethod) {
        this.procurementMethod = procurementMethod;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(final String objective) {
        this.objective = objective;
    }

    public ProcuringEntity getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(final ProcuringEntity issuedBy) {
        this.issuedBy = issuedBy;
    }

    public BigDecimal getTenderValue() {
        return tenderValue;
    }

    public void setTenderValue(final BigDecimal tenderValue) {
        this.tenderValue = tenderValue;
    }

    public TargetGroup getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(final TargetGroup targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getTenderLink() {
        return tenderLink;
    }


    public void setTenderLink(final String tenderLink) {
        this.tenderLink = tenderLink;
    }

    public List<TenderItem> getTenderItems() {
        return tenderItems;
    }

    public void setTenderItems(final List<TenderItem> tenderItems) {
        this.tenderItems = tenderItems;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (TenderItem item : tenderItems) {
            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                total.add(item.getUnitPrice().multiply(item.getQuantity()));
            }
        }
        return total;
    }

    @Override
    @Transactional
    protected Collection<AbstractMakueniEntity> getDirectChildrenEntities() {

        return Collections.singletonList(PersistenceUtil.getNext(getTenderProcessNotNull()
                .getTenderQuotationEvaluation()));
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getTitle() {
        return getTenderTitle();
    }

    @Override
    public Consumer<String> titleSetter() {
        return this::setTenderTitle;
    }

    public ProcurementMethodRationale getProcurementMethodRationale() {
        return procurementMethodRationale;
    }

    public void setProcurementMethodRationale(ProcurementMethodRationale procurementMethodRationale) {
        this.procurementMethodRationale = procurementMethodRationale;
    }

    public Set<FileMetadata> getBillOfQuantities() {
        return billOfQuantities;
    }

    public void setBillOfQuantities(Set<FileMetadata> billOfQuantities) {
        this.billOfQuantities = billOfQuantities;
    }

    @Override
    public Class<?> getNextForm() {
        return TenderQuotationEvaluation.class;
    }

    @Override
    public boolean hasDownstreamForms() {
        return getTenderProcess().hasFormsDependingOnTender();
    }
}
