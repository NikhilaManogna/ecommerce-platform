package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.NotificationMessage;
import java.util.List;

public interface NotificationRepository {

    void save(NotificationMessage notificationMessage);

    List<NotificationMessage> findAll();
}
