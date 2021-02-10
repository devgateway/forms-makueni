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
package org.devgateway.ocds.persistence.mongo.constants;

public final class MongoConstants {

    private MongoConstants() {
    }


    public static final class OCDSSchemes {
        public static final String X_KE_INTERNAL_SCHEMA = "X-KE-OCMAKUENI";
        public static final String X_KE_IFMIS = "x_KE-IFMIS";
        public static final String KE_IFMIS = "KE-IFMIS";
        public static final String UNCEFACT = "UNCEFACT";
    }

    public static final String OCDS_PREFIX = "ocds-ep75k8-";

    public static final int IMPORT_ROW_BATCH = 1000;

    public static final String MONGO_LANGUAGE = "english";

    public static final class FieldNames {
        public static final String OCID = "ocid";
        public static final String BUYER_ID = "buyer._id";
        public static final String BUYER_NAME = "buyer.name";
        public static final String AWARDS_DATE = "awards.date";
        public static final String PLANNING_FISCAL_YEAR = "planning.fiscalYear";
        public static final String PLANNING_BUDGET_PROJECT_ID = "planning.budget.projectID";
        public static final String PLANNING_BUDGET_AMOUNT = "planning.budget.amount.amount";
        public static final String PLANNING_BUDGETB = "planning.budget.budgetBreakdown";
        public static final String PLANNING_BUDGETB_AMOUNT = "planning.budget.budgetBreakdown.amount.amount";
        public static final String PLANNING_BUDGETB_ID = "planning.budget.budgetBreakdown._id";
        public static final String AWARDS_FIRST_TIME_WINNER = "awards.firstTimeWinner";
        public static final String AWARDS_STATUS = "awards.status";
        public static final String AWARDS_SUPPLIERS_ID = "awards.suppliers._id";
        public static final String AWARDS_SUPPLIERS_NAME = "awards.suppliers.name";
        public static final String AWARDS_VALUE_AMOUNT = "awards.value.amount";
        public static final String AWARDS_VALUE = "awards.value";
        public static final String TENDER_PROC_METHOD_RATIONALE = "tender.procurementMethodRationale";
        public static final String TENDER_ITEMS = "tender.items";
        public static final String TENDER_ITEMS_CLASSIFICATION = "tender.items.classification";
        public static final String TENDER_ITEMS_CLASSIFICATION_ID = "tender.items.classification._id";
        public static final String TENDER_ITEMS_UNIT_VALUE_AMOUNT = "tender.items.unit.value.amount";
        public static final String TENDER_ITEMS_QUANTITY = "tender.items.quantity";
        public static final String TENDER_PERIOD_START_DATE = "tender.tenderPeriod.startDate";
        public static final String TENDER_PROCURING_ENTITY_ID = "tender.procuringEntity._id";
        public static final String TENDER_PROCURING_ENTITY_NAME = "tender.procuringEntity.name";
        public static final String TENDER_PERIOD = "tender.tenderPeriod";
        public static final String TENDER_TITLE = "tender.title";
        public static final String CONTRACTS = "contracts";
        public static final String CONTRACTS_ID = "contracts._id";
        public static final String CONTRACTS_TITLE = "contracts.title";
        public static final String CONTRACTS_CONTRACTOR = "contracts.contractor";
        public static final String CONTRACTS_IMPL_TRANSACTIONS = "contracts.implementation.transactions";
        public static final String CONTRACTS_IMPL_TRANSACTIONS_AMOUNT = "contracts.implementation.transactions.amount"
                + ".amount";
        public static final String CONTRACTS_CONTRACTOR_ID = "contracts.contractor._id";
        public static final String CONTRACTS_MILESTONES = "contracts.implementation.milestones";
        public static final String CONTRACTS_STATUS = "contracts.status";
        public static final String CONTRACTS_DATE_SIGNED = "contracts.dateSigned";
        public static final String CONTRACTS_DELAYED = "contracts.implementation.milestones.delayed";
        public static final String CONTRACTS_PAYMENT_AUTHORIZED = "contracts.implementation.milestones"
                + ".authorizePayment";
        public static final String CONTRACTS_MILESTONE_CODE = "contracts.implementation.milestones"
                + ".code";
        public static final String TENDER_PERIOD_END_DATE = "tender.tenderPeriod.endDate";
        public static final String TENDER_VALUE = "tender.value";
        public static final String TENDER_VALUE_AMOUNT = "tender.value.amount";
        public static final String TENDER_LOCATIONS = "tender.locations";
        public static final String TENDER_LOCATIONS_ID = "tender.locations._id";
        public static final String TENDER_LOCATIONS_TYPE = "tender.locations.type";
        public static final String TENDER_NO_TENDERERS = "tender.numberOfTenderers";
        public static final String TENDER_TENDERERS_ID = "tender.tenderers._id";
        public static final String TENDER_PROC_METHOD = "tender.procurementMethod";
        public static final String TENDER_STATUS = "tender.status";
        public static final String TENDER_SUBMISSION_METHOD = "tender.submissionMethod";
        public static final String BIDS_DETAILS_TENDERERS_ID = "bids.details.tenderers._id";
        public static final String BIDS_DETAILS_VALUE_AMOUNT = "bids.details.value.amount";
        public static final String PARTIES_TARGET_GROUP = "parties.targetGroup";
        public static final String PARTIES_ID = "parties._id";
        public static final String FLAGS_TOTAL_FLAGGED = "flags.totalFlagged";
        public static final String FLAGS_COUNT = "flags.flaggedStats.count";
    }

    public static final class Filters {
        public static final String YEAR = "year";

        public static final String TEXT = "text";

        public static final String AWARD_STATUS = "awardStatus";

        public static final String FISCAL_YEAR = "fiscalYear";

        public static final String LOCATION_TYPE = "locationType";

        public static final String TENDER_STATUS = "tenderStatus";

        public static final String BID_TYPE_ID = "bidTypeId";

        public static final String NOT_BID_TYPE_ID = "notBidTypeId";

        public static final String PROCURING_ENTITY_ID = "procuringEntityId";

        public static final String OCID = "ocid";

        public static final String NOT_PROCURING_ENTITY_ID = "notProcuringEntityId";

        public static final String SUPPLIER_ID = "supplierId";

        public static final String CONTRACTOR_ID = "contractorId";

        public static final String BIDS_DETAILS_TENDERER_ID = "bidderDetailsTendererId";

        public static final String BUYER_ID = "buyerId";

        public static final String BIDDER_ID = "bidderId";

        public static final String TENDER_LOC = "tenderLoc";

        public static final String PROCUREMENT_METHOD = "procurementMethod";

        public static final String PROCUREMENT_METHOD_RATIONALE = "procurementMethodRationale";

        public static final String TENDER_VALUE = "tenderValue";

        public static final String AWARD_VALUE = "awardValue";

        public static final String FLAG_TYPE = "flagType";

        public static final String ELECTRONIC_SUBMISSION = "electronicSubmission";

        public static final String FLAGGED = "flagged";

        public static final String TOTAL_FLAGGED = "totalFlagged";
    }

    public static final Integer MONGO_DECIMAL_SCALE = 2;
}
