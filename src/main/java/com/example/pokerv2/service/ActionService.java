package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.ActionRepository;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.HandHistoryRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final HandHistoryRepository handHistoryRepository;
    private static final String BB = "bb";

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void saveAction(BoardDto boardDto, String actOption, String userId){
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        User actionUser = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(board.getId(), board.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        String actionDetail = "";
        PlayerDto actionPlayer = null;

        for(PlayerDto playerDto : boardDto.getPlayers()){
            if(playerDto.getUserId() == actionUser.getId()){
                actionPlayer = playerDto;
                break;
            }
        }

        if(actOption.equals(PlayerAction.FOLD.getActionDetail())){
            actionDetail += PlayerAction.FOLD.getActionDetail();
        } else if(actOption.equals(PlayerAction.CALL.getActionDetail()) || actOption.equals(PlayerAction.ALL_IN_CALL.getActionDetail())) {
            actionDetail += PlayerAction.CALL.getActionDetail() + " " + actionPlayer.getPhaseCallSize()/board.getBlind() + BB;
        } else if(actOption.equals(PlayerAction.RAISE.getActionDetail()) || actOption.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
            actionDetail += PlayerAction.RAISE.getActionDetail() + " " + actionPlayer.getPhaseCallSize()/board.getBlind() + BB;
        } else if(actOption.equals(PlayerAction.CHECK.getActionDetail())) {
            actionDetail = PlayerAction.CHECK.getActionDetail();
        }

        Action action = Action.builder().actionNo(handHistory.getActionList().size()).handHistory(handHistory)
                .userId(actionUser.getId()).position(actionPlayer.getPosition()).phaseStatus(board.getPhaseStatus()).detail(actionDetail).build();
    }

    @Transactional
    public void saveAnteAction(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        HandHistory handHistory = handHistoryRepository.findByBoardIdAndGameSeq(board.getId(), board.getGameSeq()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Action> actionList = handHistory.getActionList();
        Player sbPlayer = null;
        Player bbPlayer = null;

        List<Player> players = board.getPlayers();

        for (Player player : players) {
            if(player.getPhaseCallSize() == board.getBlind() * 0.5){
                sbPlayer = player;
            } else if(player.getPhaseCallSize() == board.getBlind()){
                bbPlayer = player;
            }
        }

        if(sbPlayer != null && bbPlayer != null){
            Action sbAnteAction = Action.builder().phaseStatus(PhaseStatus.PRE_FLOP).actionNo(0)
                    .userId(sbPlayer.getUser().getId()).handHistory(handHistory).position(sbPlayer.getPosition().getPosNum()).detail("Ante 0.5" + BB).build();

            Action bbAnteAction = Action.builder().phaseStatus(PhaseStatus.PRE_FLOP).actionNo(1)
                    .userId(bbPlayer.getUser().getId()).handHistory(handHistory).position(bbPlayer.getPosition().getPosNum()).detail("Ante 1" + BB).build();

            actionList.add(sbAnteAction);
            actionList.add(bbAnteAction);
        }
    }

    private int getPlayerIdxByPos(Board board, int posNum){
        List<Player> players = board.getPlayers();

        for(int i=0; i<players.size(); i++){
            Player player = players.get(i);
            if(player.getPosition().getPosNum() == posNum){
                return i;
            }
        }
        return -1;
    }
}
