package com.example.myhome1.repository;

import com.example.myhome1.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {


    Page<Board> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
