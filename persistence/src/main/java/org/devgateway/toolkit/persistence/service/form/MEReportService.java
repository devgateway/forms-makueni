package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.MEReport;

/**
 * @author mpostelnicu
 */
public interface MEReportService extends AbstractImplTenderProcessMakueniEntityService<MEReport> {

    void onApproved(MEReport report);
}
