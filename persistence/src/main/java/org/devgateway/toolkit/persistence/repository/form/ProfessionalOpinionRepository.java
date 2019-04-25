package org.devgateway.toolkit.persistence.repository.form;

import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author idobre
 * @since 2019-04-24
 */
@Transactional
public interface ProfessionalOpinionRepository extends BaseJpaRepository<ProfessionalOpinion, Long> {

}
