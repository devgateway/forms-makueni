package org.devgateway.toolkit.persistence.mongo.reader;

import java.text.ParseException;

import org.devgateway.ocvn.persistence.mongo.ocds.BigDecimal2;
import org.devgateway.ocvn.persistence.mongo.ocds.Budget;
import org.devgateway.ocvn.persistence.mongo.ocds.Release;
import org.devgateway.ocvn.persistence.mongo.ocds.Tender;
import org.devgateway.ocvn.persistence.mongo.ocds.Value;
import org.devgateway.toolkit.persistence.mongo.dao.VNItem;
import org.devgateway.toolkit.persistence.mongo.dao.VNPlanning;
import org.devgateway.toolkit.persistence.mongo.dao.VNTender;
import org.devgateway.toolkit.persistence.mongo.repository.ReleaseRepository;

public class BidPlansRowImporter extends RowImporter<Release, ReleaseRepository> {

	public BidPlansRowImporter(ReleaseRepository releaseRepository, int skipRows) {
		super(releaseRepository, skipRows);
	}

	@Override
	public boolean importRow(String[] row) throws ParseException {

		String projectID = row[0];
		Release release = repository.findByBudgetProjectId(projectID);

		if (release == null) {
			release = new Release();
			release.getTag().add("planning");
			release.setOcid("ocvn-prjid-"+projectID);			
			VNPlanning planning = new VNPlanning();
			release.setPlanning(planning);
		}
		documents.add(release);

		Budget budget = new Budget();
		budget.setProjectID(row[0]);
		release.getPlanning().setBudget(budget);
		Value value = new Value();
		value.setCurrency("VND");
		budget.setAmount(value);

		//decimal2
		value.setAmount(getDecimal(row[5]));
		
		Tender tender = release.getTender();
		if (tender == null) {
			tender = new VNTender();
			tender.setId(release.getId());
			release.setTender(tender);
		}

		// create Items
		VNItem item = new VNItem();
		item.setId(Integer.toString(tender.getItems().size()));
		tender.getItems().add(item);


		//decimal2
		value.setAmount(getDecimal(row[5]));
		item.setDescription(row[1]);
		item.setBidPlanItemRefNum(row[2]);
		item.setBidPlanItemStyle(row[3]);
		item.setBidPlanItemFund(row[4]);
		item.setBidPlanItemMethodSelect(row[6]);
		item.setBidPlanItemMethod(row[7]);
		item.setId(row[8]);

		return true;
	}
}
