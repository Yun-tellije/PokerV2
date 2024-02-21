package com.example.pokerv2.repository;

import com.example.pokerv2.model.HandHistory;
import com.example.pokerv2.model.UserHandHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserHandHistoryRepository extends JpaRepository<UserHandHistory, Long> {
}