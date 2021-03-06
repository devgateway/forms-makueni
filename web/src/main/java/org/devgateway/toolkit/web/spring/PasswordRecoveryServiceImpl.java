package org.devgateway.toolkit.web.spring;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.devgateway.ocds.web.spring.SendEmailService;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.Person;
import org.devgateway.toolkit.persistence.service.PersonService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Octavian Ciubotaru
 */
@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    public static final int RANDOM_PASSWORD_LENGTH = 16;

    private static final Pattern PWD_PATTERN = Pattern.compile(DBConstants.PASSWORD_PATTERN);

    private final PersonService personService;

    private final SendEmailService sendEmailService;

    private final PasswordEncoder passwordEncoder;

    public PasswordRecoveryServiceImpl(
            PersonService personService,
            SendEmailService sendEmailService,
            PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.sendEmailService = sendEmailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void resetPassword(String email) {
        Person person = personService.findByEmail(email);

        if (person != null) {
            final String newPassword = RandomStringUtils.random(RANDOM_PASSWORD_LENGTH, true, true);
            person.setPassword(passwordEncoder.encode(newPassword));
            person.setChangePasswordNextSignIn(true);

            personService.saveAndFlush(person);
            sendEmailService.sendEmailResetPassword(person, newPassword);
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Person person = personService.findByUsername(username);

        if (person == null) {
            throw new RuntimeException("No person with id " + username);
        }

        if (oldPassword == null || !passwordEncoder.matches(oldPassword, person.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (newPassword == null || !PWD_PATTERN.matcher(newPassword).matches()) {
            throw new RuntimeException("New password is null or does not match the pattern");
        }

        person.setPassword(passwordEncoder.encode(newPassword));
        person.setChangePasswordNextSignIn(false);
    }
}
