package org.devgateway.toolkit.web.rest.controller.alerts.processsing;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.bson.Document;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.alerts.Alert;
import org.devgateway.toolkit.persistence.dao.alerts.AlertsStatistics;
import org.devgateway.toolkit.persistence.service.alerts.AlertService;
import org.devgateway.toolkit.persistence.service.alerts.AlertsStatisticsService;
import org.devgateway.toolkit.persistence.service.filterstate.alerts.AlertFilterState;
import org.devgateway.toolkit.web.rest.controller.alerts.AlertsEmailService;
import org.devgateway.toolkit.web.rest.controller.alerts.exception.AlertsProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author idobre
 * @since 26/08/2019
 */
@Service
public class AlertsManagerImpl implements AlertsManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertsManagerImpl.class);

    private static final Integer MAX_FAIL_COUNT = 3;

    private static final int THREAD_COUNT = 2;

    @Value("${serverURL}")
    private String serverURL;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertsEmailService alertsEmailService;

    @Autowired
    private AlertsStatisticsService alertsStatisticsService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public synchronized void sendAlerts() {
        final List<Alert> alerts = getAlertableUsers();

        logger.info(alerts.size() + " alerts(s) will be processed.");
        final List<Alert>[] lists = splitList(alerts);
        logger.info(lists.length + " threads will be created.");

        // create and start alerts threads
        final List<AlertsThread> threads = new ArrayList<>();
        for (int i = 0; i < lists.length; i++) {
            final AlertsThread thread = new AlertsThread(lists[i], this);
            thread.setName("Alerts Thread #" + i);
            thread.start();
            threads.add(thread);
        }

        final AlertsStatistics globalStats = new AlertsStatistics();

        try {
            // wait for threads to finish
            for (final AlertsThread thread : threads) {
                logger.info("Waiting for thread " + thread.getName() + " to finish.");
                thread.join();

                globalStats.addStats(thread.getThreadStats());
            }

            alertsStatisticsService.saveAndFlush(globalStats);

            // clear "servicesCache" cache;
            final CacheManager cm = CacheManager.getInstance();
            final Cache servicesCache = cm.getCache("servicesCache");
            if (servicesCache != null) {
                servicesCache.removeAll();
            }
        } catch (InterruptedException e) {
            logger.error("Couldn't join all threads", e);
        }
    }

    @Override
    @Transactional
    public AlertsStatistics processAlert(final Alert alert) throws AlertsProcessingException {
        try {
            final AlertsStatistics stats = new AlertsStatistics(1);
            final LocalDateTime now = LocalDateTime.now();

            final List<Document> documents;
            // get the Tenders for the email alert
            if (alert.getPurchaseReq() != null) {
                documents = getTenderUpdate(alert, stats);
            } else {
                documents = getTendersForAlert(alert, stats);
            }

            if (documents.isEmpty()) {
                alert.setLastChecked(now);
                return stats;
            }

            // send the alert
            sendAlertMessage(alert, documents, stats);

            // Update Alert in case of success
            alert.setFailCount(0);
            alert.setLastChecked(now);
            alert.setLastSendDate(now);
            alert.setLastErrorMessage(null);
            alertService.saveAndFlush(alert);

            return stats;
        } catch (Exception e) {
            logger.error("Error processing alert: " + alert.getId(), e);

            // Update Alert in case of failure
            alert.setFailCount(alert.getFailCount() + 1);
            alert.setLastErrorMessage(e.getMessage());
            alertService.saveAndFlush(alert);

            throw new AlertsProcessingException(alert, e);
        }
    }

    private void sendAlertMessage(final Alert alert, final List<Document> documents, final AlertsStatistics stats)
            throws MailException {
        stats.startSendingStage();

        final MimeMessagePreparator message = createMailMessage(alert, documents);
        alertsEmailService.sendEmailAlert(alert, message);

        stats.endSendingStage();
    }

    private MimeMessagePreparator createMailMessage(final Alert alert, final List<Document> documents) {

        final StringBuilder tenderLinks = new StringBuilder();
        for (final Document document : documents) {
            final Document project = (Document) document.get("projects");
            final Document purchaseReq = (Document) project.get("tenderProcesses");
            final Long purchaseReqId = (Long) purchaseReq.get("_id");

            final String tenderUrl = String.format("%s/ui/index.html#!/tender/t/%d", serverURL, purchaseReqId);
            tenderLinks.append("* <a style=\"color: #3060ED; text-decoration: none;\" href=\""
                    + tenderUrl + "\">" + tenderUrl + "</a>\n");
        }

        final String unsubscribeURL = String.format("%s/unsubscribeEmail/%s", serverURL, alert.getSecret());

        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage);

            msg.setTo(alert.getEmail());
            msg.setFrom(DBConstants.FROM_EMAIL);

            final String content;
            if (alert.getPurchaseReq() != null) {
                content = "Hello,\n\n"
                        + "There has been an update to \""
                        + alert.getPurchaseReq().getSingleTender().getTenderTitle()
                        + "\" please visit the website to see the update: \n\n"
                        + tenderLinks.toString() + "\n"
                        + "Thanks,\n"
                        + "Elgeyo Marakwet Portal Team \n\n\n"
                        + "If you do not want to receive our email alerts anymore please click on the following link: "
                        + "<a style=\"color: #3060ED; text-decoration: none;\" href=\""
                        + unsubscribeURL + "\">" + unsubscribeURL + "</a>\n";

                msg.setSubject("Tender \""
                        + alert.getPurchaseReq().getSingleTender().getTenderTitle()
                        + "\" has been updated");
            } else {
                content = "Hello,\n\n"
                        + "The following tender(s) have been published: \n\n"
                        + tenderLinks.toString() + "\n"
                        + "Thanks,\n"
                        + "Elgeyo Marakwet Portal Team \n\n\n"
                        + "If you do not want to receive our email alerts anymore please click on the following link: "
                        + "<a style=\"color: #3060ED; text-decoration: none;\" href=\""
                        + unsubscribeURL + "\">" + unsubscribeURL + "</a>\n";

                msg.setSubject("New tender(s) released");
            }

            msg.setText(content.replaceAll("\n", "<br />"), true);

        };

        return messagePreparator;
    }

    private List<Document> getTenderUpdate(final Alert alert, AlertsStatistics stats) {
        stats.startDbAccess();
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final Aggregation aggregation = newAggregation(
                project("_id", "department", "fiscalYear", "tenderProcesses"),
                unwind("tenderProcesses"),
                match(where("tenderProcesses._id").is(alert.getPurchaseReq().getId())),
                unwind("tenderProcesses.tender"),
                match(where("tenderProcesses.tender.closingDate").gte(new Date())),
                match(where("tenderProcesses.lastModifiedDate")
                        .gte(Date.from(alert.getLastChecked().atZone(ZoneId.systemDefault()).toInstant()))));

        final List<Document> documents = mongoTemplate.aggregate(
                aggregation.withOptions(options), "procurementPlan", Document.class)
                .getMappedResults();

        stats.endDbAccess();

        return documents;
    }

    private List<Document> getTendersForAlert(final Alert alert, AlertsStatistics stats) {
        stats.startDbAccess();
        final AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(true).build();

        final List<Criteria> criteriaList = new ArrayList<>();
        if (!alert.getDepartments().isEmpty()) {
            criteriaList.add(createFilterCriteria("department._id", alert.getDepartments()));
        }
        if (!alert.getItems().isEmpty()) {
            criteriaList.add(createFilterCriteria(
                    "tenderProcesses.tender.tenderItems.purchaseItem.planItem.item._id",
                    alert.getItems()));
        }

        final Criteria criteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        final Aggregation aggregation = newAggregation(
                project("_id", "department", "fiscalYear", "tenderProcesses"),
                unwind("tenderProcesses"),
                unwind("tenderProcesses.tender"),
                match(where("tenderProcesses.tender.closingDate").gte(new Date())),
                match(where("tenderProcesses.lastModifiedDate")    // change to "lte" for local testing
                        .gte(Date.from(alert.getLastChecked().atZone(ZoneId.systemDefault()).toInstant()))),
                match(criteria));

        final List<Document> documents = mongoTemplate.aggregate(
                aggregation.withOptions(options), "procurementPlan", Document.class)
                .getMappedResults();

        stats.endDbAccess();

        return documents;
    }

    /**
     * Get the list of valid {@link Alert}
     * Things take into consideration {@link Alert#getEmailVerified()}, {@link Alert#getAlertable()} ()}, etc...
     */
    private List<Alert> getAlertableUsers() {
        final AlertFilterState alertFilterState = new AlertFilterState(true, true, MAX_FAIL_COUNT);

        return alertService.findAllNoCache(alertFilterState.getSpecification());
    }

    /**
     * Splits a list into <tt>THREAD_COUNT</tt> sublists.
     *
     * @param list List to be splitted.
     * @return Array of sublists.
     */
    private List[] splitList(List list) {
        final int size = list.size();
        int subLists = THREAD_COUNT;
        int chunk = size / subLists;
        if (chunk == 0) {
            chunk = 1;
            subLists = size;
        }

        final List splitted = new ArrayList(subLists);
        for (int i = 0; i < subLists; i++) {
            // for the last sublist get all remaining elements
            final int toIndex = (i == subLists - 1) ? size : (i + 1) * chunk;
            splitted.add(list.subList(i * chunk, toIndex));
        }

        return (List[]) splitted.toArray(new List[splitted.size()]);
    }

    private <S extends GenericPersistable> Criteria createFilterCriteria(
            final String filterName, final Set<S> filterValues) {
        if (ObjectUtils.isEmpty(filterValues)) {
            return new Criteria();
        }

        final Set<Long> ids = filterValues.stream()
                .map(GenericPersistable::getId)
                .collect(Collectors.toSet());

        return where(filterName).in(ids.toArray());
    }
}
