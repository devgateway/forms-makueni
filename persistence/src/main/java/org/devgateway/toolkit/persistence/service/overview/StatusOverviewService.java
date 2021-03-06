package org.devgateway.toolkit.persistence.service.overview;

import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dto.StatusOverviewRowGroup;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * @author gmutuhu
 */
public interface StatusOverviewService {
    List<StatusOverviewRowGroup> getAllProjects(FiscalYear fiscalYear, String projectTitle);

    List<StatusOverviewRowGroup> getDisplayableTenderProcesses(FiscalYear fiscalYear, String title);

    Long countProjects(FiscalYear fiscalYear, String projectTitle);

    Long countTenderProcesses(FiscalYear fiscalYear, String title);

    Specification<TenderProcess> getTenderProcessViewSpecification(Department department,
                                                                   FiscalYear fiscalYear, String title);
}
