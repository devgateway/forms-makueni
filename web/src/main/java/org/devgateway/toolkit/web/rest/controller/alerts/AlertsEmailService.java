package org.devgateway.toolkit.web.rest.controller.alerts;

import org.devgateway.ocds.web.spring.SendEmailService;
import org.devgateway.toolkit.persistence.dao.alerts.Alert;
import org.devgateway.toolkit.persistence.dao.feedback.FeedbackMessage;
import org.devgateway.toolkit.persistence.dao.feedback.ReplyableFeedbackMessage;
import org.devgateway.toolkit.persistence.repository.AdminSettingsRepository;
import org.devgateway.toolkit.persistence.service.sms.SMSMessageService;
import org.devgateway.toolkit.web.WebSecurityUtil;
import org.devgateway.toolkit.web.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static org.devgateway.toolkit.persistence.dao.DBConstants.INSTANCE_NAME;

/**
 * @author idobre
 * @since 23/08/2019
 * <p>
 * Service used to send user email alerts.
 */
@Service
public class AlertsEmailService {
    private static final Logger logger = LoggerFactory.getLogger(AlertsEmailService.class);

    @Autowired
    private SendEmailService emailSendingService;

    @Autowired
    private SMSMessageService smsMessageService;

    @Value("${serverURL}")
    private String serverURL;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Transactional
    protected void sendFeedbackAlertEmails(ReplyableFeedbackMessage parent, FeedbackMessage message) {
        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage, "UTF-8");
            msg.setTo(parent.getEmail());
            msg.setFrom(emailSendingService.getFromEmail());
            msg.setSubject("You've received a reply to your feedback message!");
            msg.setText("Click on the link below to view your message on the Government of " + INSTANCE_NAME
                    + " County Open Contracting Portal.\n" + parent.getUrl());
        };
        try {
            emailSendingService.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send alert email for feedback message from " + message.getEmail());
            throw e;
        }
    }

    @Transactional
    public void sendFeedbackAlertsForReplyable(ReplyableFeedbackMessage replyableFeedbackMessage) {
        if (SecurityUtil.getDisableEmailAlerts(adminSettingsRepository)) {
            return;
        }
        replyableFeedbackMessage.getReplies().stream().filter(AbstractPersistable::isNew).forEach(
                m -> sendFeedbackAlertEmails(replyableFeedbackMessage, m));
    }

    @Transactional
    public void sendFeedbackAlertsForReplyableAndMessage(ReplyableFeedbackMessage replyableFeedbackMessage,
                                                         FeedbackMessage fm) {
        if (SecurityUtil.getDisableEmailAlerts(adminSettingsRepository)) {
            return;
        }

        if (ObjectUtils.isEmpty(replyableFeedbackMessage.getEmail())) {
            smsMessageService.sendSMS(replyableFeedbackMessage.getPhoneNumber(),
                    "Tender Code: " + replyableFeedbackMessage.getUrl().substring("tender/t/".length())
                            + "\nFeedback reply: " + fm.getComment());
        } else {
            sendFeedbackAlertEmails(replyableFeedbackMessage, fm);
        }
    }


    /**
     * Send a secret url that allows user to verify their email address.
     */
    public void sendVerifyEmail(final Alert alert) throws MailException {
        final String url = URI.create(WebSecurityUtil.createURL(request, "/verifyEmail/"))
                .resolve(alert.getSecret()).toASCIIString();

        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage);

            final String content = "Hello,\n\n"
                    + "You have subscribed to receive " + INSTANCE_NAME + " Alerts about: \n"
                    + (!alert.getDepartments().isEmpty() ? "New tenders in departments: \n"
                    + alert.getDepartments() + "\n" : "")
                    + (!alert.getItems().isEmpty() ? "New tenders with items: " + alert.getItems() + "\n" : "")
                    + "You can do this by simply clicking on the link below: \n\n"
                    + "Verify email url: <a style=\"color: #3060ED; text-decoration: none;\" href=\""
                    + url + "\">" + url + "</a>\n\n"
                    + "If you have problems, please paste the above URL into your browser.\n\n"
                    + "Thanks,\n"
                    + INSTANCE_NAME + " Portal Team";

            msg.setTo(alert.getEmail());
            msg.setFrom(emailSendingService.getFromEmail());
            msg.setSubject(INSTANCE_NAME + " OC Portal - Please Verify Email Address");
            msg.setText(content.replaceAll("\n", "<br />"), true);
        };
        try {
            emailSendingService.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send verification email for: " + alert.getEmail(), e);
            throw e;
        }
    }

    public void sendEmailAlert(final Alert alert, final MimeMessagePreparator message) throws MailException {
        try {
            emailSendingService.send(message);
        } catch (MailException e) {
            logger.error("Failed to send email alert for for: " + alert.getEmail(), e);
            throw e;
        }
    }


    public static String createURL(final HttpServletRequest request, final String resourcePath) {
        final int port = request.getServerPort();
        final StringBuilder result = new StringBuilder();

        result.append(request.getScheme())
                .append("://")
                .append(request.getServerName());

        if ((request.getScheme().equals("http") && port != 80)
                || (request.getScheme().equals("https") && port != 443)) {
            result.append(':')
                    .append(port);
        }

        result.append(request.getContextPath());

        if (resourcePath != null && resourcePath.length() > 0) {
            if (!resourcePath.startsWith("/")) {
                result.append("/");
            }
            result.append(resourcePath);
        }

        return result.toString();
    }
}
