package com.cb.notificationtemplateservice.beans;

import lombok.Data;

import java.util.List;

// TODO: Duplicated from NotificationPreferencesService. Keep in a separate jar and put in artifactory

@Data
public class NotificationPreference {
    private String id;
    private String customerId;
    private List<String> preferredNotificationTypes;
    private Long preferredNotificationTime;
    private boolean dnd;
}
