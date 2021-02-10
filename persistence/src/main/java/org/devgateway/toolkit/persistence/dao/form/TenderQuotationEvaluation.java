package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.Form;
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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
@Form(featureName = "tenderQuotationEvaluationForm")
@UniqueTenderProcessEntity(groups = HighLevel.class, payload = Severity.NonRecoverable.class,
        message = "{org.devgateway.toolkit.persistence.dao.form.UniqueTenderQuotationEvaluation.message}")
public class TenderQuotationEvaluation extends AbstractTenderProcessMakueniEntity {
    @ExcelExport(useTranslation = true, name = "Closing Date")
    private Date closingDate;

    @ExcelExport(separateSheet = true, useTranslation = true, name = "Bids")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<Bid> bids = new ArrayList<>();

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(final Date closingDate) {
        this.closingDate = closingDate;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(final List<Bid> bids) {
        this.bids = bids;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return "Tender Quotation Evaluation for tender process " + getTenderProcessNotNull().getLabel();
    }

    @Override
    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.singletonList(PersistenceUtil.getNext(getTenderProcessNotNull()
                .getProfessionalOpinion()));
    }

    @Override
    public Class<?> getNextForm() {
        return ProfessionalOpinion.class;
    }

    @Override
    public boolean hasDownstreamForms() {
        return getTenderProcess().hasFormsDependingOnTenderQuotationAndEvaluation();
    }
}
