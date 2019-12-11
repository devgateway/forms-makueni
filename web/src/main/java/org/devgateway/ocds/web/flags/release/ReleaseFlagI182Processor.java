package org.devgateway.ocds.web.flags.release;


import org.devgateway.ocds.persistence.mongo.Detail;
import org.devgateway.ocds.persistence.mongo.FlaggedRelease;
import org.devgateway.ocds.persistence.mongo.flags.AbstractFlaggedReleaseFlagProcessor;
import org.devgateway.ocds.persistence.mongo.flags.Flag;
import org.devgateway.ocds.persistence.mongo.flags.FlagType;
import org.devgateway.ocds.persistence.mongo.flags.preconditions.FlaggedReleasePredicates;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mpostelnicu
 * <p>
 * (i182) Lowest evaluated bidder is considered non-responsive
 */
@Component
public class ReleaseFlagI182Processor extends AbstractFlaggedReleaseFlagProcessor {

    @Override
    protected void setFlag(Flag flag, FlaggedRelease flaggable) {
        flaggable.getFlags().setI182(flag);
    }

    @Override
    protected Boolean calculateFlag(FlaggedRelease flaggable, StringBuffer rationale) {
        Detail minBid = flaggable.getBids()
                .getDetails()
                .stream()
                .filter(d -> d.getValue() != null)
                .min(Comparator.comparing(d -> d.getValue().getAmount()))
                .orElse(null);
        if (minBid == null) {
            return false;
        }
        Boolean b = minBid.getStatus().equals("disqualified");
        if (b) {
            rationale.append("Bidder ").append(minBid.getTenderers().iterator().next())
                    .append(" has offered smallest bid of ").append(minBid.getValue().getAmount())
                    .append(" and was disqualified.");
            return true;
        }
        return false;
    }

    @Override
    protected Set<FlagType> flagTypes() {
        return new HashSet<FlagType>(Arrays.asList(FlagType.RIGGING));
    }

    @Override
    @PostConstruct
    protected void setPredicates() {
        preconditionsPredicates = Collections.synchronizedList(
                Arrays.asList(
                        FlaggedReleasePredicates.MULTIPLE_BIDS
                ));
    }
}