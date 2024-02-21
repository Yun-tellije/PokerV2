package com.example.pokerv2.utils;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.PlayerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PotDistributorUtils {

    private static final Comparator<PlayerDto> HAND_VALUE_COMPARATOR = Comparator
            .comparingLong(p -> p.getGameResult().getHandValue());

    private PotDistributorUtils() {}

    public static void distribute(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        players.sort(HAND_VALUE_COMPARATOR.reversed());

        Map<Long, Integer> handStrongCount = new HashMap<>();

        for(int i = 0; i < players.size(); i++) {
            GameResultDto gameResult = players.get(i).getGameResult();
            if(handStrongCount.containsKey(gameResult.getHandValue())){
                handStrongCount.replace(gameResult.getHandValue(), handStrongCount.get(gameResult.getHandValue()) + 1);
            } else {
                handStrongCount.put(gameResult.getHandValue(), 1);
            }
        }

        for(int i = 0; i < boardDto.getTotalPlayer()-1; i++) {
            PlayerDto winPlayer = players.get(i);
            Integer sameHandPlayerSize = handStrongCount.get(winPlayer.getGameResult().getHandValue());
            if(sameHandPlayerSize == 1) {
                distributeOnePlayer(boardDto, i);
            } else {
                distributeDrawPlayers(boardDto, i, winPlayer.getGameResult().getHandValue());
                i = i + sameHandPlayerSize - 1;
            }

            if(boardDto.getPot() < boardDto.getTotalPlayer()) {
                break;
            }

        }
    }

    private static void distributeOnePlayer(BoardDto boardDto, int winPlayerIdx) {
        List<PlayerDto> players = boardDto.getPlayers();
        PlayerDto winPlayer = players.get(winPlayerIdx);
        List<Integer> totalCallSize = new ArrayList<>(boardDto.getTotalCallSize());
        int wpTotalCallSize = totalCallSize.get(winPlayerIdx);
        GameResultDto gameResult = winPlayer.getGameResult();

        for(int j = winPlayerIdx +1; j < boardDto.getTotalPlayer(); j++) {
            PlayerDto losePlayer = players.get(j);
            int lpTotalCallSize = totalCallSize.get(j);
            int takePotAmount = 0;
            if(wpTotalCallSize <= lpTotalCallSize) {
                takePotAmount = wpTotalCallSize;
            }
            else {
                takePotAmount = lpTotalCallSize;
            }
            totalCallSize.set(j, totalCallSize.get(j) - takePotAmount);
            boardDto.setPot(boardDto.getPot() - takePotAmount);
            gameResult.setEarnedMoney(gameResult.getEarnedMoney() + takePotAmount);
        }
        gameResult.setEarnedMoney(gameResult.getEarnedMoney() + totalCallSize.get(winPlayerIdx));
        totalCallSize.set(winPlayerIdx, 0);

        if (winPlayer.getGameResult().getEarnedMoney() != 0){
            winPlayer.getGameResult().setWinner(true);
        }
    }

    private static void distributeDrawPlayers(BoardDto boardDto, int firstPlayerIdx, long handValue) {
        List<PlayerDto> winPlayerList = new ArrayList<>();
        List<Integer> winPlayerTotalCallSize = new ArrayList<>();

        List<PlayerDto> players = boardDto.getPlayers();
        List<Integer> totalCallSize = new ArrayList<>(boardDto.getTotalCallSize());

        for(int i = firstPlayerIdx; i < players.size(); i++){
            PlayerDto playerDto = players.get(i);
            if(playerDto.getGameResult().getHandValue() == handValue){
                winPlayerList.add(playerDto);
                winPlayerTotalCallSize.add(boardDto.getTotalCallSize().get(i));
            }
        }
        Collections.sort(winPlayerTotalCallSize);
        List<PlayerDto> sortedWinPlayerList = new ArrayList<>();
        for (int wpTotalCallSize : winPlayerTotalCallSize) {
            for (PlayerDto playerDto : winPlayerList) {
                if (boardDto.getTotalCallSize().get(players.indexOf(playerDto)) == wpTotalCallSize) {
                    sortedWinPlayerList.add(playerDto);
                    break;
                }
            }
        }

        winPlayerList = sortedWinPlayerList;

        for(int i = firstPlayerIdx + winPlayerList.size(); i < players.size(); i++) {
            PlayerDto losePlayer = players.get(i);
            for(int j = 0; j < winPlayerList.size(); j++) {
                PlayerDto winPlayer = winPlayerList.get(j);;
                GameResultDto wpGameResult = winPlayer.getGameResult();
                int takePotAmount;

                if(winPlayerTotalCallSize.get(j) <= totalCallSize.get(i) / (winPlayerList.size() - j)) {
                    takePotAmount = winPlayerTotalCallSize.get(j);
                    winPlayer.setMoney(winPlayer.getMoney() + takePotAmount);
                    totalCallSize.set(i, totalCallSize.get(i) - takePotAmount);
                    boardDto.setPot(boardDto.getPot() - takePotAmount);
                    wpGameResult.setEarnedMoney(wpGameResult.getEarnedMoney() + takePotAmount);
                }
                else {
                    takePotAmount = totalCallSize.get(i) / (winPlayerList.size() - j);
                    for(int k = j; k < winPlayerList.size(); k++) {
                        PlayerDto drawPlayer = winPlayerList.get(k);
                        drawPlayer.setMoney(drawPlayer.getMoney() + takePotAmount);
                        boardDto.setPot(boardDto.getPot() - takePotAmount);
                        drawPlayer.getGameResult().setEarnedMoney(drawPlayer.getGameResult().getEarnedMoney() + takePotAmount);
                    }
                    break;
                }
            }
        }

        for(int i = 0; i < winPlayerList.size(); i++){
            PlayerDto winPlayer = winPlayerList.get(i);
            GameResultDto wpGameResult = winPlayer.getGameResult();
            if(wpGameResult.getEarnedMoney() > 0) {
                wpGameResult.setWinner(true);
            }
        }
    }
}