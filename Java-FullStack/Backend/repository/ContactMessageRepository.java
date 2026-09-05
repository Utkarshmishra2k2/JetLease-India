package com.jetlease.repository;

import com.jetlease.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, String> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}
