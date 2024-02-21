package com.example.pokerv2.repository;

import com.example.pokerv2.model.Hud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HudRepository extends JpaRepository<Hud, Long> {
    Optional<Hud> findByUserId(Long userId);
}