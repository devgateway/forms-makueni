package org.devgateway.toolkit.persistence.service.sms;

import org.apache.commons.lang3.StringUtils;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.alerts.AlertsSubscription;
import org.devgateway.toolkit.persistence.dao.alerts.ApprovedReport;
import org.devgateway.toolkit.persistence.dao.categories.SubWard;
import org.devgateway.toolkit.persistence.dao.feedback.ReplyableFeedbackMessage;
import org.devgateway.toolkit.persistence.dao.form.MEReport;
import org.devgateway.toolkit.persistence.dao.form.PMCReport;
import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.sms.SMSMessage;
import org.devgateway.toolkit.persistence.repository.alerts.ApprovedReportRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.repository.sms.SMSMessageRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.devgateway.toolkit.persistence.service.alerts.AlertsSubscriptionService;
import org.devgateway.toolkit.persistence.service.feedback.ReplyableFeedbackMessageService;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.devgateway.toolkit.persistence.dao.DBConstants.SMSCommands.INFO;
import static org.devgateway.toolkit.persistence.dao.DBConstants.SMSCommands.REPORT;

/**
 * @author mpostelnicu
 */
@Service
@Transactional
public class SMSMessageServiceImpl extends BaseJpaServiceImpl<SMSMessage> implements SMSMessageService {

    @Value("${infobip.key}")
    private String infobipKey;

    private static final Logger logger = LoggerFactory.getLogger(SMSMessageServiceImpl.class);

    @Autowired
    private SMSMessageRepository repository;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private ReplyableFeedbackMessageService replyableFeedbackMessageService;

    @Autowired
    private ApprovedReportRepository approvedReportRepository;

    @Autowired
    private AlertsSubscriptionService alertsSubscriptionService;

    @Autowired
    private OnfonMediaClient onfonMediaClient;

    @Autowired
    @Qualifier("smsAlerts")
    private MessageSource messageSource;

    @Override
    protected BaseJpaRepository<SMSMessage, Long> repository() {
        return repository;
    }

    @Override
    public SMSMessage newInstance() {
        return new SMSMessage();
    }

    @Override
    @Async
    @Scheduled(cron = "0 0/10 * * * ?")
    public void processSMSQueue() {
        repository.findByProcessedIsFalse().forEach(this::processAndPersistSMSResult);
    }

    @Override
    public synchronized void processAndPersistSMSResult(SMSMessage m) {
        boolean ret = processSMS(m);
        if (ret) {
            m.setProcessed(true);
            repository.saveAndFlush(m);
        }
    }

    @Override
    public boolean sendSMS(String destination, String text) {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> msg = new HashMap<>();
        msg.put("from", "40014");
        Map<String, Object> dest = new HashMap<>();
        dest.put("to", destination);
        msg.put("destinations", Arrays.asList(dest));
        msg.put("text", text);
        root.put("messages", msg);
        JSONObject jo = new JSONObject(root);

        final String baseUrl = "https://ekllr.api.infobip.com/sms/2/text/advanced";
        URI uri = null;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "App " + infobipKey);
        HttpEntity<String> request = new HttpEntity<>(jo.toString(), headers);

