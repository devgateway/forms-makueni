package org.devgateway.toolkit.persistence.service.sms;

import java.util.ArrayList;
import java.util.List;

import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.dao.form.Tender;

/**
 * @author Octavian Ciubotaru
 */
public class TenderUpdates {

    private final Tender tender;
    private final List<MEReport> meReports = new ArrayList<>();
    private final List<PMCReport> pmcReports = new ArrayList<>();

    public TenderUpdates(Tender tender) {
        this.tender = tender;
    }

    public Tender getTender() {
        return tender;
    }

    public List<MEReport> getMeReports() {
        return meReports;
    }

    public List<PMCReport> getPmcReports() {
        return pmcReports;
    }
}
