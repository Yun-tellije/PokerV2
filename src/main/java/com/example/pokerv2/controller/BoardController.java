package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.BoardService;
import com.example.pokerv2.service.handleService.GameHandleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final GameHandleService gameHandleService;

    @GetMapping("/context")
    public List<BoardDto> getContext(Principal principal) {
        return boardService.getContext(principal);
    }

    @PostMapping("/joinGame")
    public BoardDto joinGame(@RequestParam int blind, @RequestParam int bb, Principal principal) {
        return gameHandleService.joinRandomBoard(blind, bb, principal);
    }

    @PostMapping("/joinGame/{boardId}")
    public BoardDto joinGame(@RequestParam Long boardId, @RequestParam int bb, Principal principal) {
        return gameHandleService.join(boardId, bb, principal);
    }

    @MessageMapping("/board/action/{option}")
    public void action(@RequestBody BoardDto boardDto, @DestinationVariable String option, Principal principal){
        gameHandleService.action(boardDto, option, principal.getName());
    }

    @GetMapping("/{boardId}")
    public BoardDto get(@PathVariable Long boardId, Principal principal) {
        return boardService.get(boardId, principal);
    }

    @MessageMapping("/board/exit")
    public void exitGame(@RequestBody BoardDto board, Principal principal) {
        gameHandleService.exitPlayer(board, principal.getName());
    }

    @GetMapping("/search/{blind}")
    public List<BoardDto> getBoardList(@PathVariable int blind) {
        return boardService.getBoardList(blind);
    }

    @PostMapping("/start/{boardId}")
    public ResponseEntity startGame(@PathVariable Long boardId) {
        gameHandleService.startGame(boardId);
        return new ResponseEntity(HttpStatus.OK);
    }
}
