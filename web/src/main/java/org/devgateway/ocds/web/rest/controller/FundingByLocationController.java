/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.ocds.web.rest.controller;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.devgateway.ocds.web.rest.controller.request.DefaultFilterPagingRequest;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomOperation;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomProjectionOperation;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import io.swagger.annotations.ApiOperation;

/**
 *
 * @author mpostelnicu
 *
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class FundingByLocationController extends GenericOCDSController {

	@ApiOperation(value = "Planned funding by location by year. Returns the total amount of planning.budget"
			+ " available per planning.budget.projectLocation, grouped by year. "
			+ "This will return full location information, including geocoding data.")
    @RequestMapping(value = "/api/plannedFundingByLocation", method = RequestMethod.GET, produces = "application/json")
    public List<DBObject> plannedFundingByLocation(@ModelAttribute @Valid final DefaultFilterPagingRequest filter) {

        DBObject vars = new BasicDBObject();
        vars.put("numberOfLocations", new BasicDBObject("$size", "$planning.budget.projectLocation"));
        vars.put("planningBudget", "$planning.budget.amount.amount");
        DBObject in = new BasicDBObject("$divide", Arrays.asList("$$planningBudget", "$$numberOfLocations"));

        DBObject let = new BasicDBObject();
        let.put("vars", vars);
        let.put("in", in);

        DBObject dividedTotal = new BasicDBObject("$let", let);

        DBObject project = new BasicDBObject();
        project.put("planning.budget.projectLocation", 1);
        project.put("cntprj", new BasicDBObject("$literal", 1));
        project.put("planning.budget.amount.amount", 1);
        project.put("dividedTotal", dividedTotal);
        project.put("year", new BasicDBObject("$year", "$planning.bidPlanProjectDateApprove"));

        Aggregation agg = newAggregation(
                match(where("planning").exists(true).and("planning.budget.projectLocation.0").exists(true)
                        .andOperator(getProcuringEntityIdCriteria(filter))),
                new CustomOperation(new BasicDBObject("$project", project)), unwind("$planning.budget.projectLocation"),
                group("year", "planning.budget.projectLocation").sum("$dividedTotal").as("totalPlannedAmount")
                        .sum("$cntprj").as("recordsCount"),
                sort(Direction.DESC, "totalPlannedAmount"), skip(filter.getSkip()), limit(filter.getPageSize()));

        AggregationResults<DBObject> results = mongoTemplate.aggregate(agg, "release", DBObject.class);
        List<DBObject> tagCount = results.getMappedResults();
        return tagCount;

    }
	
	
	@ApiOperation(value = "Total estimated funding (tender.value) grouped by "
			+ "tender.items.deliveryLocation and also grouped by year."
			+ " The endpoint also returns the count of tenders for each location. "
			+ "It responds to all filters. The year is calculated based on tender.tenderPeriod.startDate")
	@RequestMapping(value = "/api/fundingByTenderDeliveryLocation", 
	method = RequestMethod.GET, produces = "application/json")
	public List<DBObject> fundingByTenderDeliveryLocation(
			@ModelAttribute @Valid final DefaultFilterPagingRequest filter) {

		DBObject project = new BasicDBObject();
		project.put("tender.items.deliveryLocation", 1);
		project.put("tender.value.amount", 1);
		project.put("year", new BasicDBObject("$year", "$tender.tenderPeriod.startDate"));

		Aggregation agg = newAggregation(
				match(where("tender").exists(true).and("tender.tenderPeriod.startDate").exists(true)
						.and("tender.value.amount").exists(true).andOperator(getDefaultFilterCriteria(filter))),
				new CustomProjectionOperation(project), unwind("$tender.items"),
				unwind("$tender.items.deliveryLocation"), match(where("tender.items.deliveryLocation").exists(true)),
				group("year", "tender.items.deliveryLocation").sum("$tender.value.amount").as("totalTendersAmount")
						.count().as("tendersCount"),
				sort(Direction.DESC, "totalTendersAmount"), skip(filter.getSkip()), limit(filter.getPageSize()));
			
		AggregationResults<DBObject> results = mongoTemplate.aggregate(agg, "release", DBObject.class);
		List<DBObject> tagCount = results.getMappedResults();
		return tagCount;
	}


}