package org.devgateway.toolkit.persistence.validator.validators;

import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.service.form.TenderProcessEntityServiceResolver;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Octavian Ciubotaru
 */
public class UniqueTenderProcessEntityConstraintValidator
        implements ConstraintValidator<UniqueTenderProcessEntity, AbstractTenderProcessMakueniEntity> {

    @Autowired
    private TenderProcessEntityServiceResolver tenderProcessEntityServiceResolver;

    @Override
    public boolean isValid(AbstractTenderProcessMakueniEntity entity, ConstraintValidatorContext context) {
        if (entity == null) {
            return true;
        }

        return tenderProcessEntityServiceResolver.countByTenderProcess(entity) == 0;
    }
}
