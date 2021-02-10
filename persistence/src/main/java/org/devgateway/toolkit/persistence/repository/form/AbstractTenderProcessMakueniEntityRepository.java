package org.devgateway.toolkit.persistence.repository.form;


import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface AbstractTenderProcessMakueniEntityRepository<T extends AbstractTenderProcessMakueniEntity>
        extends AbstractMakueniEntityRepository<T> {
    T findByTenderProcess(@Param("tenderProcess") TenderProcess tenderProcess);

    @Override
    @Query("select c from  #{#entityName} c "
            + " where c.tenderProcess.procurementPlan.fiscalYear = :fiscalYear")
    List<T> findByFiscalYear(@Param("fiscalYear") FiscalYear fiscalYear);

    @Query("select count(c) "
            + "from #{#entityName} c "
            + "where c.id <> :id "
            + "and c.tenderProcess = :tenderProcess")
    long countByTenderProcess(
            @Param("id") Long id,
            @Param("tenderProcess") TenderProcess tenderProcess);
}
