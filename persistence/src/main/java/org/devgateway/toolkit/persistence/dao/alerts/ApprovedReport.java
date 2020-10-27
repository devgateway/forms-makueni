package org.devgateway.toolkit.persistence.dao.alerts;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.devgateway.toolkit.persistence.dao.AbstractAuditableEntity;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

/**
 * @author Octavian Ciubotaru
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
public class ApprovedReport extends AbstractAuditableEntity {

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private MEReport meReport;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private PMCReport pmcReport;

    private Boolean processed = false;

    public MEReport getMeReport() {
        return meReport;
    }

    public void setMeReport(MEReport meReport) {
        this.meReport = meReport;
    }

    public PMCReport getPmcReport() {
        return pmcReport;
    }

    public void setPmcReport(PMCReport pmcReport) {
        this.pmcReport = pmcReport;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public AbstractAuditableEntity getParent() {
        return null;
    }
}
