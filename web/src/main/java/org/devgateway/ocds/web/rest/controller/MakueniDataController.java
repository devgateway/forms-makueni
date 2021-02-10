package org.devgateway.ocds.web.rest.controller;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.devgateway.ocds.persistence.mongo.repository.main.ProcurementPlanMongoRepository;
import org.devgateway.ocds.web.convert.MongoFileStorageService;
import org.devgateway.ocds.web.rest.controller.request.MakueniFilterPagingRequest;
import org.devgateway.toolkit.persistence.dao.categories.Category;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.Item;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomOperation;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomSortingOperation;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.persistence.service.category.ItemService;
import org.devgateway.toolkit.persistence.service.category.SubcountyService;
import org.devgateway.toolkit.persistence.service.category.WardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author idobre
 * @since 2019-07-12
 */
@RestController
@CacheConfig(keyGenerator = "genericKeyGenerator", cacheNames = "genericPagingRequestJson")
public class MakueniDataController extends GenericOCDSController {
    private static final Logger logger = LoggerFactory.getLogger(MakueniDataController.class);

    @Autowired
    private ProcurementPlanMongoRepository procurementPlanMongoRepository;

    @Autowired
    private MongoFileStorageService mongoFileStorageService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SubcountyService subcountyService;

    @Autowired
    private WardService wardService;

    @Autowired
    private GridFsOperations gridFsOperations;

    @Resource
    private MakueniDataController self; // Self-autowired reference to proxified bean of this class.

    private List<AggregationOperation> makueniTenderOperations(MakueniFilterPagingRequest filter) {
        final Criteria criteria = new Criteria().andOperator(
                createFilterCriteria("department._id", filter.getDepartment()),
                createFilterCriteria("fiscalYear._id", filter.getFiscalYear()));

        final Criteria criteriaTender = new Criteria().andOperator(
                where("tender.tenderTitle").exists(true),
                createFilterCriteria("project.subcounties._id", filter.getSubcounty()),
                createFilterCriteria("project.wards._id", filter.getWard()),
                createFilterCriteria(
                        "tender.tenderItems.purchaseItem.planItem.item._id",
                        filter.getItem()
                ),
                createRangeFilterCriteria("tender.tenderValue",
                        filter.getMin(), filter.getMax()
                ),
                createTextCriteria(filter.getText()),
                getYearFilterCriteria(filter, "tender.closingDate")
        );

        List<AggregationOperation> operations = Lists.newArrayList(
                match(criteria),
                unwind("tenderProcesses"),
                unwind("tenderProcesses.tender"),
                project("tenderProcesses._id", "department", "fiscalYear", "tenderProcesses.tender.tenderTitle",
                        "tenderProcesses.project", "tenderProcesses.tender.closingDate", "tenderProcesses.tender._id",
                        "tenderProcesses.tender.tenderValue", "tenderProcesses.tender.formDocs",
                        "tenderProcesses.tender.tenderItems"),
                match(criteriaTender));
        return operations;
    }

