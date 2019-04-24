package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.repository.form.ProfessionalOpinionRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author idobre
 * @since 2019-04-24
 */
@Service
@Transactional(readOnly = true)
public class ProfessionalOpinionServiceImpl extends BaseJpaServiceImpl<ProfessionalOpinion>
        implements ProfessionalOpinionService {

    @Autowired
    private ProfessionalOpinionRepository professionalOpinionRepository;

    @Override
    protected BaseJpaRepository<ProfessionalOpinion, Long> repository() {
        return professionalOpinionRepository;
    }

    @Override
    public ProfessionalOpinion newInstance() {
        return new ProfessionalOpinion();
    }

}
