package org.devgateway.toolkit.persistence.service.form;

import com.google.common.collect.ImmutableMap;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.CabinetPaper;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Transactional
public class MakeniEntityServiceResolverImpl implements MakueniEntityServiceResolver {

    @Autowired
    private TenderProcessEntityServiceResolver tenderProcessEntityServiceResolver;

    @Autowired
    private ProcurementPlanService procurementPlanService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CabinetPaperService cabinetPaperService;

    private Map<Class<?>, AbstractMakueniEntityService<?>> serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = ImmutableMap.<Class<?>, AbstractMakueniEntityService<?>>builder()
                .put(Project.class, projectService)
                .put(ProcurementPlan.class, procurementPlanService)
                .put(CabinetPaper.class, cabinetPaperService)
                .build();
    }

    public <S extends AbstractMakueniEntity> S saveAndFlushMakueniEntity(S entity) {
        Class<? extends AbstractMakueniEntity> aClass = entity.getClass();
        if (AbstractTenderProcessMakueniEntity.class.isAssignableFrom(aClass)) {
            return (S) tenderProcessEntityServiceResolver.saveAndFlush((AbstractTenderProcessMakueniEntity) entity);
        } else {
            AbstractMakueniEntityService service = serviceMap.get(aClass);
            return (S) service.saveAndFlush(entity);
        }
    }
}
