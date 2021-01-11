package org.devgateway.toolkit.persistence.repository.form;

import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 *
 */
@Transactional
public interface TenderRepository extends AbstractTenderProcessMakueniEntityRepository<Tender> {
    @Override
    @Query("select tender from  #{#entityName} tender where lower(tender.tenderTitle) like %:name%")
    Page<Tender> searchText(@Param("name") String name, Pageable page);
}
