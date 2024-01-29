package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    @MessageMapping("/aaa")
    public void test(Principal principal){
        principal.getName();
    }

    @PostMapping("/joinGame")
    public BoardDto joinGame(@RequestParam int bb, Principal principal){
        System.out.println("principal = " + principal.getName());
        return boardService.join(bb, principal);
    }

    @PostMapping("/start/{boardId}")
    public BoardDto startGame(@PathVariable Long boardId){
        return new BoardDto(boardService.startGame(boardId));
    }

    @MessageMapping("/board/action/{option}")
    public void action(@RequestBody BoardDto boardDto, @DestinationVariable String option, Principal principal){
        System.out.println("action controller");
        boardService.action(boardDto, option, principal.getName());
    }

}
