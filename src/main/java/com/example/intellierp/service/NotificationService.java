package com.example.intellierp.service;

import com.example.intellierp.entity.Notification;
import com.example.intellierp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(String title, String message, String type, String linkUrl) {
        Notification notification = new Notification(title, message, type, linkUrl);
        return notificationRepository.save(notification);
    }

    public List<Notification> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderByTimestampDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadStatusFalseOrderByTimestampDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByReadStatusFalse();
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setReadStatus(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByReadStatusFalseOrderByTimestampDesc();
        for (Notification n : unread) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(unread);
    }
}
