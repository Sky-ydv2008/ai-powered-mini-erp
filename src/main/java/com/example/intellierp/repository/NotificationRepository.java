package com.example.intellierp.repository;

import com.example.intellierp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop20ByOrderByTimestampDesc();
    List<Notification> findByReadStatusFalseOrderByTimestampDesc();
    long countByReadStatusFalse();
}
