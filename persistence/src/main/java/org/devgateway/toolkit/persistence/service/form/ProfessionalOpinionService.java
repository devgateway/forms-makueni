package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.ProfessionalOpinion;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;

/**
 * @author idobre
 * @since 2019-04-24
 */
public interface ProfessionalOpinionService extends AbstractTenderProcessEntityService<ProfessionalOpinion> {

    ProfessionalOpinion findByTenderProcess(TenderProcess tenderProcess);

}
