package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "tender_process_id")}, uniqueConstraints =
@UniqueConstraint(columnNames = "tender_process_id"))
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form(featureName = "awardAcceptanceForm")
@UniqueTenderProcessEntity(groups = HighLevel.class, payload = Severity.NonRecoverable.class,
        message = "{org.devgateway.toolkit.persistence.dao.form.UniqueAwardAcceptance.message}")
public class AwardAcceptance extends AbstractTenderProcessMakueniEntity {

    @ExcelExport(name = "Award Acceptances", separateSheet = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<AwardAcceptanceItem> items = new ArrayList<>();

    @Override
    public void setLabel(final String label) {
    }

    public boolean hasAccepted() {
        return items.stream().map(AwardAcceptanceItem::isAccepted).filter(a -> a).findFirst().orElse(false);
    }

    public AwardAcceptanceItem getAcceptedAcceptance() {
        return getItems().stream().filter(AwardAcceptanceItem::isAccepted).findFirst().orElse(null);
    }

    public List<Supplier> getAwardee() {
        return items.stream()
                .map(AwardAcceptanceItem::getAwardee)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return "Award acceptance for tender process " + getTenderProcessNotNull().getLabel();
    }

    @Override
    @Transactional
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.singletonList(PersistenceUtil.getNext(getTenderProcessNotNull().getContract()));
    }

    public List<AwardAcceptanceItem> getItems() {
        return items;
    }

    public void setItems(List<AwardAcceptanceItem> items) {
        this.items = items;
    }

    @Override
    public Class<?> getNextForm() {
        return hasAccepted() ? Contract.class : ProfessionalOpinion.class;
    }

    @Override
    public boolean hasDownstreamForms() {
        return getTenderProcess().hasFormsDependingOnAwardAcceptance();
    }
}
