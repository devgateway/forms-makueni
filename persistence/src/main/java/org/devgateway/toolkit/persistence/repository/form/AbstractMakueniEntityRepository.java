package org.devgateway.toolkit.persistence.repository.form;


import org.devgateway.toolkit.persistence.dao.Person;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.repository.norepository.TextSearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

@NoRepositoryBean
public interface AbstractMakueniEntityRepository<T extends AbstractMakueniEntity>
        extends TextSearchableRepository<T, Long> {
    List<T> findByFiscalYear(FiscalYear fiscalYear);

    @Override
    @Query("select c from  #{#entityName} c")
    Page<T> searchText(@Param("name") String name, Pageable page);

    Stream<T> findByStatus(String status);

    @Query("select c "
            + "from #{#entityName} c "
            + "where c.owner = :person")
    List<T> getAllLocked(@Param("person") Person person);

    @Query("select c "
            + "from #{#entityName} c "
            + "where c.owner is not null")
    List<T> getAllLocked();
}
