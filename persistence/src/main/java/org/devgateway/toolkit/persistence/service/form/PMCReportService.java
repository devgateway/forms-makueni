package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.service.TextSearchableService;

import java.util.Collection;
import java.util.List;

/**
 * @author mpostelnicu
 */
public interface PMCReportService extends AbstractImplTenderProcessMakueniEntityService<PMCReport>,
        TextSearchableService<PMCReport> {

    List<PMCReport> getPMCReportsForDepartments(Collection<Department> departments);

    PMCReport saveReportAndUpdateTenderProcess(PMCReport entity);

    void onApproved(PMCReport report);
}
