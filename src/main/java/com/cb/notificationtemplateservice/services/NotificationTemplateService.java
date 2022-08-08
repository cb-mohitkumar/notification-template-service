package com.cb.notificationtemplateservice.services;

import com.cb.notificationtemplateservice.beans.NotificationPreference;
import com.cb.notificationtemplateservice.beans.NotificationTemplate;
import com.cb.notificationtemplateservice.beans.dtos.NotificationTemplateRequest;
import com.cb.notificationtemplateservice.beans.dtos.NotificationTemplateResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cb.notificationtemplateservice.beans.dtos.NotificationTemplateRequest.*;

@Service
@AllArgsConstructor
public class NotificationTemplateService {
    private final RestTemplate restTemplate;
    private final SpringTemplateEngine templateEngine;
    public NotificationTemplateResponse createNotificationTemplate(NotificationTemplateRequest request) {
//        Flux<NotificationTemplate> response = Flux.empty();
        List<NotificationTemplate> templates = new ArrayList();
        String resourceUrl
                = "http://localhost:9092/notification-preferences/" + request.getCustomerId(); // TODO: This should be handled by service discovery
        ResponseEntity<NotificationPreference> preferenceResponse
                = restTemplate.getForEntity(resourceUrl, NotificationPreference.class);
        preferenceResponse.getBody().getPreferredNotificationTypes().forEach(preference -> {
            //TODO: multiple if statements. The code is not following Open Close Principle
            // Apply simple template creation strategy so that appropriate template creation handler is used at runtime
            if("EMAIL".equalsIgnoreCase(preference)) {
                templates.add(prepareEmailTemplate(request));
            }
            if("SMS".equalsIgnoreCase(preference)) {
                templates.add(prepareSMSTemplate(request));
            }
        });
        NotificationTemplateResponse response = new NotificationTemplateResponse();
        response.setNotificationTemplates(templates);
        return response;

    }

    private NotificationTemplate prepareSMSTemplate(NotificationTemplateRequest request) {
        NotificationTemplate smsTemplate = new NotificationTemplate();
        String smsTemplateString = "";
        if ("InvoiceDue".equalsIgnoreCase(request.getNotificationTemplateName())) {
            smsTemplateString = getInvoiceDueAmount();
        } else if ("PhoneNumberChanged".equalsIgnoreCase(request.getNotificationTemplateName())) {
            smsTemplateString = this.getPhoneNumberChanged();
        }
        StringSubstitutor sub = new StringSubstitutor(prepareNotificationParamsMap(request));
        String notificationContent = sub.replace(smsTemplateString);
        smsTemplate.setSmsContent(notificationContent);
        smsTemplate.setTemplateType("SMS");
        return smsTemplate;
    }

    private NotificationTemplate prepareEmailTemplate(NotificationTemplateRequest request) {
        String notificationContent = prepareNotificationContent(request);
        NotificationTemplate emailTemplate = new NotificationTemplate();
        emailTemplate.setEmailContent(notificationContent);
        if ("InvoiceDue".equalsIgnoreCase(request.getNotificationTemplateName())) {
            emailTemplate.setEmailSubject("Invoice Due Soon!!");
        } else if ("PhoneNumberChanged".equalsIgnoreCase(request.getNotificationTemplateName())) {
            emailTemplate.setEmailSubject("Your Phone Number Changed");
        }
        emailTemplate.setTemplateType("EMAIL");
        return emailTemplate;
    }

    private String prepareNotificationContent(NotificationTemplateRequest request) {
        String notificationContent = "";
        Context context = new Context();
        Map<String, Object> notificationParametersMap = prepareNotificationParamsMap(request);
        context.setVariables(notificationParametersMap);
        File emailTemplateFile = new File(
                "./src/main/resources/templates/" +
                        request.getNotificationTemplateName() + ".html");
        if (emailTemplateFile.exists()) {
            notificationContent = templateEngine.process(
                    request.getNotificationTemplateName() + ".html",
                    context);
        }
        return notificationContent;
    }

    private Map<String, Object> prepareNotificationParamsMap(NotificationTemplateRequest request) {
        Map<String, Object> notificationParametersMap =
                request.getNotificationParameters().
                        stream()
                        .collect(Collectors
                                .toMap(NotificationParameter::getNotificationParameterName, NotificationParameter::getNotificationParameterValue));
        return notificationParametersMap;
    }

    public static String getInvoiceDueAmount() {
        return "Hello ${name}"
                .concat("\n")
                .concat("Welcome to Chargebee\n")
                .concat("Your due amount is ${totalDue}\n")
                .concat("Thanks");
    }

    private String getPhoneNumberChanged() {
        return "Hello ${name}"
                .concat("\n")
                .concat("Welcome to Chargebee\n")
                .concat("Your Phone number is changed from ${oldPhoneNumber} to ${newPhoneNumber}\n");
    }
}
