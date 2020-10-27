package org.devgateway.toolkit.persistence.repository.alerts;

import java.util.List;

import org.devgateway.toolkit.persistence.dao.alerts.ApprovedReport;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;

/**
 * @author Octavian Ciubotaru
 * @since 12/10/2020
 */
public interface ApprovedReportRepository extends BaseJpaRepository<ApprovedReport, Long> {

    List<ApprovedReport> findByProcessedIsFalse();
}
