package com.example.pokerv2.service;

import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.PlayerRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void setDisconnect(Long playerId) {
        Optional<Player> disconnectPlayer = playerRepository.findById(playerId);
        if (disconnectPlayer.isPresent()) {
            Player player = disconnectPlayer.get();

            Board board = player.getBoard();
            if (board.getTotalPlayer() == 1) {
                playerRepository.delete(player);
                return;
            }

            if (player.getStatus() == PlayerStatus.FOLD) {
                player.setStatus(PlayerStatus.DISCONNECT_FOLD);
            } else if (player.getStatus() == PlayerStatus.PLAY) {
                player.setStatus(PlayerStatus.DISCONNECT_PLAYED);
            } else if (player.getStatus() == PlayerStatus.ALL_IN) {
                player.setStatus(PlayerStatus.DISCONNECT_ALL_IN);
            }

        }
    }

    @Transactional
    public void setConnect(Principal principal){
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> playerList = user.getPlayerList();

        for(Player player : playerList){
            Board board = player.getBoard();
            if(board.getPhaseStatus().ordinal() >= PhaseStatus.PRE_FLOP.ordinal() && board.getPhaseStatus().ordinal() <= PhaseStatus.RIVER.ordinal()){
                if(player.getStatus() == PlayerStatus.DISCONNECT_FOLD){
                    player.setStatus(PlayerStatus.FOLD);
                }else if(player.getStatus() == PlayerStatus.DISCONNECT_PLAYED){
                    player.setStatus(PlayerStatus.PLAY);
                }else if(player.getStatus() == PlayerStatus.DISCONNECT_ALL_IN){
                    player.setStatus(PlayerStatus.ALL_IN);
                }
            }
        }
        playerRepository.saveAll(playerList);
    }
}