    @ApiOperation(value = "Fetch Makueni Tenders")
    @RequestMapping(value = "/api/makueni/tenders",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    @Cacheable
    public List<Document> makueniTenders(@ModelAttribute @Valid final MakueniFilterPagingRequest filter) {
        List<AggregationOperation> aggregationOperations = makueniTenderOperations(filter);
        aggregationOperations.addAll(Arrays.asList(
                sort(Sort.Direction.DESC, "tender.closingDate"),
                skip(filter.getSkip()),
                limit(filter.getPageSize()))
        );
        return mongoTemplate.aggregate(newAggregation(aggregationOperations).withOptions(makueniAggOptions()),
                "procurementPlan", Document.class).getMappedResults();
    }

    private AggregationOptions makueniAggOptions() {
        return Aggregation.newAggregationOptions().allowDiskUse(true).build();
    }

    @ApiOperation(value = "Counts Makueni Tenders")
    @RequestMapping(value = "/api/makueni/tendersCount",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    @Cacheable
    public Integer makueniTendersCount(@ModelAttribute @Valid final MakueniFilterPagingRequest filter) {
        List<AggregationOperation> aggregationOperations = makueniTenderOperations(filter);
        aggregationOperations.add(group().count().as("count"));

        Document doc = mongoTemplate.aggregate(newAggregation(aggregationOperations)
                .withOptions(makueniAggOptions()), "procurementPlan", Document.class).getUniqueMappedResult();

        return doc == null ? 0 : (Integer) doc.get("count");
    }

    private List<AggregationOperation> makueniPlanOperations(MakueniFilterPagingRequest filter) {

        final Criteria criteria = new Criteria().andOperator(
                createFilterCriteria("department._id", filter.getDepartment()),
                createFilterCriteria("fiscalYear._id", filter.getFiscalYear()));

        List<AggregationOperation> operations = Lists.newArrayList(
                match(criteria),
                project("formDocs", "department", "fiscalYear", "status", "approvedDate")
        );

        return operations;
    }

    @ApiOperation(value = "Fetch Makueni Procurement Plans")
    @RequestMapping(value = "/api/makueni/procurementPlans",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    @Cacheable
    public List<ProcurementPlan> makueniProcurementPlans(
            @ModelAttribute @Valid final MakueniFilterPagingRequest filter) {
        List<AggregationOperation> aggregationOperations = makueniPlanOperations(filter);

        BasicDBObject customSorting = new BasicDBObject();
        customSorting.put("fiscalYear.startDate", -1);
        customSorting.put("department.label", 1);

        aggregationOperations.addAll(Arrays.asList(
                new CustomSortingOperation(customSorting), skip(filter.getSkip()), limit(filter.getPageSize())));

        return mongoTemplate.aggregate(newAggregation(aggregationOperations)
                .withOptions(makueniAggOptions()), "procurementPlan", ProcurementPlan.class)
                .getMappedResults();
    }

    @ApiOperation(value = "Counts Makueni Procurement Plans")
    @RequestMapping(value = "/api/makueni/procurementPlansCount",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    @Cacheable
    public Integer makueniProcurementPlansCount(@ModelAttribute @Valid final MakueniFilterPagingRequest filter) {
        List<AggregationOperation> aggregationOperations = makueniPlanOperations(filter);
        aggregationOperations.add(group().count().as("count"));

        final Document doc = mongoTemplate.aggregate(
                newAggregation(aggregationOperations).withOptions(makueniAggOptions()),
                "procurementPlan", Document.class).getUniqueMappedResult();

        return doc == null ? 0 : (Integer) doc.get("count");
    }

    @RequestMapping(value = "/api/makueni/procurementPlan/id/{id:^[0-9\\-]*$}",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ApiOperation(value = "Finds ProcurementPlan by the given id")
    @Cacheable
    public ProcurementPlan procurementPlanById(@PathVariable final Long id) {
        final Optional<ProcurementPlan> procurementPlan = procurementPlanMongoRepository.findById(id);

        if (procurementPlan.isPresent()) {
            return procurementPlan.get();
        } else {
            logger.error("We didn't found a ProcurementPlan with the ID: " + id);
            return null;
        }
    }

    @RequestMapping(value = "/api/makueni/project/id/{id:^[0-9\\-]*$}",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ApiOperation(value = "Finds a Project by the given id")
    @Cacheable
    public Document projectById(@PathVariable final Long id) {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final Aggregation aggregation = newAggregation(project("_id", "department", "fiscalYear", "projects"),
                unwind("projects"),
                match(Criteria.where("projects._id").is(id)));

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getUniqueMappedResult();
    }

    @RequestMapping(value = "/api/makueni/purchaseReq/id/{id:^[0-9\\-]*$}",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ApiOperation(value = "Finds a Tender Process by the given id")
    @Cacheable
    public Document purchaseReqById(@PathVariable final Long id) {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final Aggregation aggregation = newAggregation(project("department", "fiscalYear", "tenderProcesses"),
                unwind("tenderProcesses"),
                project("department", "fiscalYear", "tenderProcesses"),
                match(Criteria.where("tenderProcesses._id").is(id)));

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getUniqueMappedResult();
    }

    @RequestMapping(value = "/api/makueni/contractStats",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ApiOperation(value = "Fetch Contract Stats")
    @Cacheable
    public Document makueniContractStats() {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final Aggregation aggregation = newAggregation(
                unwind("tenderProcesses"),
                unwind("tenderProcesses.contract"),
                group().count().as("count").sum("tenderProcesses.contract.contractValue").as("value"));

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getUniqueMappedResult();
    }

    @ApiOperation(value = "Display the available Procurement Plan Departments.")
    @RequestMapping(value = "/api/makueni/filters/departments", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getDepartments() {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final DBObject project = new BasicDBObject("_id._id", 1);
        project.put("_id.label", 1);
        project.put("_id.code", 1);

        final Aggregation aggregation = newAggregation(project("department", Fields.UNDERSCORE_ID),
                group("department"), new CustomOperation(new Document("$project", project)),
                sort(Sort.by("_id.label")));

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getMappedResults();
    }

    @ApiOperation(value = "Display ALL Procurement Plan Departments.")
    @RequestMapping(value = "/api/makueni/filters/departments/all", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getAllDepartments() {
        final List<Document> results = new ArrayList<>();
        final List<Department> departments = departmentService.findAll();

        departments.stream().forEach(item -> results.add(new Document()
                .append("id", item.getId())
                .append("label", item.getLabel())));

        return results;
    }

    @ApiOperation(value = "Display the available Items.")
    @RequestMapping(value = "/api/makueni/filters/items", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getItems() {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();
        final Aggregation aggregation = newAggregation(
                project("tenderProcesses"),
                unwind("tenderProcesses"),
                project("tenderProcesses.tender"),
                unwind("tender"),
                project("tender.tenderItems"),
                unwind("tenderItems"),
                group("tenderItems.nonNullPlanItem.item")
                        .first("tenderItems.nonNullPlanItem.item.label").as("label"),
                sort(Sort.by(Sort.Direction.ASC, "label"))
        );

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getMappedResults();
    }

    @ApiOperation(value = "Display ALL Items.")
    @RequestMapping(value = "/api/makueni/filters/items/all", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getAllItems() {
        final List<Document> results = new ArrayList<>();
        final List<Item> items = itemService.findAll()
                .parallelStream()
                .filter(item -> item.getLabel() != null).collect(Collectors.toList());

        items.sort(Comparator.comparing(Category::getLabel));

        items.stream().forEach(item -> results.add(new Document()
                .append("id", item.getId())
                .append("label", item.getLabel())));

        return results;
    }

    @ApiOperation(value = "Display the available Sub Counties.")
    @RequestMapping(value = "/api/makueni/filters/subcounties", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getSubcounty() {
        final List<Document> results = new ArrayList<>();
        final List<Subcounty> items = subcountyService.findAll();

        items.sort(Comparator.comparing(Category::getLabel));

        items.stream().forEach(item -> results.add(new Document()
                .append("id", item.getId())
                .append("label", item.getLabel())));

        return results;
    }

    @ApiOperation(value = "Display the available Wards.")
    @RequestMapping(value = "/api/makueni/filters/wards", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getWard(@RequestParam(required = false) List<Long> subcountyIds) {
        final List<Document> results = new ArrayList<>();
        final List<Ward> items = wardService.findAll();

        items.sort(Comparator.comparing(Category::getLabel));

        items.stream().filter(i -> ObjectUtils.isEmpty(subcountyIds)
                || subcountyIds.contains(i.getSubcounty().getId())).
                forEach(item -> results.add(new Document()
                        .append("id", item.getId())
                        .append("label", item.getLabel())
                        .append("subcounty", item.getSubcounty().getLabel())
                        .append("subcountyId", item.getSubcounty().getId())));

        return results;
    }

    @ApiOperation(value = "Display the available Procurement Plan FY.")
    @RequestMapping(value = "/api/makueni/filters/fiscalYears", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    @Cacheable
    public List<Document> getFiscalYears() {
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final DBObject project = new BasicDBObject("_id._id", 1);
        project.put("_id.label", "$_id.name");
        project.put("_id.startDate", 1);
        project.put("_id.endDate", 1);

        final Aggregation aggregation = newAggregation(project("fiscalYear", Fields.UNDERSCORE_ID),
                group("fiscalYear"), new CustomOperation(new Document("$project", project)));

        return mongoTemplate.aggregate(aggregation.withOptions(options), "procurementPlan", Document.class)
                .getMappedResults();
    }

    @RequestMapping(value = "/api/file/{id:^[a-zA-Z0-9\\-]*$}",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ApiOperation(value = "Downloads a Makueni file")
    public void downloadFile(@PathVariable final String id, final HttpServletResponse response) throws IOException {
        final GridFSFile file = mongoFileStorageService.retrieveFile(new ObjectId(id));

        if (file != null) {
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getFilename());
            response.getOutputStream().write(IOUtils.toByteArray(gridFsOperations.getResource(file).getInputStream()));
        } else {
            logger.error("File with id: " + id + " not found!");
        }
    }

    private Criteria createFilterCriteria(final String filterName, final Object filterValues) {
        if (filterValues == null) {
            return new Criteria();
        }

        return where(filterName).is(filterValues);
    }

    private <S> Criteria createFilterCriteria(final String filterName, final Set<S> filterValues) {
        if (ObjectUtils.isEmpty(filterValues)) {
            return new Criteria();
        }

        return where(filterName).in(filterValues.toArray());
    }

    private Criteria createRangeFilterCriteria(final String filterName, final BigDecimal min, final BigDecimal max) {
        if (min == null && max == null) {
            return new Criteria();
        }

        Criteria criteria = where(filterName);
        if (min != null) {
            criteria = criteria.gte(min);
        }
        if (max != null) {
            criteria = criteria.lte(max);
        }
        return criteria;
    }

    private Criteria createTextCriteria(final String text) {
        if (text == null || text.isEmpty()) {
            return new Criteria();
        }

        Criteria criteria = new Criteria();
        criteria.orOperator(where("tender.tenderTitle")
                        .regex(Pattern.compile(text, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                where("projects.projectTitle")
                        .regex(Pattern.compile(text, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));

        return criteria;
    }
}
