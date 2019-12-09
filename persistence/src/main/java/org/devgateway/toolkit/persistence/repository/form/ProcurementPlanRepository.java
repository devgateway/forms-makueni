package org.devgateway.toolkit.persistence.repository.form;

import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

/**
 * @author idobre
 * @since 2019-04-02
 */
@Transactional
public interface ProcurementPlanRepository extends AbstractMakueniEntityRepository<ProcurementPlan> {

    @Override
    @Query("select proc from  #{#entityName} proc where lower(proc.department.label) "
            + " like %:name% or lower(proc.fiscalYear.name) like %:name%")
    Page<ProcurementPlan> searchText(@Param("name") String name, Pageable page);

    Long countByDepartmentAndFiscalYear(Department department, FiscalYear fiscalYear);

    ProcurementPlan findByDepartmentAndFiscalYear(Department department, FiscalYear fiscalYear);

    @Query("select p from ProcurementPlan p")
    Stream<ProcurementPlan> findAllStream();
}
