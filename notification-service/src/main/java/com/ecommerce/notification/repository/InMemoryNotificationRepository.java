package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.NotificationMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final CopyOnWriteArrayList<NotificationMessage> storage = new CopyOnWriteArrayList<>();

    @Override
    public void save(NotificationMessage notificationMessage) {
        storage.add(notificationMessage);
    }

    @Override
    public List<NotificationMessage> findAll() {
        return new ArrayList<>(storage);
    }
}
