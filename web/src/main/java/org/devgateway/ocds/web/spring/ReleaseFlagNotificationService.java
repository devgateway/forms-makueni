package org.devgateway.ocds.web.spring;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.util.Strings;
import org.devgateway.ocds.persistence.mongo.FlaggedRelease;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.persistence.mongo.flags.ReleaseFlags;
import org.devgateway.ocds.persistence.mongo.repository.main.FlaggedReleaseRepository;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Person;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.flags.ReleaseFlagHistory;
import org.devgateway.toolkit.persistence.repository.AdminSettingsRepository;
import org.devgateway.toolkit.persistence.service.PersonService;
import org.devgateway.toolkit.persistence.service.ReleaseFlagHistoryService;
import org.devgateway.toolkit.persistence.service.category.DepartmentService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.devgateway.toolkit.web.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.devgateway.ocds.persistence.mongo.flags.FlagsConstants.FLAGS_LIST;

@Service
public class ReleaseFlagNotificationService {

    protected static Logger logger = LoggerFactory.getLogger(ReleaseFlagNotificationService.class);

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PersonService personService;

    private Map<Long, Map<String, Set<String>>> departmentFlagRelease;

    @Autowired
    private ReleaseFlagHistoryService releaseFlagHistoryService;

    @Autowired
    private FlaggedReleaseRepository flaggedReleaseRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${serverURL}")
    private String serverURL;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @PostConstruct
    public void init() {
        departmentFlagRelease = new ConcurrentHashMap<>();
    }

    private void addDepartmentFlagReleaseId(Long department, String flag, String releaseId) {
        departmentFlagRelease.putIfAbsent(department, new ConcurrentHashMap<>());
        Map<String, Set<String>> stringSetMap = departmentFlagRelease.get(department);
        stringSetMap.putIfAbsent(flag, new HashSet<>());
        Set<String> set = stringSetMap.get(flag);
        set.add(releaseId);
    }

    @Transactional
    private Set<String> getUsersValidatorsEmailsFromRelease(Long departmentId) {
        Optional<Department> department = departmentService.findById(departmentId);
        return department.map(value -> personService.findByDepartmentWithRoles(value,
                SecurityConstants.Roles.ROLE_PROCUREMENT_USER, SecurityConstants.Roles.ROLE_PROCUREMENT_VALIDATOR
        ).stream().map(Person::getEmail).map(Strings::trimToNull).filter(Objects::nonNull).collect(Collectors.toSet()))
                .orElseGet(Sets::newHashSet);
    }

    @Transactional
    public void addFlaggedReleaseToNotificationTree(FlaggedRelease flaggedRelease) {
        Optional<ReleaseFlagHistory> latestReleaseFlagHistory = releaseFlagHistoryService.findLatestReleaseFlagHistory(
                flaggedRelease.getId());

        FLAGS_LIST.stream().map(ReleaseFlags::getShortFlagName).forEach(f -> {
                    boolean flag = flaggedRelease.getFlags().getFlagSet(f);
                    if (flag && (!latestReleaseFlagHistory.isPresent()
                            || !latestReleaseFlagHistory.get().getFlagged().contains(f))) {
                        addDepartmentFlagReleaseId(
                                Long.valueOf(flaggedRelease.getBuyer().getId().replaceAll(
                                        MongoConstants.OCDSSchemes.X_KE_INTERNAL_SCHEMA + "-", "")),
                                f, flaggedRelease.getId()
                        );

                    }
                }
        );
    }

    private long countAdminContent() {
        return departmentFlagRelease.values().stream().flatMap(s -> s.values().stream()).flatMap(Collection::stream)
                .count();
    }

    private long countDepartmentContent(Long departmentId) {
        return departmentFlagRelease.get(departmentId).values().stream().flatMap(Collection::stream).count();
    }

    @Transactional
    private String getDepartmentNameFromId(Long departmentId) {
        return departmentService.findById(departmentId).get().getLabel();
    }

    private String getReleaseURL(String releaseId) {
        return URI.create(serverURL + "/tenderProcess?id=" + releaseId).toASCIIString();
    }

    private String createAdminContent() {
        StringBuffer sb = new StringBuffer();
        sb.append("<ul>");
        departmentFlagRelease.forEach((department, flagReleaseId) -> {
            sb.append("<li>").append(getDepartmentNameFromId(department));
            sb.append("<ul>");
            flagReleaseId.forEach((flag, releaseId) -> {
                createFlagContent(sb, flag, releaseId);
            });
            sb.append("</ul>");
            sb.append("</li>");
        });
        sb.append("</ul>");
        return sb.toString();
    }

    private void createFlagContent(StringBuffer sb, String flag, Set<String> releaseId) {
        sb.append("<li><b>").append(getFlagNameFromId(flag)).append("</b><br/>");
        releaseId.forEach(r -> sb.append(getLinkedReleaseTitle(r)).append("<br/>"));
        sb.append("</li>");
    }

    private String createDepartmentContent(Long department) {
        StringBuffer sb = new StringBuffer();
        sb.append("<ul>");
        departmentFlagRelease.get(department).forEach((flag, releaseId) -> {
            createFlagContent(sb, flag, releaseId);
        });
        sb.append("</ul>");
        return sb.toString();
    }

    private String getFlagNameFromId(String flagName) {
        return translationService.getValue("en_US", "crd:indicators:" + flagName + ":name");
    }

    private void sendDepartmentEmails(Long department) {
        String[] strings = getUsersValidatorsEmailsFromRelease(department).toArray(new String[0]);
        long count = countDepartmentContent(department);
        if (strings.length == 0 || count == 0) {
            return;
        }

        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage, "UTF-8");
            msg.setTo(strings);
            msg.setFrom(DBConstants.FROM_EMAIL);
            msg.setSubject(count + " new Corruption Risk Flags for "
                    + getDepartmentNameFromId(department));
            msg.setText(createDepartmentContent(department), true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send red flag notification email for: " + strings, e);
            throw e;
        }
    }

    private void sendAdminEmails() {
        String[] strings = personService.getEmailsByRole(SecurityConstants.Roles.ROLE_ADMIN).toArray(new String[0]);
        long count = countAdminContent();
        if (strings.length == 0 || count == 0) {
            return;
        }
        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper msg = new MimeMessageHelper(mimeMessage, "UTF-8");
            msg.setTo(strings);
            msg.setFrom(DBConstants.FROM_EMAIL);
            msg.setSubject(count + " new Corruption Risk Flags");
            msg.setText(createAdminContent(), true);

        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Failed to send red flag notification email for: " + strings, e);
            throw e;
        }
    }

    private String getLinkedReleaseTitle(String releaseId) {
        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"").append(getReleaseURL(releaseId)).append("\">").append(getReleaseTitle(releaseId))
                .append("</a>");
        return sb.toString();
    }

    public String getReleaseTitle(String releaseId) {
        Optional<FlaggedRelease> optRelease = flaggedReleaseRepository.findById(releaseId);
        if (!optRelease.isPresent() || ObjectUtils.isEmpty(optRelease.get().getTender())
                || ObjectUtils.isEmpty(optRelease.get().getTender().getTitle())) {
            return "Unkown [id=" + releaseId + "]";
        }
        return optRelease.get().getTender().getTitle();
    }


    void sendNotifications() {
        if (SecurityUtil.getDisableEmailAlerts(adminSettingsRepository)) {
            return;
        }
        logger.info("Sending Red Flag Notifications " + departmentFlagRelease);
        sendAdminEmails();
        departmentFlagRelease.keySet().forEach(this::sendDepartmentEmails);
        departmentFlagRelease.clear();
    }
}
