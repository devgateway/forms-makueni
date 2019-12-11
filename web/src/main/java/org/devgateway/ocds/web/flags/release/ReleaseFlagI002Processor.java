package org.devgateway.ocds.web.flags.release;

import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.Detail;
import org.devgateway.ocds.persistence.mongo.FlaggedRelease;
import org.devgateway.ocds.persistence.mongo.flags.AbstractFlaggedReleaseFlagProcessor;
import org.devgateway.ocds.persistence.mongo.flags.Flag;
import org.devgateway.ocds.persistence.mongo.flags.FlagType;
import org.devgateway.ocds.persistence.mongo.flags.preconditions.FlaggedReleasePredicates;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mpostelnicu
 * <p>
 * i002 Winning supplier provides a substantially lower bid price than competitors
 */
@Component
public class ReleaseFlagI002Processor extends AbstractFlaggedReleaseFlagProcessor {
    private MathContext mc = new MathContext(4, RoundingMode.HALF_UP);

    private static final BigDecimal THRESH = BigDecimal.valueOf(1.25f);

    @Override
    protected void setFlag(Flag flag, FlaggedRelease flaggable) {
        flaggable.getFlags().setI002(flag);
    }

    @Override
    protected Boolean calculateFlag(FlaggedRelease flaggable, StringBuffer rationale) {
        if (flaggable.getBids() == null || flaggable.getBids().getDetails() == null) {
            return false;
        }
        List<Detail> sortedList = flaggable.getBids()
                .getDetails()
                .stream()
                .filter(d -> d.getValue() != null)
                .sorted(Comparator.comparing(o -> o.getValue().getAmount()))
                .collect(Collectors.toList());

        if (sortedList.size() < 2) {
            return false;
        }

        Detail highestBid = sortedList.get(sortedList.size() - 1);
        Detail secondHighestBid = sortedList.get(
                sortedList.size() - 2); //this will not break because we have multiple bids per eligibility

        if (BigDecimal.ZERO.equals(secondHighestBid.getValue().getAmount())) {
            return false;
        }

        BigDecimal fraction = highestBid.getValue().getAmount().divide(secondHighestBid.getValue().getAmount(), mc);

        Award award = flaggable.getAwards().iterator().next();

        rationale.append("Highest bidder is ").append(highestBid.getTenderers())
                .append("Fraction between largest and 2nd largest bid is ").append(fraction).append(". Threshold is ")
                .append(THRESH);

        return fraction.compareTo(THRESH) > 0 && award.getSuppliers().containsAll(highestBid.getTenderers());
    }

    @Override
    protected Set<FlagType> flagTypes() {
        return new HashSet<>(Arrays.asList(FlagType.COLLUSION));
    }

    @Override
    @PostConstruct
    protected void setPredicates() {
        preconditionsPredicates = Collections.synchronizedList(
                Arrays.asList(
                        FlaggedReleasePredicates.ACTIVE_AWARD,
                        FlaggedReleasePredicates.OPEN_PROCUREMENT_METHOD
                                .or(FlaggedReleasePredicates.SELECTIVE_PROCUREMENT_METHOD),
                        FlaggedReleasePredicates.MULTIPLE_BIDS
                ));
    }

}