package org.devgateway.toolkit.persistence.mongo.spring;

import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.DefaultLocation;
import org.devgateway.ocds.persistence.mongo.Organization;
import org.devgateway.ocds.persistence.mongo.Release;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.persistence.mongo.flags.FlagsConstants;
import org.slf4j.Logger;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;

import javax.annotation.PostConstruct;

import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.FLAGS_TOTAL_FLAGGED;

public abstract class AbstractMongoDatabaseConfiguration {

    protected abstract Logger getLogger();

    protected abstract MongoTemplate getTemplate();

    public void createMandatoryImportIndexes() {
        //mongoTemplate.indexOps(Release.class).ensureIndex(new Index().on("planning.budget.projectID", Direction.ASC));
        //mongoTemplate.indexOps(Location.class).ensureIndex(new Index().on("description", Direction.ASC));
        getTemplate().indexOps(Organization.class).ensureIndex(new Index().on("identifier._id", Direction.ASC));
        getTemplate().indexOps(Organization.class)
                .ensureIndex(new Index().on("additionalIdentifiers._id", Direction.ASC));
        getTemplate().indexOps(Organization.class).ensureIndex(
                new Index().on("roles", Direction.ASC));
        getTemplate().indexOps(Organization.class).ensureIndex(new Index().on("name", Direction.ASC).unique());
        getTemplate().indexOps(DefaultLocation.class).ensureIndex(new Index().on("description", Direction.ASC));
        getTemplate().indexOps("fs.files").ensureIndex(new Index().on("md5", Direction.ASC));
        getTemplate().indexOps("fs.files").ensureIndex(new CompoundIndexDefinition(
                new Document("filename", 1).append("md5", 1)).unique());
        getLogger().info("Added mandatory Mongo indexes");
    }

    public void createCorruptionFlagsIndexes() {
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FLAGS_TOTAL_FLAGGED, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().on("flags.flaggedStats.type", Direction.ASC)
                .on("flags.flaggedStats.count", Direction.ASC)
        );

        getTemplate().indexOps(Release.class).ensureIndex(new Index().on("flags.eligibleStats.type", Direction.ASC)
                .on("flags.eligibleStats.count", Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I038_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I007_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I004_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I077_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I180_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I019_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I002_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I085_VALUE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(FlagsConstants.I171_VALUE, Direction.ASC));
        getLogger().info("Added corruption flags indexes");
    }

    @PostConstruct
    public void mongoPostInit() {
        createMandatoryImportIndexes();
        createPostImportStructures();
    }

    public void createPostImportStructures() {

        createCorruptionFlagsIndexes();


        // initialize some extra indexes
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on("ocid", Direction.ASC).unique());

        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_PROC_METHOD, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_PROC_METHOD_RATIONALE, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_STATUS, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_STATUS, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_SUPPLIERS_ID, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_SUPPLIERS_NAME, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_DATE, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.PLANNING_FISCAL_YEAR, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_FIRST_TIME_WINNER, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.AWARDS_VALUE_AMOUNT, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.PARTIES_TARGET_GROUP, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_NO_TENDERERS, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().on(
                MongoConstants.FieldNames.TENDER_SUBMISSION_METHOD, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on(MongoConstants.FieldNames.TENDER_PERIOD_START_DATE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index()
                .on(MongoConstants.FieldNames.TENDER_PERIOD_END_DATE, Direction.ASC));
        getTemplate().indexOps(Release.class)
                .ensureIndex(new Index().on("tender.items.classification._id", Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.TENDER_LOCATIONS_ID, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.TENDER_LOCATIONS_TYPE, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.BIDS_DETAILS_TENDERERS_ID, Direction.ASC));
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.BIDS_DETAILS_VALUE_AMOUNT, Direction.ASC));

        //contract indexes
        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_MILESTONE_CODE, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_ID, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_PAYMENT_AUTHORIZED, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_CONTRACTOR_ID, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_DELAYED, Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new Index().
                on(MongoConstants.FieldNames.CONTRACTS_STATUS, Direction.ASC));


        getTemplate().indexOps(Organization.class).ensureIndex(new TextIndexDefinitionBuilder()
                .withDefaultLanguage(MongoConstants.MONGO_LANGUAGE)
                .onField("name")
                .onField("_id").onField("additionalIdentifiers._id").build());

        getTemplate().indexOps(Organization.class).ensureIndex(new Index()
                .on("additionalIdentifiers.scheme", Direction.ASC)
                .on("identifier.scheme", Direction.ASC));

        getTemplate().indexOps(Release.class).ensureIndex(new TextIndexDefinitionBuilder()
                .named("text_search")
                .withDefaultLanguage(MongoConstants.MONGO_LANGUAGE)
                .onFields("tender.title", "tender.description",
                        "tender.procuringEntity.name", "tender._id", "tender.procuringEntity.description",
                        "awards._id", "awards.description", "awards.suppliers.name", "awards.suppliers.description",
                        "ocid", "buyer.name", "buyer._id", "awards.suppliers._id", "awards.suppliers.name",
                        "tender.procuringEntity._id", "tender.procuringEntity.additionalIdentifiers._id"
                ).build());

        getLogger().info("Added extra Mongo indexes");
    }

}
