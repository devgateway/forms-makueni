package org.devgateway.toolkit.forms.service;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.service.BaseJpaService;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.category.FiscalYearService;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.persistence.service.form.ProjectService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;

import static org.devgateway.toolkit.forms.WebConstants.ALL_SESSION_KEYS;
import static org.devgateway.toolkit.forms.WebConstants.DEPARTMENT;
import static org.devgateway.toolkit.forms.WebConstants.FISCAL_YEAR;
import static org.devgateway.toolkit.forms.WebConstants.PROCUREMENT_PLAN;
import static org.devgateway.toolkit.forms.WebConstants.PROJECT;
import static org.devgateway.toolkit.forms.WebConstants.PURCHASE_REQUISITION;

/**
 * @author idobre
 * @since 2019-05-15
 */
@Service
public class SessionMetadataService {


    @Autowired
    private ProcurementPlanService procurementPlanService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private FiscalYearService fiscalYearService;



    private void setSessionKey(final MetaDataKey<Long> key, final Long persistableId) {
        final Session session = Session.get();
        if (session != null && persistableId != null) {
            session.setMetaData(key, persistableId);
        } else {
            if (session != null) {
                session.setMetaData(key, null);
            }
        }
    }

    private void setSessionKey(final MetaDataKey<Long> key, final GenericPersistable persistable) {
        final Session session = Session.get();
        if (session != null && persistable != null) {
            session.setMetaData(key, persistable.getId());
        } else {
            if (session != null) {
                session.setMetaData(key, null);
            }
        }
    }

    private <S extends GenericPersistable & Serializable> S getSessionPersistable(final MetaDataKey<Long> key,
                                                                                  final BaseJpaService<S> service) {
        final Session session = Session.get();
        if (session != null) {
            final Long objId = session.getMetaData(key);
            if (objId != null) {
                Optional<S> object = service.findById(objId);
                if (object.isPresent()) {
                    return object.get();
                }
                //remove entry completely
                session.setMetaData(key, null);
                return null;
            }
        }
        return null;
    }

    public void setSessionPP(final ProcurementPlan object) {
        setSessionKey(PROCUREMENT_PLAN, object);
    }


    public ProcurementPlan getSessionPP() {
        return getSessionPersistable(PROCUREMENT_PLAN, procurementPlanService);
    }

    public void setSessionProject(final Project object) {
        setSessionKey(PROJECT, object);
    }

    public void setSessionProjectId(Long id) {
        setSessionKey(PROJECT, id);
    }

    public void setSessionTenderProcessId(Long id) {
        setSessionKey(PURCHASE_REQUISITION, id);
    }


    public Project getSessionProject() {
        return getSessionPersistable(PROJECT, projectService);
    }

    public void setSessionTenderProcess(final TenderProcess object) {
        setSessionKey(PURCHASE_REQUISITION, object);
    }

    public TenderProcess getSessionTenderProcess() {
        return getSessionPersistable(PURCHASE_REQUISITION, tenderProcessService);
    }

    public void setSessionDepartment(final Department object) {
        setSessionKey(DEPARTMENT, object);
    }

    public Department getSessionDepartment() {
        return getSessionPersistable(DEPARTMENT, departmentService);
    }

    public void setSessionFiscalYear(final FiscalYear object) {
        setSessionKey(FISCAL_YEAR, object);
    }

    public FiscalYear getSessionFiscalYear() {
        return getSessionPersistable(FISCAL_YEAR, fiscalYearService);
    }

    public static void clearSessionData() {
        final Session session = Session.get();
        if (session != null) {
            ALL_SESSION_KEYS.forEach(key -> session.setMetaData(key, null));
        }
    }
}
