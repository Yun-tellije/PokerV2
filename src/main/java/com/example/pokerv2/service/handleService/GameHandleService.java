package com.example.pokerv2.service.handleService;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.service.ActionService;
import com.example.pokerv2.service.BoardService;
import com.example.pokerv2.service.HandHistoryService;
import com.example.pokerv2.service.HudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameHandleService {

    private final BoardService boardService;
    private final ActionService actionService;
    private final HandHistoryService handHistoryService;
    private final HudService hudService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final static String TOPIC_PREFIX = "/topic/board/";
    private final static String ERROR_PREFIX = "/queue/error/";
    private final static int ACTION_TIME = 10;
    private final static int RESULT_ANIMATION_TIME = 5;

    public BoardDto joinRandomBoard(int blind, int requestBb, Principal principal) {

        BoardDto boardDto = boardService.joinRandom(blind, requestBb, principal);
        sendUpdateBoardToPlayers(boardDto, MessageType.PLAYER_JOIN);

        if (boardDto.getPhaseStatus() == PhaseStatus.WAITING.ordinal() && boardDto.getTotalPlayer() >= 2) {
            boardDto = startGame(boardDto.getId());
        }

        return boardDto;
    }

    public BoardDto join(Long boardId, int requestBb, Principal principal) {
        BoardDto boardDto = boardService.join(boardId, requestBb, principal);
        sendUpdateBoardToPlayers(boardDto, MessageType.PLAYER_JOIN);

        if (boardDto.getPhaseStatus() == PhaseStatus.WAITING.ordinal() && boardDto.getTotalPlayer() >= 2) {
            boardDto = startGame(boardDto.getId());
        }

        return boardDto;
    }

    public void action(BoardDto boardDto, String action, String userId) {
        hudService.addCountBeforeSaveAction(boardDto, action);
        actionService.saveAction(boardDto, action, userId);
        Board board = boardService.saveBoardChanges(boardDto, action, userId);
        while (true) {
            if (boardService.isGameEnd(board.getId())) {
                endGame(board.getId());
                break;
            }
            board = boardService.setNextAction(board.getId());

            if (board.getActionPos() == -1) {
                hudService.addCountBeforePhaseChange(board.getId());
                handHistoryService.savePhaseHistory(board.getId());
                if (boardDto.getPhaseStatus() != PhaseStatus.RIVER.ordinal()) {
                    boardDto = boardService.nextPhase(board.getId());
                    hudService.addCountAfterPhaseChange(board.getId());
                    sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_PHASE_START);
                } else {
                    endGame(board.getId());
                    break;
                }
            } else {
                sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_ACTION);
            }

            if (boardService.isGameEnd(board.getId())) {
                endGame(board.getId());
                break;
            }

            if (boardService.isActionPlayerConnect(board.getId())) {
                break;
            }

            waitDisconnectPlayer();

            if (boardService.isActionPlayerConnect(board.getId())) {
                break;
            } else {
                timeOutDisconnectPlayer(board);

                if (boardService.isGameEnd(board.getId())) {
                    endGame(board.getId());
                    break;
                } else {
                    boardService.sitOut(boardDto, boardService.getCurrentActionUserId(board.getId()));
                    sendUpdateBoardToPlayers(board.getId(), MessageType.PLAYER_EXIT);
                }
            }
        }
    }

    private void timeOutDisconnectPlayer(Board board) {
        BoardDto boardDto;
        boardDto = boardService.getBoard(board.getId());
        actionService.saveAction(boardDto, PlayerAction.FOLD.getActionDetail(), boardService.getCurrentActionUserId(board.getId()));
        board = boardService.saveBoardChanges(boardDto, PlayerAction.FOLD.getActionDetail(), boardService.getCurrentActionUserId(board.getId()));
    }

    public void exitPlayer(BoardDto boardDto, String userId) {

        boardService.sitOut(boardDto, userId);
        sendUpdateBoardToPlayers(boardDto.getId(), MessageType.PLAYER_EXIT);
        boardDto = boardService.getBoard(boardDto.getId());
        if (boardDto.getPhaseStatus() >= PhaseStatus.PRE_FLOP.ordinal() && boardDto.getPhaseStatus() <= PhaseStatus.RIVER.ordinal() &&
                boardService.isGameEnd(boardDto.getId())) {
            endGame(boardDto.getId());
        }
    }

    public BoardDto startGame(Long boardId) {
        BoardDto board = boardService.getBoard(boardId);
        if (board.getTotalPlayer() >= 2) {
            boardService.startGame(boardId);
            sendUpdateBoardToPlayers(boardId, MessageType.GAME_START);
            handHistoryService.createHandHistory(boardId);
            actionService.saveAnteAction(boardId);
            hudService.addCountAfterPhaseChange(boardId);
        }

        return boardService.getBoard(boardId);
    }

    public void endGame(Long boardId) {

        int resultAnimationCount = 0;
        BoardDto boardDto;

        boardService.refundOverBet(boardId);


        if (boardService.isShowDown(boardId)) {
            boardDto = boardService.showDown(boardId);
            sendUpdateBoardToPlayers(boardDto, MessageType.SHOW_DOWN);
            hudService.addCountAfterShowDown(boardDto);
            for (PlayerDto player : boardDto.getPlayers()) {
                GameResultDto gameResult = player.getGameResult();
                if (gameResult.isWinner()) {
                    resultAnimationCount++;
                }
            }

        } else {
            boardService.initPhase(boardId);
            boardDto = boardService.winOnePlayer(boardId);
            sendUpdateBoardToPlayers(boardDto, MessageType.GAME_END);
            resultAnimationCount = 1;
        }

        handHistoryService.end(boardDto);

        try {
            Thread.sleep(resultAnimationCount * RESULT_ANIMATION_TIME * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        if (boardDto.getGameSeq() != boardService.getBoard(boardId).getGameSeq()) {
            return;
        }

        boardService.dropDisconnectPlayers(boardId);
        List<PlayerDto> playerDtos = boardService.chargeMoney(boardId);
        boardService.dropMoneyLessPlayers(boardId, playerDtos);
        boardService.initBoard(boardId);

        for (PlayerDto playerDto : playerDtos) {
            sendErrorToPlayer(boardId, playerDto.getUserId(), new CustomException(ErrorCode.NOT_ENOUGH_MONEY));
        }

        sendUpdateBoardToPlayers(boardId, MessageType.INIT_BOARD);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        if (boardDto.getGameSeq() != boardService.getBoard(boardId).getGameSeq()) {
            return;
        }

        startGame(boardId);
    }

    private static void waitDisconnectPlayer() {
        try {
            Thread.sleep(ACTION_TIME * 1000);
        } catch (InterruptedException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendUpdateBoardToPlayers(Long boardId, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardId, new MessageDto(messageType.getDetail(), boardService.getBoard(boardId)));
    }

    private void sendUpdateBoardToPlayers(BoardDto boardDto, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardDto.getId(), new MessageDto(messageType.getDetail(), boardDto));
    }

    private void sendErrorToPlayer(Long boardId, Long userId, CustomException ex) {
        simpMessagingTemplate.convertAndSend(ERROR_PREFIX + boardId + "/" + userId, new MessageDto(MessageType.EXIT_BOARD.getDetail(), ex.getErrorCode().getDetail()));
    }
}