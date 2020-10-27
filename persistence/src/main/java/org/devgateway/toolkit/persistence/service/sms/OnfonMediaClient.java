package org.devgateway.toolkit.persistence.service.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Octavian Ciubotaru
 */
@Service
public class OnfonMediaClient {

    private final Logger logger = LoggerFactory.getLogger(OnfonMediaClient.class);

    @Autowired
    private OnfonMediaProperties properties;

    public static class BulkSMSRequest {

        @JsonProperty("ApiKey")
        private final String apiKey;

        @JsonProperty("ClientId")
        private final String clientId;

        @JsonProperty("SenderId")
        private final String senderId;

        @JsonProperty("MessageParameters")
        private final List<Message> messageParameters;

        public BulkSMSRequest(String apiKey, String clientId, String senderId,
                List<Message> messageParameters) {
            this.apiKey = apiKey;
            this.clientId = clientId;
            this.senderId = senderId;
            this.messageParameters = messageParameters;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getClientId() {
            return clientId;
        }

        public String getSenderId() {
            return senderId;
        }

        public List<Message> getMessageParameters() {
            return messageParameters;
        }
    }

    public static class BulkSMSResponse {

        @JsonProperty("Data")
        private List<SMSResponse> data;

        public List<SMSResponse> getData() {
            return data;
        }

        public void setData(List<SMSResponse> data) {
            this.data = data;
        }
    }

    public static class SMSResponse {

        @JsonProperty("MessageErrorCode")
        private int messageErrorCode;

        @JsonProperty("MessageErrorDescription")
        private String messageErrorDescription;

        @JsonProperty("MobileNumber")
        private String mobileNumber;

        public int getMessageErrorCode() {
            return messageErrorCode;
        }

        public void setMessageErrorCode(int messageErrorCode) {
            this.messageErrorCode = messageErrorCode;
        }

        public String getMessageErrorDescription() {
            return messageErrorDescription;
        }

        public void setMessageErrorDescription(String messageErrorDescription) {
            this.messageErrorDescription = messageErrorDescription;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SMSResponse.class.getSimpleName() + "[", "]")
                    .add("messageErrorCode=" + messageErrorCode)
                    .add("messageErrorDescription='" + messageErrorDescription + "'")
                    .add("mobileNumber='" + mobileNumber + "'")
                    .toString();
        }
    }

    private class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody,
                ClientHttpRequestExecution execution) throws IOException {

            logger.info(">> {} {}\n{}", request.getMethodValue(), request.getURI(),
                    new String(requestBody, StandardCharsets.UTF_8));

            ClientHttpResponse response = execution.execute(request, requestBody);

            InputStreamReader isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
            String responseBody = new BufferedReader(isr)
                    .lines()
                    .collect(Collectors.joining("\n"));
            logger.info("<< {} {}\n{}", response.getRawStatusCode(), response.getStatusText(), responseBody);

            return response;
        }
    }

    public void sendBulkSMS(List<Message> messages) {
        BulkSMSRequest request = new BulkSMSRequest(properties.getApiKey(), properties.getClientId(),
                properties.getSenderId(), messages);

        HttpHeaders headers = new HttpHeaders();
        headers.add("AccessKey", properties.getAccessKey());
        HttpEntity<BulkSMSRequest> httpEntity = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(
                        new HttpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        String url = properties.getBaseURL() + "/v1/sms/SendBulkSMS";
        BulkSMSResponse response = restTemplate.postForObject(url, httpEntity, BulkSMSResponse.class);

        response.getData().stream()
                .filter(r -> r.getMessageErrorCode() != 0)
                .forEach(r -> logger.error(r.toString()));
    }
}
