package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.HandHistoryDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HandHistoryService {

    private final HandHistoryRepository handHistoryRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final UserHandHistoryRepository userHandHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public HandHistory createHandHistory(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        List<UserHandHistory> connectionList = new ArrayList<>();
        List<Integer> cardList = new ArrayList<>();
        List<Integer> posList = new ArrayList<>();


        HandHistory handHistory = HandHistory.builder().boardId(board.getId()).gameSeq(board.getGameSeq()).cardList(cardList).btnPosition(board.getBtn())
                .communityCard1(board.getCommunityCard1()).communityCard2(board.getCommunityCard2()).communityCard3(board.getCommunityCard3()).communityCard4(board.getCommunityCard4()).communityCard5(board.getCommunityCard5()).build();

        for (Player player : board.getPlayers()) {
            UserHandHistory connection = UserHandHistory.builder().user(player.getUser()).handHistory(handHistory).build();
            cardList.add(player.getCard1());
            cardList.add(player.getCard2());
            connectionList.add(connection);
            posList.add(player.getPosition().getPosNum());
        }

        handHistory.setUserList(connectionList);
        handHistory.setPosList(posList);

        handHistoryRepository.save(handHistory);
        userHandHistoryRepository.saveAll(connectionList);
        return handHistory;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePhaseHistory(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(board.getId(), board.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        int potAmount = board.getPot();

        for (Player player : players) {
            potAmount += player.getPhaseCallSize();
        }

        if(board.getPhaseStatus() == PhaseStatus.PRE_FLOP) {
            handHistory.setPotAmountPf(potAmount);
        } else if (board.getPhaseStatus() == PhaseStatus.FLOP) {
            handHistory.setPotAmountFlop(potAmount);
        } else if (board.getPhaseStatus() == PhaseStatus.TURN) {
            handHistory.setPotAmountTurn(potAmount);
        } else if (board.getPhaseStatus() == PhaseStatus.RIVER) {
            handHistory.setPotAmountRiver(potAmount);
        } else throw new CustomException(ErrorCode.BAD_REQUEST);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void end(BoardDto boardDto) {
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(boardDto.getId(), boardDto.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<PlayerDto> players = boardDto.getPlayers();
        List<Long> showDownUserIdList = handHistory.getShowDownUserIdList();
        handHistory.setFinish(true);
        int phaseStatus = boardDto.getPhaseStatus();

        if(phaseStatus == PhaseStatus.SHOWDOWN.ordinal()) {
            for (PlayerDto player : players) {
                GameResultDto gameResult = player.getGameResult();
                if(gameResult.getHandValue() != 0L) {
                    showDownUserIdList.add(player.getUserId());
                }
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<HandHistoryDto> get(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<UserHandHistory> connectionList = user.getHandHistoryList();
        List<HandHistoryDto> handHistoryList = new ArrayList<>();

        for (UserHandHistory userHandHistory : connectionList) {
            if(userHandHistory.getUser().equals(user) ) {
                HandHistory handHistory = userHandHistory.getHandHistory();
                if(handHistory.isFinish()) {
                    handHistoryList.add(new HandHistoryDto(handHistory));
                    System.out.println("new handHistoryDto = " + new HandHistoryDto(handHistory));
                }
            }
        }
        return handHistoryList;
    }

}