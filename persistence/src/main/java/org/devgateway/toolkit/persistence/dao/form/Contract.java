package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Form;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
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
import java.util.Date;
import java.util.List;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "tender_process_id")},
        uniqueConstraints =
        @UniqueConstraint(columnNames = "tender_process_id"))
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form(featureName = "contractForm")
@UniqueTenderProcessEntity(groups = HighLevel.class, payload = Severity.NonRecoverable.class,
        message = "{org.devgateway.toolkit.persistence.dao.form.UniqueContract.message}")
public class Contract extends AbstractTenderProcessMakueniEntity {
    @ExcelExport(useTranslation = true, name = "Contract Value")
    private BigDecimal contractValue;

    @ExcelExport(name = "Supplier")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Supplier awardee;

    @ExcelExport(useTranslation = true, name = "Contract Date")
    private Date contractDate;

    @ExcelExport(useTranslation = true, name = "Contract Approved Date")
    private Date contractApprovalDate;

    @ExcelExport(useTranslation = true, name = "Expiry Date")
    private Date expiryDate;

    @ExcelExport(useTranslation = true, name = "Reference Number")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String referenceNumber;

    @ExcelExport(separateSheet = true, useTranslation = true, name = "Contract Documents")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<ContractDocument> contractDocs = new ArrayList<>();

    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(final BigDecimal contractValue) {
        this.contractValue = contractValue;
    }

    public Supplier getAwardee() {
        return awardee;
    }

    public void setAwardee(final Supplier awardee) {
        this.awardee = awardee;
    }

    public Date getContractApprovalDate() {
        return contractApprovalDate;
    }

    public void setContractApprovalDate(final Date contractApprovalDate) {
        this.contractApprovalDate = contractApprovalDate;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(final Date contractDate) {
        this.contractDate = contractDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(final String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public List<ContractDocument> getContractDocs() {
        return contractDocs;
    }

    public void setContractDocs(final List<ContractDocument> contractDocs) {
        this.contractDocs = contractDocs;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return "Contract for tender process " + getTenderProcessNotNull().getLabel();
    }

    @JsonIgnore
    public boolean isTerminatedWithImplementation() {
        return PersistenceUtil.checkTerminated(
                ArrayUtils.add(getDirectChildrenEntities().toArray(new Statusable[]{}), this));
    }


    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    @Override
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        ArrayList<AbstractMakueniEntity> children = new ArrayList<>();
        children.addAll(getTenderProcessNotNull().getAdministratorReports());
        children.addAll(getTenderProcessNotNull().getPmcReports());
        children.addAll(getTenderProcessNotNull().getInspectionReports());
        children.addAll(getTenderProcessNotNull().getMeReports());
        children.addAll(getTenderProcessNotNull().getPaymentVouchers());
        return children;
    }

    @Override
    public Class<?> getNextForm() {
        return null;
    }

    @Override
    public boolean hasDownstreamForms() {
        return false;
    }
}
