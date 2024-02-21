package com.example.myhome1.service;

import com.example.myhome1.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    @Autowired
    BoardRepository boardRepository;

    public  void boardDelete(Long id){

        boardRepository.deleteById(id);
    }
}
