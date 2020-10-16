package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author idobre
 * @since 2019-04-17
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "project_id"),
        @Index(columnList = "purchaseRequestNumber")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenderProcess extends AbstractMakueniEntity implements ProjectAttachable, ProcurementPlanAttachable {
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    private Project project;

    @ExcelExport(useTranslation = true, name = "Purchase Request Number")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String purchaseRequestNumber;

    @ExcelExport(name = "Purchase Requisitions", separateSheet = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<PurchRequisition> purchRequisitions = new ArrayList<>();

    @ExcelExport(separateSheet = true, name = "Tender")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<Tender> tender = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Tender Quotation Evaluation")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<TenderQuotationEvaluation> tenderQuotationEvaluation = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Professional Opinion")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<ProfessionalOpinion> professionalOpinion = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Award Notification")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<AwardNotification> awardNotification = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Award Acceptance")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<AwardAcceptance> awardAcceptance = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Contract")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @LazyToOne(value = LazyToOneOption.NO_PROXY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<Contract> contract = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Administrator Reports")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<AdministratorReport> administratorReports = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "PMC Reports")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<PMCReport> pmcReports = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "ME Reports")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<MEReport> meReports = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Inspection Reports")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<InspectionReport> inspectionReports = new HashSet<>();

    @ExcelExport(separateSheet = true, name = "Payment Vouchers")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenderProcess")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private Set<PaymentVoucher> paymentVouchers = new HashSet<>();


    /**
     * Calculates if this {@link TenderProcess} is terminated. This involves going through all stages and
     * checking if any of them is terminated
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTerminated() {
        ArrayList<Statusable> entityTree = new ArrayList<>();
        entityTree.add(PersistenceUtil.getNext(tender));
        entityTree.add(PersistenceUtil.getNext(tenderQuotationEvaluation));
        entityTree.add(PersistenceUtil.getNext(professionalOpinion));
        entityTree.add(PersistenceUtil.getNext(awardNotification));
        entityTree.add(PersistenceUtil.getNext(awardAcceptance));
        entityTree.add(PersistenceUtil.getNext(contract));
        entityTree.addAll(administratorReports);
        entityTree.addAll(inspectionReports);
        entityTree.addAll(pmcReports);
        entityTree.addAll(meReports);
        entityTree.addAll(paymentVouchers);
        return PersistenceUtil.checkTerminated(entityTree.toArray(new Statusable[]{}));
    }

    @JsonProperty("tender")
    public Tender getSingleTender() {
        return PersistenceUtil.getNext(tender);
    }

    @JsonProperty("tenderQuotationEvaluation")
    public TenderQuotationEvaluation getSingleTenderQuotationEvaluation() {
        return PersistenceUtil.getNext(tenderQuotationEvaluation);
    }

    @JsonProperty("professionalOpinion")
    public ProfessionalOpinion getSingleProfessionalOpinion() {
        return PersistenceUtil.getNext(professionalOpinion);
    }

    @JsonProperty("awardNotification")
    public AwardNotification getSingleAwardNotification() {
        return PersistenceUtil.getNext(awardNotification);
    }

    @JsonProperty("awardAcceptance")
    public AwardAcceptance getSingleAwardAcceptance() {
        return PersistenceUtil.getNext(awardAcceptance);
    }

    @JsonProperty("contract")
    public Contract getSingleContract() {
        return PersistenceUtil.getNext(contract);
    }

    @Override
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public String getPurchaseRequestNumber() {
        return purchaseRequestNumber;
    }

    public void setPurchaseRequestNumber(final String purchaseRequestNumber) {
        this.purchaseRequestNumber = purchaseRequestNumber;
    }

    @Override
    public void setLabel(final String label) {

    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return purchaseRequestNumber;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public BigDecimal getAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (PurchRequisition pr : purchRequisitions) {
            for (PurchaseItem item : pr.getPurchaseItems()) {
                if (item.getAmount() != null && item.getQuantity() != null) {
                    amount = amount.add(item.getAmount().multiply(item.getQuantity()));
                }
            }
        }

        return amount;
    }

    @Transient
    public List<PurchaseItem> getPurchaseItems() {
        return purchRequisitions.stream().flatMap(pr -> pr.getPurchaseItems().stream()).collect(Collectors.toList());
    }

    public Set<Tender> getTender() {
        return tender;
    }

    public void setTender(Set<Tender> tender) {
        this.tender = tender;
    }

    public void addTender(final Tender item) {
        tender.add(item);
        item.setTenderProcess(this);
    }

    public void removeTender(final Tender item) {
        tender.remove(item);
        item.setTenderProcess(null);
    }

    public void removeAdministratorReport(final AdministratorReport item) {
        administratorReports.remove(item);
        item.setTenderProcess(null);
    }

    public void removeInspectonReport(final InspectionReport item) {
        inspectionReports.remove(item);
        item.setTenderProcess(null);
    }

    public void removePMCReport(final PMCReport item) {
        pmcReports.remove(item);
        item.setTenderProcess(null);
    }

    public void removeMEReport(final MEReport item) {
        meReports.remove(item);
        item.setTenderProcess(null);
    }

    public void removePaymentVoucher(final PaymentVoucher item) {
        paymentVouchers.remove(item);
        item.setTenderProcess(null);
    }

    public Set<TenderQuotationEvaluation> getTenderQuotationEvaluation() {
        return tenderQuotationEvaluation;
    }

    public void setTenderQuotationEvaluation(Set<TenderQuotationEvaluation> tenderQuotationEvaluation) {
        this.tenderQuotationEvaluation = tenderQuotationEvaluation;
    }

    public void addTenderQuotationEvaluation(final TenderQuotationEvaluation item) {
        tenderQuotationEvaluation.add(item);
        item.setTenderProcess(this);
    }

    public void removeTenderQuotationEvaluation(final TenderQuotationEvaluation item) {
        tenderQuotationEvaluation.remove(item);
        item.setTenderProcess(null);
    }

    public Set<ProfessionalOpinion> getProfessionalOpinion() {
        return professionalOpinion;
    }

    public void setProfessionalOpinion(Set<ProfessionalOpinion> professionalOpinion) {
        this.professionalOpinion = professionalOpinion;
    }

    public void addProfessionalOpinion(final ProfessionalOpinion item) {
        professionalOpinion.add(item);
        item.setTenderProcess(this);
    }

    public void removeProfessionalOpinion(final ProfessionalOpinion item) {
        professionalOpinion.remove(item);
        item.setTenderProcess(null);
    }

    public Set<AwardNotification> getAwardNotification() {
        return awardNotification;
    }

    public void setAwardNotification(Set<AwardNotification> awardNotification) {
        this.awardNotification = awardNotification;
    }

    public void addAwardNotification(final AwardNotification item) {
        awardNotification.add(item);
        item.setTenderProcess(this);
    }

    public void removeAwardNotification(final AwardNotification item) {
        awardNotification.remove(item);
        item.setTenderProcess(null);
    }

    public Set<AwardAcceptance> getAwardAcceptance() {
        return awardAcceptance;
    }

    public void setAwardAcceptance(Set<AwardAcceptance> awardAcceptance) {
        this.awardAcceptance = awardAcceptance;
    }

    public void addAwardAcceptance(final AwardAcceptance item) {
        awardAcceptance.add(item);
        item.setTenderProcess(this);
    }

    public void addAdministratorReport(final AdministratorReport item) {
        administratorReports.add(item);
        item.setTenderProcess(this);
    }

    public void addPMCReport(final PMCReport item) {
        pmcReports.add(item);
        item.setTenderProcess(this);
    }

    public void addMEReport(final MEReport item) {
        meReports.add(item);
        item.setTenderProcess(this);
    }

    public void addPaymentVoucher(final PaymentVoucher item) {
        paymentVouchers.add(item);
        item.setTenderProcess(this);
    }

    public void addInspectionReport(final InspectionReport item) {
        inspectionReports.add(item);
        item.setTenderProcess(this);
    }

    public void removeAwardAcceptance(final AwardAcceptance item) {
        awardAcceptance.remove(item);
        item.setTenderProcess(null);
    }

    public Set<Contract> getContract() {
        return contract;
    }

    public void setContract(Set<Contract> contract) {
        this.contract = contract;
    }

    public void addContract(final Contract item) {
        contract.add(item);
        item.setTenderProcess(this);
    }

    public void removeContract(final Contract item) {
        contract.remove(item);
        item.setTenderProcess(null);
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public ProcurementPlan getProcurementPlan() {
        if (project != null) {
            return project.getProcurementPlan();
        }
        return null;
    }

    @Override
    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    protected Collection<AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.singletonList(PersistenceUtil.getNext(tender));
    }

    @Override
    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public Department getDepartment() {
        return getProcurementPlan().getDepartment();
    }

    public List<PurchRequisition> getPurchRequisitions() {
        return purchRequisitions;
    }

    public void setPurchRequisitions(List<PurchRequisition> purchRequisitions) {
        this.purchRequisitions = purchRequisitions;
    }

    public Set<AdministratorReport> getAdministratorReports() {
        return administratorReports;
    }

    public void setAdministratorReports(Set<AdministratorReport> administratorReports) {
        this.administratorReports = administratorReports;
    }

    public Set<PMCReport> getPmcReports() {
        return pmcReports;
    }

    public void setPmcReports(Set<PMCReport> pmcReports) {
        this.pmcReports = pmcReports;
    }

    public Set<MEReport> getMeReports() {
        return meReports;
    }

    public void setMeReports(Set<MEReport> meReports) {
        this.meReports = meReports;
    }

    public Set<InspectionReport> getInspectionReports() {
        return inspectionReports;
    }

    public void setInspectionReports(Set<InspectionReport> inspectionReports) {
        this.inspectionReports = inspectionReports;
    }

    public Set<PaymentVoucher> getPaymentVouchers() {
        return paymentVouchers;
    }

    public void setPaymentVouchers(Set<PaymentVoucher> paymentVouchers) {
        this.paymentVouchers = paymentVouchers;
    }
}
