package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.repository.form.ProjectRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author idobre
 * @since 2019-04-02
 */
@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl extends BaseJpaServiceImpl<Project> implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    protected BaseJpaRepository<Project, Long> repository() {
        return projectRepository;
    }

    @Override
    public Project newInstance() {
        return new Project();
    }

    @Cacheable
    @Override
    public Long countByProcurementPlanAndProjectTitleAndIdNot(final ProcurementPlan procurementPlan,
                                                      final String projectTitle,
                                                      final Long id) {
        return projectRepository.countByProcurementPlanAndProjectTitleAndIdNot(procurementPlan, projectTitle, id);
    }
}
