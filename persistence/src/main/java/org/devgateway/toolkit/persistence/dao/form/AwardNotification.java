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
import org.springframework.util.ObjectUtils;

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
import java.util.Optional;
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
@Form(featureName = "awardNotificationForm")
@UniqueTenderProcessEntity(groups = HighLevel.class, payload = Severity.NonRecoverable.class,
        message = "{org.devgateway.toolkit.persistence.dao.form.UniqueAwardNotification.message}")
public class AwardNotification extends AbstractTenderProcessMakueniEntity {

    @ExcelExport(name = "Award Notifications", separateSheet = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<AwardNotificationItem> items = new ArrayList<>();


    public List<Supplier> getAwardee() {
        return items.stream().map(AwardNotificationItem::getAwardee).filter(Objects::nonNull).
                collect(Collectors.toList());
    }


    @JsonIgnore
    public AwardNotificationItem getAcceptedNotification() {
        final AwardAcceptance awardAcceptance = getTenderProcess().getSingleAwardAcceptance();
        if (ObjectUtils.isEmpty(awardAcceptance) || ObjectUtils.isEmpty(awardAcceptance.getItems())) {
            return null;
        }
        Optional<AwardAcceptanceItem> accepted = awardAcceptance.getItems()
                .stream()
                .filter(AwardAcceptanceItem::isAccepted)
                .findFirst();

        if (accepted.isPresent()) {
            return this.getItems().stream().filter(i -> i.getAwardee() != null && i.getAwardee()
                    .equals(accepted.get().getAwardee()))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return "Award notifications for tender process " + getTenderProcessNotNull().getLabel();
    }

    @Override
    @Transactional
    protected Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.singletonList(PersistenceUtil.getNext(getTenderProcessNotNull().getAwardAcceptance()));
    }

    public List<AwardNotificationItem> getItems() {
        return items;
    }

    public void setItems(List<AwardNotificationItem> items) {
        this.items = items;
    }

    @Override
    public Class<?> getNextForm() {
        return AwardAcceptance.class;
    }

    @Override
    public boolean hasDownstreamForms() {
        return getTenderProcess().hasFormsDependingOnAwardNotification();
    }
}
