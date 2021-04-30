package org.devgateway.toolkit.persistence.dao;

import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinionItem;
import org.devgateway.toolkit.persistence.dao.form.PurchRequisition;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.Set;

/**
 * @author mpostelnicu
 */
@MappedSuperclass
@Audited
public abstract class AbstractDocsChildExpAuditEntity<P extends AbstractAuditableEntity> extends
        AbstractChildExpandableAuditEntity<P> {

    @ExcelExport(justExport = true, useTranslation = true, name = "Documents")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FileMetadata> formDocs;

    @ExcelExport(useTranslation = true, name = "Approved Date",
            onlyForClass = { PurchRequisition.class, ProfessionalOpinionItem.class })
    private Date approvedDate;

    public Set<FileMetadata> getFormDocs() {
        return formDocs;
    }

    public void setFormDocs(Set<FileMetadata> formDocs) {
        this.formDocs = formDocs;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }
}
