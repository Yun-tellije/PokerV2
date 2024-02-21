package com.example.pokerv2.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.pokerv2.model.Board;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE b.totalPlayer < 6 AND b.blind = :blind AND NOT EXISTS (SELECT p FROM Player p WHERE p.board = b AND p.user.id = :userId) ORDER BY b.id")
    List<Board> findFirstPlayableBoard(@Param("userId") Long userId, int blind, Pageable pageable);

    List<Board> findByBlind(int blind);
}