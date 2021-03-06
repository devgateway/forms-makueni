package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.Form;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.persistence.validator.validators.MaxAttachedFiles;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Collections;

/**
 * @author gmutuhu
 */

@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "procurement_plan_id"),
        @Index(columnList = "number"),
        @Index(columnList = "name")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Form(featureName = "cabinetPaperForm")
public class CabinetPaper extends AbstractMakueniEntity implements ProcurementPlanAttachable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procurement_plan_id")
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    private ProcurementPlan procurementPlan;

    @ExcelExport(useTranslation = true, name = "Name")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String name;

    @ExcelExport(useTranslation = true, name = "Cabinet/Ministerial Paper Number")
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String number;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    @Override
    public void setLabel(final String label) {

    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public ProcurementPlan getProcurementPlan() {
        return procurementPlan;
    }

    public void setProcurementPlan(ProcurementPlan procurementPlan) {
        this.procurementPlan = procurementPlan;
    }

    @Override
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return name;
    }

    @Override
    public String toString() {
        return getLabel();
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
    public Department getDepartment() {
        return null;
    }

    @Override
    @JsonIgnore
    public boolean isExportable() {
        return true;
    }

    @MaxAttachedFiles
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public Collection<FileMetadata> getAllAttachedFiles() {
        return getFormDocs();
    }
}
