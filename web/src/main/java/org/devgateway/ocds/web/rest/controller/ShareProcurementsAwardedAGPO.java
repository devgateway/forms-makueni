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

import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames;
import org.devgateway.ocds.web.rest.controller.request.YearFilterPagingRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.AWARDS_SUPPLIERS_ID;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.PARTIES_ID;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author mpostelnicu
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class ShareProcurementsAwardedAGPO extends GenericOCDSController {

    public static final String TARGET_GROUP_CITIZEN_CONTRACTOR = "Citizen Contractor";
    public static final String TARGET_GROUP_MARGIN_OF_PREFERENCE = "Margin of Preference for Local Contractor";
    public static final String NON_AGPO = "Non-AGPO";

    @ApiOperation(value = "Percent awarded for each target group by following the formula: (sum=(Awarded value for "
            + "procurements won by each target group))/sum(Total awarded value of all awards))*100")
    @RequestMapping(value = "/api/shareProcurementsAwardedAgpo",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")

    public List<Document> shareProcurementsAwardedAgpo(@ModelAttribute
                                                       @Valid final YearFilterPagingRequest filter) {
        Aggregation agg = newAggregation(
                match(where(FieldNames.AWARDS_VALUE_AMOUNT).exists(true)
                        .andOperator(getYearDefaultFilterCriteria(filter, getAwardDateField()))),
                unwind("awards"),
                match(where(FieldNames.AWARDS_STATUS).is(Award.Status.active.toString())),
                unwind("awards.suppliers"),
                unwind("parties"),
                project(FieldNames.PARTIES_TARGET_GROUP).and(FieldNames.AWARDS_VALUE_AMOUNT).as("value")
                        .and(ConditionalOperators.ifNull(FieldNames.PARTIES_TARGET_GROUP)
                                .then(NON_AGPO)).as("targetGroup").and(ConditionalOperators.when(
                        where(AWARDS_SUPPLIERS_ID)
                                .is(ref(PARTIES_ID))).then(1).otherwise(0)).as("partyMatch"),
                match(where("partyMatch").is(1)),
                project("targetGroup", "value")
                        .and(ConditionalOperators.when(where("targetGroup").in(
                                TARGET_GROUP_CITIZEN_CONTRACTOR, TARGET_GROUP_MARGIN_OF_PREFERENCE))
                                .then(NON_AGPO).otherwiseValueOf("targetGroup"))
                        .as("targetGroup"),
                group("targetGroup").sum("value").as("value")
        );
        return releaseAgg(agg);
    }
}