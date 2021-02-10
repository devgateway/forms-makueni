package org.devgateway.toolkit.persistence.service.form;

import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.TextSearchableService;

/**
 * @author gmutuhu
 *
 */
public interface TenderQuotationEvaluationService
        extends AbstractTenderProcessEntityService<TenderQuotationEvaluation>,
        TextSearchableService<TenderQuotationEvaluation> {
    TenderQuotationEvaluation findByTenderProcess(TenderProcess tenderProcess);
}
