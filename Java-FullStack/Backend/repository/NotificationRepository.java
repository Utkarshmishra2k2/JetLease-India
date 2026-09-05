package com.jetlease.repository;

import com.jetlease.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
