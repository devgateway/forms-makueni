package org.devgateway.toolkit.web.rest.controller.alerts.processsing;

import org.devgateway.toolkit.persistence.dao.alerts.AlertsStatistics;
import org.devgateway.toolkit.web.rest.controller.alerts.exception.AlertsProcessingException;

/**
 * @author idobre
 * @since 26/08/2019
 */
public interface AlertsManager {

    /**
     * Send all alerts.
     */
    void sendAlerts();

    /**
     * Process this alert and send an email (if necessary).
     */
    AlertsStatistics processAlert(Long alertId) throws AlertsProcessingException;
}
