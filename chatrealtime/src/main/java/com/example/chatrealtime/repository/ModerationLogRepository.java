package com.example.chatrealtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chatrealtime.entity.ModerationLog;

public interface ModerationLogRepository extends JpaRepository<ModerationLog, Integer> {
}
