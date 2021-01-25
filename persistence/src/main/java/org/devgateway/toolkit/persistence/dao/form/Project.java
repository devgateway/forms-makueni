package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Form;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author idobre
 * @since 2019-04-02
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "procurement_plan_id"), @Index(columnList = "projectTitle")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form
public class Project extends AbstractMakueniEntity implements ProcurementPlanAttachable, TitleAutogeneratable {
    @ExcelExport(separateSheet = true, useTranslation = true, name = "Cabinet Papers")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<CabinetPaper> cabinetPapers;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "procurement_plan_id")
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    private ProcurementPlan procurementPlan;

    @ExcelExport(useTranslation = true, name = "Project Title")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String projectTitle;

    @ExcelExport(useTranslation = true, name = "Amount Budgeted")
    private BigDecimal amountBudgeted;

    @ExcelExport(useTranslation = true, name = "Amount Requested")
    private BigDecimal amountRequested;

    @ExcelExport(justExport = true, useTranslation = true, name = "Sub-Counties")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<Subcounty> subcounties;

    @ExcelExport(justExport = true, useTranslation = true, name = "Wards")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    private List<Ward> wards = new ArrayList<>();

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(final String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public BigDecimal getAmountBudgeted() {
        return amountBudgeted;
    }

    public void setAmountBudgeted(final BigDecimal amountBudgeted) {
        this.amountBudgeted = amountBudgeted;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(final BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public List<Subcounty> getSubcounties() {
        return subcounties;
    }

    public void setSubcounties(final List<Subcounty> subcounties) {
        this.subcounties = subcounties;
    }

    public List<Ward> getWards() {
        return wards;
    }

    public void setWards(final List<Ward> wards) {
        this.wards = wards;
    }

    @Override
    public void setLabel(final String label) {

    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return projectTitle;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public ProcurementPlan getProcurementPlan() {
        return procurementPlan;
    }

    public void setProcurementPlan(ProcurementPlan procurementPlan) {
        this.procurementPlan = procurementPlan;
    }


    @Override
    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getTitle() {
        return getProjectTitle();
    }

    @Override
    public Consumer<String> titleSetter() {
        return this::setProjectTitle;
    }

    @Override
    @JsonIgnore
    @Transactional
    @org.springframework.data.annotation.Transient
    public Department getDepartment() {
        return getProcurementPlan().getDepartment();
    }

    public List<CabinetPaper> getCabinetPapers() {
        return cabinetPapers;
    }

    public void setCabinetPapers(List<CabinetPaper> cabinetPapers) {
        this.cabinetPapers = cabinetPapers;
    }
}
