package org.devgateway.ocds.web.flags.release;


import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.Contract;
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
import java.util.HashSet;
import java.util.Set;

/**
 * @author mpostelnicu
 * <p>
 * i184: Difference between the contract price and winning bid
 */
@Component
public class ReleaseFlagI184Processor extends AbstractFlaggedReleaseFlagProcessor {

    private MathContext mc = new MathContext(4, RoundingMode.HALF_UP);

    private static final BigDecimal THRESH = BigDecimal.valueOf(0.30f);

    @Override
    protected void setFlag(Flag flag, FlaggedRelease flaggable) {
        flaggable.getFlags().setI184(flag);
    }

    @Override
    protected Boolean calculateFlag(FlaggedRelease flaggable, StringBuffer rationale) {
        Contract contract = flaggable.getContracts().iterator().next();
        Award award = flaggable.getAwards().iterator().next();
        if (BigDecimal.ZERO.equals(award.getValue().getAmount())) {
            return false;
        }
        BigDecimal ratio = contract.getValue().getAmount().divide(award.getValue().getAmount(), mc);
        rationale.append("Contract value ").append(contract.getValue().getAmount())
                .append(" Award value ").append(award.getValue().getAmount()).append(" Ratio ").append(ratio)
                .append(" Threshold is ").append(THRESH);
        return ratio.compareTo(THRESH.add(BigDecimal.ONE)) > 0 || ratio.compareTo(BigDecimal.ONE.subtract(THRESH)) < 0;
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
                        FlaggedReleasePredicates.ACTIVE_AWARD,
                        FlaggedReleasePredicates.ACTIVE_CONTRACT
                ));
    }
}