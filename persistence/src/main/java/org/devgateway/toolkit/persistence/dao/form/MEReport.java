package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Form;
import org.devgateway.toolkit.persistence.dao.categories.MEStatus;
import org.devgateway.toolkit.persistence.dao.categories.SubWard;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.AccessType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author mpostelnicu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "tender_process_id")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form
public class MEReport extends AbstractImplTenderProcessMakueniEntity {

    @ExcelExport(useTranslation = true)
    private Long sno;

    @ExcelExport(justExport = true, useTranslation = true, name = "Sub-Counties")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<Subcounty> subcounties;

    @ExcelExport(justExport = true, useTranslation = true, name = "Wards")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<Ward> wards = new ArrayList<>();

    @ExcelExport(justExport = true, useTranslation = true, name = "Sub-Wards")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<SubWard> subwards = new ArrayList<>();

    @ExcelExport(useTranslation = true)
    private BigDecimal lpoAmount;

    @ExcelExport(useTranslation = true)
    private String lpoNumber;

    @ExcelExport(useTranslation = true)
    private BigDecimal expenditure;

    @ExcelExport(useTranslation = true)
    private BigDecimal uncommitted;

    @ExcelExport(useTranslation = true)
    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    private String projectScope;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    @ExcelExport(useTranslation = true)
    private String output;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    @ExcelExport(useTranslation = true)
    private String outcome;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    @ExcelExport(useTranslation = true)
    private String projectProgress;

    @ExcelExport(useTranslation = true)
    private Integer directBeneficiariesTarget;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    @ExcelExport(useTranslation = true)
    private String wayForward;

    @ExcelExport(useTranslation = true)
    private Date byWhen;

    @ExcelExport(useTranslation = true)
    private Boolean inspected;

    @ExcelExport(useTranslation = true)
    private Boolean invoiced;

    private String officerResponsible;

    @ExcelExport(name = "M&E Status")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private MEStatus meStatus;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_AREA)
    @ExcelExport(useTranslation = true)
    private String remarks;

    @ExcelExport(useTranslation = true)
    private String contractorContact;

    @AccessType(AccessType.Type.PROPERTY)
    public BigDecimal getAmountBudgeted() {
        if (getProject() == null) {
            return null;
        }
        return getProject().getAmountBudgeted();
    }

    public void setAmountBudgeted(BigDecimal amountBudgeted) {

    }

    @Override
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.emptyList();
    }

    @Override
    public void setLabel(String label) {

    }

    @Override
    public String getLabel() {
        return super.getLabel() + (getMeStatus() == null ? "" : " (" + getMeStatus() + ")");
    }

    public List<Subcounty> getSubcounties() {
        return subcounties;
    }

    public void setSubcounties(List<Subcounty> subcounties) {
        this.subcounties = subcounties;
    }

    public List<Ward> getWards() {
        return wards;
    }

    public void setWards(List<Ward> wards) {
        this.wards = wards;
    }

    public Long getSno() {
        return sno;
    }

    public void setSno(Long sno) {
        this.sno = sno;
    }

    public BigDecimal getLpoAmount() {
        return lpoAmount;
    }

    public void setLpoAmount(BigDecimal lpoAmount) {
        this.lpoAmount = lpoAmount;
    }

    public String getLpoNumber() {
        return lpoNumber;
    }

    public void setLpoNumber(String lpoNumber) {
        this.lpoNumber = lpoNumber;
    }

    public BigDecimal getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(BigDecimal expenditure) {
        this.expenditure = expenditure;
    }

    public BigDecimal getUncommitted() {
        return uncommitted;
    }

    public void setUncommitted(BigDecimal uncommitted) {
        this.uncommitted = uncommitted;
    }

    public String getProjectScope() {
        return projectScope;
    }

    public void setProjectScope(String projectScope) {
        this.projectScope = projectScope;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getProjectProgress() {
        return projectProgress;
    }

    public void setProjectProgress(String projectProgress) {
        this.projectProgress = projectProgress;
    }

    public Integer getDirectBeneficiariesTarget() {
        return directBeneficiariesTarget;
    }

    public void setDirectBeneficiariesTarget(Integer directBeneficiariesTarget) {
        this.directBeneficiariesTarget = directBeneficiariesTarget;
    }

    public String getWayForward() {
        return wayForward;
    }

    public void setWayForward(String wayForward) {
        this.wayForward = wayForward;
    }

    public Date getByWhen() {
        return byWhen;
    }

    public void setByWhen(Date byWhen) {
        this.byWhen = byWhen;
    }

    public Boolean getInspected() {
        return inspected;
    }

    public void setInspected(Boolean inspected) {
        this.inspected = inspected;
    }

    public Boolean getInvoiced() {
        return invoiced;
    }

    public void setInvoiced(Boolean invoiced) {
        this.invoiced = invoiced;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getContractorContact() {
        return contractorContact;
    }

    public void setContractorContact(String contractorContact) {
        this.contractorContact = contractorContact;
    }

    public MEStatus getMeStatus() {
        return meStatus;
    }

    public void setMeStatus(MEStatus meStatus) {
        this.meStatus = meStatus;
    }

    public String getOfficerResponsible() {
        return officerResponsible;
    }

    public void setOfficerResponsible(String officerResponsible) {
        this.officerResponsible = officerResponsible;
    }

    public List<SubWard> getSubwards() {
        return subwards;
    }

    public void setSubwards(List<SubWard> subwards) {
        this.subwards = subwards;
    }
}