        logger.info("Sending SMS " + jo.toString());
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error("Error " + response.getStatusCode().value() + "sending SMS " + jo.toString());
            return false;
        }

        return true;
    }


    @Override
    public boolean processSMS(SMSMessage message) {
        if (message.getTextNoKeyword().toUpperCase().startsWith(INFO)) {
            return executeINFOCommand(message);
        }
        if (message.getTextNoKeyword().toUpperCase().startsWith(REPORT)) {
            return executeREPORTCommand(message);
        }
        return true;
    }

    private Long getTenderProcessIdFromCommand(String tenderProcessStr) {

        Long tenderProcessId = null;

        try {
            tenderProcessId = Long.valueOf(tenderProcessStr);
        } catch (NumberFormatException e) {
            logger.error("Error executing INFO command: number format exception for process id:" + tenderProcessStr);
            return null;
        }
        return tenderProcessId;
    }

    private TenderProcess getTenderProcessIfUsableBySMS(Long tenderProcessId) {
        Optional<TenderProcess> byId = tenderProcessService.findById(tenderProcessId);
        if (byId.isPresent() && byId.get().getStatus().equals(DBConstants.Status.APPROVED)
                && byId.get().getSingleTender() != null && byId.get().getSingleTender().getStatus()
                .equals(DBConstants.Status.APPROVED)) {
            return byId.get();
        }
        return null;
    }

    @Override
    public boolean executeINFOCommand(SMSMessage message) {
        String tenderProcessStr = message.getTextNoKeyword().substring(INFO.length()).trim();
        Long tenderProcessId = getTenderProcessIdFromCommand(tenderProcessStr);
        if (tenderProcessId == null) {
            return true;
        }

        TenderProcess tenderProcess = getTenderProcessIfUsableBySMS(tenderProcessId);
        if (tenderProcess != null) {

            // send sms back to host
            StringBuffer sb = new StringBuffer("Tender Code: ");
            sb.append(tenderProcess.getId()).append("\n");
            sb.append("Tender Name: ").append(tenderProcess.getSingleTender().getTenderTitle()).append("\n");
            Optional<MEReport> lastMEReport = tenderProcess.getMeReports().stream()
                    .filter(r -> r.getStatus().equals(DBConstants.Status.APPROVED))
                    .sorted(Comparator.comparing(MEReport::getApprovedDate).reversed()).findFirst();
            sb.append("M&E Status: ");
            if (lastMEReport.isPresent()) {
                sb.append(lastMEReport.get().getMeStatus().getLabel());
            } else {
                sb.append("Not available");
            }
            sb.append("\n");

            Optional<PMCReport> lastPMCReport = tenderProcess.getPmcReports().stream()
                    .filter(r -> r.getStatus().equals(DBConstants.Status.APPROVED))
                    .sorted(Comparator.comparing(PMCReport::getApprovedDate).reversed()).findFirst();
            sb.append("PMC Status: ");
            if (lastPMCReport.isPresent()) {
                sb.append(lastPMCReport.get().getPmcStatus().getLabel());
            } else {
                sb.append("Not available");
            }
            sb.append("\n");
            logger.info(sb.toString());
            return sendSMS(message.getFrom(), sb.toString());
        } else {
            return true;
        }

    }

    @Override
    public boolean executeREPORTCommand(SMSMessage message) {
        String idWithMessage = message.getTextNoKeyword().substring(REPORT.length()).trim();
        String idWithMessageNorm = StringUtils.normalizeSpace(idWithMessage);
        if (!idWithMessageNorm.contains(" ")) {
            return true;
        }
        String tenderProcessStr = idWithMessageNorm.split(" ")[0];
        Long tenderProcessId = getTenderProcessIdFromCommand(tenderProcessStr);
        if (tenderProcessId == null) {
            return true;
        }

        TenderProcess tenderProcess = getTenderProcessIfUsableBySMS(tenderProcessId);
        if (tenderProcess != null) {
            ReplyableFeedbackMessage fm = new ReplyableFeedbackMessage();
            fm.setUrl("tender/t/" + tenderProcessId);
            fm.setAddedByPublic(true);
            fm.setName("Mobile User " + (message.getFrom().hashCode() & 0xfffffff));
            fm.setPhoneNumber(message.getFrom());
            fm.setComment(idWithMessage.substring(tenderProcessStr.length()).trim());
            fm.setDepartment(tenderProcess.getDepartment());
            replyableFeedbackMessageService.save(fm);

            StringBuffer sb = new StringBuffer("Tender Code: ");
            sb.append(tenderProcess.getId()).append("\n");
            sb.append("Feedback recorded.").append("\n");
            sb.append("Thank you.").append("\n");
            logger.info(sb.toString());
            sendSMS(message.getFrom(), sb.toString());
        }
        return true;
    }

    @Override
    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void processAlertsQueue() {

        logger.info("processAlertsQueue");

        List<ApprovedReport> updates = approvedReportRepository.findByProcessedIsFalse();

        List<AlertsSubscription> subs = alertsSubscriptionService.findAll();

        List<Message> messages = subs.stream()
                .flatMap(sub -> getAlertsForSub(updates, sub).stream())
                .collect(Collectors.toList());

        if (!messages.isEmpty()) {
            onfonMediaClient.sendBulkSMS(messages);
        }

        updates.forEach(u -> u.setProcessed(true));
    }

    private List<Message> getAlertsForSub(List<ApprovedReport> updates, AlertsSubscription sub) {
        List<Message> messages = new ArrayList<>();

        List<MEReport> meReports = new ArrayList<>();
        List<PMCReport> pmcReports = new ArrayList<>();
        List<Tender> tenders = new ArrayList<>();

        Locale locale = new Locale(sub.getLanguage());

        updates.forEach(upd -> {
            if (upd.getMeReport() != null && matches(upd.getMeReport(), sub)) {
                meReports.add(upd.getMeReport());
                tenders.add(upd.getMeReport().getTenderProcess().getSingleTender());
            }

            if (upd.getPmcReport() != null && matches(upd.getPmcReport(), sub)) {
                pmcReports.add(upd.getPmcReport());
                tenders.add(upd.getPmcReport().getTenderProcess().getSingleTender());
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Tender tender : tenders) {
            StringJoiner text = new StringJoiner("\n");

            text.add(String.format("%s: %s", getMessage(locale, "tenderName"), tender.getTitle()));

            meReports.stream()
                    .filter(r -> r.getTenderProcess().getSingleTender().equals(tender))
                    .forEach(r -> text.add(String.format("%s [%s]: %s",
                            getMessage(locale, "meReportStatus"),
                            sdf.format(r.getApprovedDate()),
                            getMessage(locale, "meReportStatus." + r.getMeStatus().getCode()))));

            pmcReports.stream()
                    .filter(r -> r.getTenderProcess().getSingleTender().equals(tender))
                    .forEach(r -> text.add(String.format("%s [%s]: %s",
                            getMessage(locale, "pmcReportStatus"),
                            sdf.format(r.getApprovedDate()),
                            getMessage(locale, "pmcReportStatus." + r.getPmcStatus().getCode()))));

            messages.add(new Message(sub.getMsisdn(), text.toString()));
        }

        return messages;
    }

    private String getMessage(Locale locale, String code) {
        return messageSource.getMessage(code, null, locale);
    }

    private boolean matches(MEReport r, AlertsSubscription sub) {
        return r.getSubwards().stream().map(SubWard::getWard).anyMatch(sub::includes)
                || r.getWards().stream().anyMatch(sub::includes)
                || r.getSubcounties().stream().anyMatch(sub::includes);
    }

    private boolean matches(PMCReport r, AlertsSubscription sub) {
        return r.getWards().stream().anyMatch(sub::includes)
                || r.getSubcounties().stream().anyMatch(sub::includes);
    }
}

