package com.example.myhome1.controller;

import com.example.myhome1.model.Board;
import com.example.myhome1.repository.BoardRepository;
import com.example.myhome1.repository.UserRepository;
import com.example.myhome1.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.myhome1.model.User;
import com.example.myhome1.repository.UserRepository;

import java.util.List;


import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
@RequestMapping("/board")
public class BorardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;

    @GetMapping("/list")
    public  String list(Model model, @PageableDefault(size = 10) Pageable pageable,
                        @RequestParam(required = false,defaultValue = "") String searchText){
        // Page<Board> boards = boardRepository.findAll(pageable);
        Page<Board> boards = boardRepository.findByTitleContainingOrContentContaining(searchText, searchText,pageable);
        int  startPage = Math.max(1,boards.getPageable().getPageNumber() - 4);
        int  endPage = Math.min(boards.getTotalPages(),boards.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);
        model.addAttribute("boards",boards);
        return "board/list";
    }

    @GetMapping("/form")
    public  String form(Model model, @RequestParam(required = false) Long id){
        if(id == null){
            model.addAttribute("board",new Board());
        }else{
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board",board);
        }

        return  "board/form";
    }

    @PostMapping("/form")
    public String from(@ModelAttribute Board board){
        // 현재 로그인한 사용자의 정보를 가져와서 board 객체에 설정합니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User user = userRepository.findByUsername(currentPrincipalName).orElse(null); // 수정: findByUsername의 결과가 Optional일 가능성이 있으므로 orElse(null) 추가
        if(user != null) {
            board.setUser(user);
        }
        boardRepository.save(board);
        return  "redirect:/board/list";
    }

    //상세조회
    @GetMapping("/view")
    public  String view(Model model, Long id){
        if(id == null){
            model.addAttribute("board",new Board());
        }else {
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board",board);
        }
        return "board/view";
    }

    //삭제
    @GetMapping("/delete")
    public  String boardDelete(Long id){
        boardService.boardDelete(id);
        return "redirect:/board/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id).orElse(null);
        model.addAttribute("board", board);
        return "board/edit";  // 'board/edit'는 수정 페이지 템플릿의 이름이어야 합니다.
    }
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Board updatedBoard) {
        Board board = boardRepository.findById(id).orElse(null);
        if (board != null) {
            board.setTitle(updatedBoard.getTitle());
            board.setContent(updatedBoard.getContent());
            boardRepository.save(board);
        }
        return "redirect:/board/view?id=" + id;
    }



}
