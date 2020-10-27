package org.devgateway.toolkit.persistence.service.sms;

import org.devgateway.toolkit.persistence.dao.sms.SMSMessage;
import org.devgateway.toolkit.persistence.service.BaseJpaService;

/**
 * @author mpostelnicu
 */
public interface SMSMessageService extends BaseJpaService<SMSMessage> {

    /**
     * Decides how to process each message based on the command given
     *
     * @param message
     * @return
     */
    boolean processSMS(SMSMessage message);

    /**
     * @param message
     * @return
     */
    boolean executeINFOCommand(SMSMessage message);

    boolean executeREPORTCommand(SMSMessage message);

    void processSMSQueue();

    boolean sendSMS(String destination, String text);

    void processAlertsQueue();

    void processAndPersistSMSResult(SMSMessage m);

}