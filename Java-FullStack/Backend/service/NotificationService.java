package com.jetlease.service;

import com.jetlease.entity.Notification;
import com.jetlease.repository.NotificationRepository;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void addNotification(String userEmail, String title, String message, String type) {
        Notification n = new Notification();
        n.setId(IdGen.uid("NTF"));
        n.setUserEmail(userEmail);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        n.setCreatedAt(IdGen.nowIso());
        notificationRepository.save(n);
    }

    public List<Notification> findByUser(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public void markAllRead(String userEmail) {
        List<Notification> list = notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }
}
