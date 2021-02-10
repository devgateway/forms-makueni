package org.devgateway.toolkit.persistence.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * @author Octavian Ciubotaru
 */
@ConfigurationProperties("onfonmedia")
public class OnfonMediaProperties {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String baseURL = "https://api.onfonmedia.co.ke";

    private String senderId;

    private String clientId;

    private String apiKey;

    private String accessKey;

    @PostConstruct
    public void logWarning() {
        if (senderId == null || clientId == null || apiKey == null || accessKey == null || baseURL == null) {
            log.warn("**** SERVER STARTED WITH INCOMPLETE ONFONMEDIA CONFIGURATION !! ****");
        }
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
