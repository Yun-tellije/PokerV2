package com.example.pokerv2.service;

import com.example.pokerv2.dto.*;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.HandHistoryRepository;
import com.example.pokerv2.repository.HudRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HudService {

    private final HudRepository hudRepository;
    private final BoardRepository boardRepository;
    private final HandHistoryRepository handHistoryRepository;
    private final UserRepository userRepository;


    /**
     *
     * HUD의 각 변수들을 계산해야 하는 시점
     *     private int pfr; -> 프리플랍. 액션을 저장하기 전에.
     *     private int cBet; -> 플랍. 액션을 저장하기 전에.
     *     private int threeBet; -> 모든 페이즈. 액션을 저장하기 전에
     *     private int wsd; -> 팟 분배 이후
     *     private int vpip; -> 프리플랍. PhaseStatus가 플랍으로 변경되기 전에.
     *     private int totalHands; -> 게임 시작 시
     *     private int pfAggressiveCnt; -> 프리플랍. PhaseStatus가 플랍으로 변경되기 전에.
     *     private int wtf; -> 플랍. PhaseStatus가 변경된 직후
     *     private int wtsd; -> 리버 끝.
     */


    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public HudDto get(Long userId) {
        Hud hud = hudRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        return new HudDto(hud);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public HudDto get(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        return new HudDto(user.getHud());
    }

    /**
     * totalHands, wtf, wtsd 계산
     * @param boardId
     */
    @Transactional
    public void addCountAfterPhaseChange(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        List<Hud> hudList = new ArrayList<>();

        PhaseStatus phaseStatus = board.getPhaseStatus();

        if(phaseStatus == PhaseStatus.SHOWDOWN) {
            for (Player player : players) {
                plusWentToShowDown(player);
            }
        } else if(phaseStatus == PhaseStatus.PRE_FLOP) {
            for (Player player : players) {
                plusTotalHands(player);
            }
        } else if(phaseStatus == PhaseStatus.FLOP) {
            for (Player player : players) {
                plusWentToFlop(player);
            }
        }

        hudRepository.saveAll(hudList);
    }

    /**
     * addCountBeforePhaseChange
     *
     * Vpip, PfAggressiveCnt, Pfr 게산.
     * @param boardId
     */
    @Transactional
    public void addCountBeforePhaseChange(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();

        if(board.getPhaseStatus() == PhaseStatus.PRE_FLOP) {
            Player player = players.get(getPlayerIdxByPos(board, board.getBettingPos()));
            plusPfAggressiveCnt(player);
            plusVpipCnt(board);
            plusPfr(board);
        }
    }


    /**
     * addCountBeforeSaveAction
     * cBet, 3bet 계산.
     */
    @Transactional
    public void addCountBeforeSaveAction(BoardDto boardDto, String action) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if(boardDto.getPhaseStatus() == PhaseStatus.FLOP.ordinal()) {
            if (action.equals(PlayerAction.RAISE.getActionDetail()) || action.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
                if(board.getBettingSize() == 0) {
                    plusCBet(boardDto);
                }
            }
        }

        if(board.getBettingSize() != 0 && boardDto.getPhaseStatus() != PhaseStatus.PRE_FLOP.ordinal()) {
            if (action.equals(PlayerAction.RAISE.getActionDetail()) || action.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
                plusThreeBet(board);
            }
        }
    }

    /**
     * wsd 계산.
     * @param boardDto
     */
    @Transactional
    public void addCountAfterShowDown(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();

        for (PlayerDto player : players) {
            GameResultDto gameResult = player.getGameResult();
            if(gameResult.isWinner()) {
                Hud hud = hudRepository.findByUserId(player.getUserId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
                hud.setWsd(hud.getWsd() + 1);
            }
        }
    }

    private void plusThreeBet(Board board) {
        int raiseIdx = getPlayerIdxByPos(board, board.getBettingPos());
        List<Player> players = board.getPlayers();
        if(raiseIdx >= 0 && raiseIdx < players.size()) {
            Hud hud = players.get(raiseIdx).getUser().getHud();
            hud.setThreeBet(hud.getThreeBet() + 1);
        }
    }

    private void plusCBet(BoardDto boardDto) {
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(boardDto.getId(), boardDto.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Action> actionList = handHistory.getActionList();

        List<Action> pfActionList = actionList.stream()
                .filter(a -> a.getDetail().contains(PlayerAction.RAISE.getActionDetail()) && a.getPhaseStatus().equals(PhaseStatus.PRE_FLOP)).collect(Collectors.toList());

        if(pfActionList.size() > 0) {
            Action pfLastRaise = pfActionList.get(pfActionList.size() - 1);
            if(boardDto.getBettingPos() == pfLastRaise.getPosition()) {
                Hud hud = hudRepository.findByUserId(pfLastRaise.getUserId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
                hud.setCBet(hud.getCBet() + 1);
            }
        }
    }

    private void plusPfr(Board board) {
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(board.getId(), board.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Action> actionList = handHistory.getActionList();
        List<User> raiseUserList = new ArrayList<>();

        for (Action action : actionList) {
            if(action.getDetail().contains(PlayerAction.RAISE.getActionDetail())) {
                User user = userRepository.findById(action.getUserId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
                if(!raiseUserList.contains(user)) {
                    raiseUserList.add(user);
                }
            }
        }

        for (User user : raiseUserList) {
            Hud hud = user.getHud();
            hud.setPfr(hud.getPfr() + 1);
        }
    }

    private void plusVpipCnt(Board board) {
        List<Player> players = board.getPlayers();
        List<Hud> hudList = new ArrayList<>();
        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        int sbPlayerIdx = (btnPlayerIdx + 1) % board.getTotalPlayer();
        int bbPlayerIdx = (btnPlayerIdx + 2) % board.getTotalPlayer();

        for(int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            int phaseCallSize = player.getPhaseCallSize();
            if(i == sbPlayerIdx) {
                if(board.getBlind() * 0.5 < phaseCallSize) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            } else if(i == bbPlayerIdx) {
                if(board.getBlind() < phaseCallSize) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            } else {
                if(phaseCallSize > 0) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            }
        }

        hudRepository.saveAll(hudList);
    }

    private void plusWentToShowDown(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if(findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setWtsd(hud.getWtsd() + 1);
        }
    }

    private void plusTotalHands(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if(findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setTotalHands(hud.getTotalHands() + 1);
            hudRepository.save(hud);
        }
    }

    private void plusWentToFlop(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if (findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setWtf(hud.getWtf() + 1);
            hudRepository.save(hud);
        }
    }

    private void plusTotalHands(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        List<Hud> hudList = new ArrayList<>();
        for (PlayerDto player : players) {
            Optional<Hud> findHud = hudRepository.findByUserId(player.getUserId());

            if(findHud.isPresent()) {
                Hud hud = findHud.get();
                hud.setTotalHands(hud.getTotalHands() + 1);
                hudList.add(hud);
            }
        }
        hudRepository.saveAll(hudList);
    }

    private void plusPfAggressiveCnt(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
        if (findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setPfAggressiveCnt(hud.getPfAggressiveCnt() + 1);
            hudRepository.save(hud);
        }
    }



    private int getPlayerIdxByPos(Board board, int posNum) {
        List<Player> players = board.getPlayers();

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == posNum)
                return i;
        }

        return -1;
    }
}