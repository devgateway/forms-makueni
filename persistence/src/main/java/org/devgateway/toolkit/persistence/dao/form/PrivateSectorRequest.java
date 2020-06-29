package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.AbstractChildExpandableAuditEntity;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.ListViewItem;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mpostelnicu
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
@Table(indexes = {@Index(columnList = "parent_id")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivateSectorRequest extends AbstractChildExpandableAuditEntity<InspectionReport> implements ListViewItem {

    @ExcelExport(justExport = true, useTranslation = true, name = "Private Sector Request Upload")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FileMetadata> upload = new HashSet<>();

    @ExcelExport(useTranslation = true)
    private Date requestDate;

    public Set<FileMetadata> getUpload() {
        return upload;
    }

    public void setUpload(Set<FileMetadata> upload) {
        this.upload = upload;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
}
