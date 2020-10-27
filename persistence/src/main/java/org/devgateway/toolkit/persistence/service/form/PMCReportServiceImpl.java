package org.devgateway.toolkit.persistence.service.form;


import java.util.Collection;
import java.util.List;

import org.devgateway.toolkit.persistence.dao.alerts.ApprovedReport;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport_;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan_;
import org.devgateway.toolkit.persistence.dao.form.Project_;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess_;
import org.devgateway.toolkit.persistence.repository.alerts.ApprovedReportRepository;
import org.devgateway.toolkit.persistence.repository.form.PMCReportRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.repository.norepository.TextSearchableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mpostelnicu
 */
@Service
@Transactional
public class PMCReportServiceImpl extends AbstractMakueniEntityServiceImpl<PMCReport>
        implements PMCReportService {

    @Autowired
    private PMCReportRepository repository;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private ApprovedReportRepository approvedReportRepository;

    @Override
    protected BaseJpaRepository<PMCReport, Long> repository() {
        return repository;
    }

    @Override
    public PMCReport newInstance() {
        return new PMCReport();
    }

    @Override
    public List<PMCReport> findByTenderProcess(final TenderProcess tenderProcess) {
        return repository.findByTenderProcess(tenderProcess);
    }

    @Override
    public TextSearchableRepository<PMCReport, Long> textRepository() {
        return repository;
    }

    @Override
    public List<PMCReport> getPMCReportsForDepartments(Collection<Department> deps) {
        return repository.findAll((r, cq, cb) -> cb.and(
                r.join(PMCReport_.tenderProcess).join(TenderProcess_.project).join(Project_.procurementPlan)
                        .join(ProcurementPlan_.department).in(deps)
        ));
    }

    @Override
    public PMCReport saveReportAndUpdateTenderProcess(PMCReport entity) {
        TenderProcess tenderProcess = entity.getTenderProcess();
        tenderProcess.addPMCReport(entity);
        tenderProcessService.save(tenderProcess);
        return save(entity);
    }

    @Override
    public void onApproved(PMCReport report) {
        ApprovedReport approvedReport = new ApprovedReport();
        approvedReport.setPmcReport(report);

        approvedReportRepository.save(approvedReport);
    }
}
