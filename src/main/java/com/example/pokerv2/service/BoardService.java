package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.ActionRepository;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.PlayerRepository;
import com.example.pokerv2.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int MAX_PLAYER = 6;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ActionRepository actionRepository;

    /**
     * 게임 입장 서비스
     *
     * 1. 방 입장
     *  1) 게임을 대기중인 방으로 입장
     *  2) 최대 6명
     *  3) 돈 있는지 확인
     *
     * 2. 6명이 됐을 때 게임 시작
     */

    @Transactional
    // 들어갈 수 있는지 방 찾기
    // 입장할 때 최대 인원을 초과하는지 확인이 필요??
    public BoardDto join(int requestBb, Principal principal){
        Board board;
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        List<Board> playableBoard = boardRepository.findFirstPlayableBoard(user.getId(), PageRequest.of(0,1));
        // 1. 방이 있는지 확인
        //  1) 있으면 들어가기
        //  2) 없으면 생성

        if(!playableBoard.isEmpty()){
            board = playableBoard.get(0);
        }
        else{
            board = Board.builder().phaseStatus(PhaseStatus.WAITING).build();
        }
        board.setTotalPlayer(board.getTotalPlayer()+1);

        List<Player> players = board.getPlayers();

        Position position = pos(board);
        board = boardRepository.save(board);

        Player player = buyIn(board, user, requestBb);
                //Player.builder().user(user).board(board).status(PlayerStatus.FOLD).build();
        players.add(player);
        player.setPosition(position);
        playerRepository.save(player);

        if(board.getTotalPlayer() > 1 && board.getPhaseStatus().equals(PhaseStatus.WAITING)){
            board = startGame(board.getId());
        }

        return new BoardDto(board);
    }

    public Position pos(Board board){
        List<Player> players = board.getPlayers();
        // 플레이어 각자 포지션이 일치하는지 확인
        // 없는거 리턴
        // Position 열거랑 비교
        int[] check = new int[Position.values().length];

        for(int i=0; i<check.length; i++){
            check[i] = 0;
        }

        for(int i=0; i<players.size(); i++){
            Player player = players.get(i);
            int j=0;
            for(Position temp : Position.values()) {
                if(player.getPosition() == temp) {
                    check[j]++;
                }
                j++;
            }
        }
        for(int i=0; i< check.length; i++){
            if(check[i]==0){
                System.out.println("포지션 체크");
                System.out.println(Position.values()[i]);
                return Position.values()[i];
            }
        }
        return null;
    }

    @Transactional
    // 참가비 걷기
    public Player buyIn(Board board, User user, int bb){
        if(user.getMoney() < board.getBlind() * bb){
            throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        user.setMoney(user.getMoney() - board.getBlind() * bb);
        return Player.builder().user(user).board(board).money(board.getBlind()*bb).status(PlayerStatus.FOLD).build();
    }

    private void setCommunityCard(Board board, int order, int card) {
        switch (order) {
            case 1 -> board.setCommunityCard1(card);
            case 2 -> board.setCommunityCard2(card);
            case 3 -> board.setCommunityCard3(card);
            case 4 -> board.setCommunityCard4(card);
            case 5 -> board.setCommunityCard5(card);
        };
    }

    @Transactional
    public Board startGame(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        for(Player player : board.getPlayers()){
            player.setStatus(PlayerStatus.PLAY);
        }

        boardRepository.save(board);
        // 버튼 누르는 로직 추가
        return board;
    }
}