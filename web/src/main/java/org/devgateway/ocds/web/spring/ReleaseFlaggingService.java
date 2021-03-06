/**
 *
 */
package org.devgateway.ocds.web.spring;

import org.apache.commons.lang3.time.StopWatch;
import org.devgateway.ocds.persistence.mongo.FlaggedRelease;
import org.devgateway.ocds.persistence.mongo.flags.AbstractFlaggedReleaseFlagProcessor;
import org.devgateway.ocds.persistence.mongo.flags.ReleaseFlags;
import org.devgateway.ocds.persistence.mongo.repository.main.FlaggedReleaseRepository;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI002Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI007Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI016Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI019Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI038Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI045Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI077Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI083Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI085Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI171Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI180Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI182Processor;
import org.devgateway.ocds.web.flags.release.ReleaseFlagI184Processor;
import org.devgateway.toolkit.persistence.dao.flags.ReleaseFlagHistory;
import org.devgateway.toolkit.persistence.mongo.spring.MongoUtil;
import org.devgateway.toolkit.persistence.service.ReleaseFlagHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.devgateway.ocds.persistence.mongo.flags.FlagsConstants.FLAGS_LIST;
import static org.springframework.data.mongodb.core.query.Criteria.where;


/**
 * @author mpostelnicu
 */
@Service
public class ReleaseFlaggingService {


    protected static Logger logger = LoggerFactory.getLogger(ReleaseFlaggingService.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private FlaggedReleaseRepository releaseRepository;
    @Autowired
    private ReleaseFlagI038Processor releaseFlagI038Processor;
    @Autowired
    private ReleaseFlagI007Processor releaseFlagI007Processor;
    @Autowired
    private ReleaseFlagI019Processor releaseFlagI019Processor;
    @Autowired
    private ReleaseFlagI077Processor releaseFlagI077Processor;
    @Autowired
    private ReleaseFlagI180Processor releaseFlagI180Processor;
    @Autowired
    private ReleaseFlagI002Processor releaseFlagI002Processor;
    @Autowired
    private ReleaseFlagI085Processor releaseFlagI085Processor;
    @Autowired
    private ReleaseFlagI171Processor releaseFlagI171Processor;
    @Autowired
    private ReleaseFlagI184Processor releaseFlagI184Processor;
    @Autowired
    private ReleaseFlagI016Processor releaseFlagI016Processor;
    @Autowired
    private ReleaseFlagI045Processor releaseFlagI045Processor;
    @Autowired
    private ReleaseFlagI182Processor releaseFlagI182Processor;
    @Autowired
    private ReleaseFlagI083Processor releaseFlagI083Processor;
    @Autowired
    private ReleaseFlagHistoryService releaseFlagHistoryService;
    @Autowired
    private ReleaseFlagNotificationService releaseFlagNotificationService;

    @Autowired
    private CacheManager cacheManager;

    private Collection<AbstractFlaggedReleaseFlagProcessor> releaseFlagProcessors;

    public void logMessage(String message) {
        logger.info(message);
    }


    /**
     * Trigger {@link AbstractFlaggedReleaseFlagProcessor#reInitialize()} for all processors
     */
    private void reinitialize() {
        releaseFlagProcessors.forEach(AbstractFlaggedReleaseFlagProcessor::reInitialize);
    }

    private void processAndSaveFlagsForRelease(FlaggedRelease release) {
        releaseFlagProcessors.forEach(processor -> processor.process(release));
        prepareStats(release);
        //releaseRepository.save(release);
        mongoTemplate.updateFirst(Query.query(where("_id").is(release.getId())),
                Update.update("flags", release.getFlags()),
                FlaggedRelease.class);

        releaseFlagNotificationService.addFlaggedReleaseToNotificationTree(release);
        createReleaseHistoryFromRelease(release);

    }

    private void createReleaseHistoryFromRelease(FlaggedRelease flaggedRelease) {
        ReleaseFlagHistory rfh = new ReleaseFlagHistory();
        FLAGS_LIST.stream().
                map(ReleaseFlags::getShortFlagName).
                filter(f -> flaggedRelease.getFlags().getFlagSet(f)).
                collect(Collectors.toCollection(rfh::getFlagged));
        rfh.setReleaseId(flaggedRelease.getOcid());
        rfh.setFlaggedDate(ZonedDateTime.now());
        releaseFlagHistoryService.save(rfh);
    }

    /**
     * Saves the stats map in a collection format that Mongo can persist easily
     *
     * @param release
     */
    private void prepareStats(FlaggedRelease release) {
        release.getFlags().setFlaggedStats(release.getFlags().getFlaggedStatsMap().values());
        release.getFlags().setEligibleStats(release.getFlags().getEligibleStatsMap().values());
        release.getFlags().setTotalFlagged(release.getFlags().getFlagCnt());
    }

    public void processAndSaveFlagsForAllReleases(Consumer<String> logMessage) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logMessage.accept("<b>RUNNING CORRUPTION FLAGGING.</b>");

        reinitialize();

        MongoUtil.processRepositoryItemsPaginated(releaseRepository, this::processAndSaveFlagsForRelease,
                this::logMessage);

        stopWatch.stop();
        logMessage.accept("<b>CORRUPTION FLAGGING COMPLETE.</b>. Elapsed " + stopWatch.getTime() + "ms");

        releaseFlagNotificationService.sendNotifications();
    }

    /**
     * Sets flags on top of a stub empty release. This is just to populate the flags property.
     *
     * @return the flags property of {@link FlaggedRelease}
     */
    public ReleaseFlags createStubFlagTypes() {
        FlaggedRelease fr = new FlaggedRelease();
        releaseFlagProcessors.forEach(processor -> processor.process(fr));
        return fr.getFlags();
    }

    @PostConstruct
    protected void setProcessors() {
        releaseFlagProcessors = Collections.unmodifiableList(Arrays.asList(
                releaseFlagI038Processor,
                releaseFlagI007Processor,
                releaseFlagI019Processor,
                releaseFlagI077Processor,
                releaseFlagI180Processor,
                releaseFlagI002Processor,
                releaseFlagI085Processor,
                releaseFlagI171Processor,
                releaseFlagI184Processor,
                releaseFlagI016Processor,
                releaseFlagI045Processor,
                releaseFlagI182Processor,
                releaseFlagI083Processor
        ));
//        processAndSaveFlagsForAllReleases(this::logMessage);
    }
}
