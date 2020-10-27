package org.devgateway.toolkit.persistence.service.form;


import org.devgateway.toolkit.persistence.dao.alerts.ApprovedReport;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.repository.alerts.ApprovedReportRepository;
import org.devgateway.toolkit.persistence.repository.form.MEReportRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author mpostelnicu
 */
@Service
@Transactional
public class MEReportServiceImpl extends AbstractMakueniEntityServiceImpl<MEReport>
        implements MEReportService {

    @Autowired
    private MEReportRepository repository;

    @Autowired
    private ApprovedReportRepository approvedReportRepository;

    @Override
    protected BaseJpaRepository<MEReport, Long> repository() {
        return repository;
    }

    @Override
    public MEReport newInstance() {
        return new MEReport();
    }

    @Override
    public List<MEReport> findByTenderProcess(final TenderProcess tenderProcess) {
        return repository.findByTenderProcess(tenderProcess);
    }

    @Override
    public void onApproved(MEReport report) {
        ApprovedReport approvedReport = new ApprovedReport();
        approvedReport.setMeReport(report);

        approvedReportRepository.save(approvedReport);
    }
}
