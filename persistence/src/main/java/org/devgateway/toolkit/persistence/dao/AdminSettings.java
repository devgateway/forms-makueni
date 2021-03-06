package org.devgateway.toolkit.persistence.dao;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

/**
 * @author idobre
 * @since 6/22/16
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AdminSettings extends AbstractAuditableEntity {
    private static final long serialVersionUID = -1051140524022133178L;

    private Integer excelBatchSize;

    private Boolean rebootServer = false;

    private Boolean disableEmailAlerts = false;

    private String adminEmail = null;

    private Boolean enableDailyAutomatedImport = false;

    private String importFilesPath = null;

    private Integer daysSubmittedReminder = 14;

    private String superAdminEmail;

    /**
     * This disables the security of /api/ endpoints, should be used for demo purposes only
     */
    private Boolean disableApiSecurity = false;

    private Integer autosaveTime;

    private Boolean emailNotification = false;

    private Integer unlockAfterHours;

    @Override
    public AbstractAuditableEntity getParent() {
        return null;
    }

    public Integer getExcelBatchSize() {
        return excelBatchSize;
    }

    public void setExcelBatchSize(final Integer excelBatchSize) {
        this.excelBatchSize = excelBatchSize;
    }

    public Boolean getRebootServer() {
        return rebootServer;
    }

    public void setRebootServer(final Boolean rebootServer) {
        this.rebootServer = rebootServer;
    }

    public Integer getAutosaveTime() {
        return autosaveTime;
    }

    public void setAutosaveTime(final Integer autosaveTime) {
        this.autosaveTime = autosaveTime;
    }

    public Boolean getEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(final Boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public Boolean getDisableApiSecurity() {
        return disableApiSecurity;
    }

    public void setDisableApiSecurity(Boolean disableApiSecurity) {
        this.disableApiSecurity = disableApiSecurity;
    }


    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public Boolean getEnableDailyAutomatedImport() {
        return enableDailyAutomatedImport;
    }

    public void setEnableDailyAutomatedImport(Boolean enableDailyAutomatedImport) {
        this.enableDailyAutomatedImport = enableDailyAutomatedImport;
    }

    public String getImportFilesPath() {
        return importFilesPath;
    }

    public void setImportFilesPath(String importFilesPath) {
        this.importFilesPath = importFilesPath;
    }

    public Boolean getDisableEmailAlerts() {
        return disableEmailAlerts;
    }

    public void setDisableEmailAlerts(Boolean disableEmailAlerts) {
        this.disableEmailAlerts = disableEmailAlerts;
    }

    public Integer getDaysSubmittedReminder() {
        return daysSubmittedReminder;
    }

    public void setDaysSubmittedReminder(Integer daysSubmittedReminder) {
        this.daysSubmittedReminder = daysSubmittedReminder;
    }

    public String getSuperAdminEmail() {
        return superAdminEmail;
    }

    public void setSuperAdminEmail(String superAdminEmail) {
        this.superAdminEmail = superAdminEmail;
    }

    public Integer getUnlockAfterHours() {
        return unlockAfterHours;
    }

    public void setUnlockAfterHours(Integer unlockAfterHours) {
        this.unlockAfterHours = unlockAfterHours;
    }
}
