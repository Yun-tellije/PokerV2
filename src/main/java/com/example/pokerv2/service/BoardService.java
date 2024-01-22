package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.enums.MessageType;
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
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int MAX_PLAYER = 6;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ActionRepository actionRepository;
    private final static String TOPIC_PREFIX = "/topic/board/";
    private final static int RESULT_ANIMATION_DELAY = 5;

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
            board = Board.builder().blind(1000).phaseStatus(PhaseStatus.WAITING).build();
        }
//        board.setTotalPlayer(board.getTotalPlayer()+1);

//        List<Player> players = board.getPlayers();

        board = boardRepository.save(board);

        Player player = buyIn(board, user, requestBb);
        sitIn(board, player);

//        players.add(player);
        playerRepository.save(player);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_JOIN.getDetail(), new BoardDto(board)));

        if(board.getTotalPlayer() > 1 && board.getPhaseStatus().equals(PhaseStatus.WAITING)){
            board = startGame(board.getId());
        }

        System.out.println("board.getTotalPlayer() = " + board.getTotalPlayer());
        // sitout test(성공)
//        if(board.getTotalPlayer() == 2){
//            sitOut(new BoardDto(board), principal);
//            // 남은 플레이어의 status는 그대로 유지됨 -> board 초기화 할때 한번에 손대기
//        }

        return new BoardDto(board);
    }

//    public Position pos(Board board){
//        List<Player> players = board.getPlayers();
//        // 플레이어 각자 포지션이 일치하는지 확인
//        // 없는거 리턴
//        // Position 열거랑 비교
//        int[] check = new int[Position.values().length];
//
//        for(int i=0; i<check.length; i++){
//            check[i] = 0;
//        }
//
//        for(int i=0; i<players.size(); i++){
//            Player player = players.get(i);
//            int j=0;
//            for(Position temp : Position.values()) {
//                if(player.getPosition() == temp) {
//                    check[j]++;
//                }
//                j++;
//            }
//        }
//        for(int i=0; i< check.length; i++){
//            if(check[i]==0){
//                System.out.println("포지션 체크");
//                System.out.println(Position.values()[i]);
//                return Position.values()[i];
//            }
//        }
//        return null;
//    }

    @Transactional
    public void sitIn(Board board, Player joinPlayer) {
        List<Player> players = board.getPlayers();
        boolean[] isExistSeat = new boolean[MAX_PLAYER];
        Random random = new Random();
        int pos = random.nextInt(MAX_PLAYER);

        for (Player player : players) {
            isExistSeat[player.getPosition().ordinal()] = true;
        }

        for (int i = 0; i < MAX_PLAYER; i++) {
            if (isExistSeat[pos]) {
                pos = (pos + 1) % MAX_PLAYER;
            } else {
                joinPlayer.setPosition(Position.getPositionByNumber(pos));
                System.out.println(joinPlayer.getPosition());
                players.add(joinPlayer);
                board.setTotalPlayer(board.getTotalPlayer() + 1);
                break;
            }
        }
    }

    @Transactional
    // 참가비 걷기
    public Player buyIn(Board board, User user, int bb){
        int money = user.getMoney();
        int blind = board.getBlind();
        if (money < blind * bb)
            throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

        user.setMoney(user.getMoney() - blind * bb);
        Player player = Player.builder().money(blind * bb).board(board).status(PlayerStatus.FOLD).user(user).build();

        return playerRepository.save(player);
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

    @Transactional
    public void sitOut(BoardDto boardDto, Principal principal){
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();

        Player exitPlayer;

        for(Player player : players){
            exitPlayer = playerRepository.findById(player.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
            if(player.getUser().equals(user)){
                exitPlayer = playerRepository.findById(player.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

                players.remove(exitPlayer);
                System.out.println("플레이어 퇴장");
                board.setTotalPlayer(board.getTotalPlayer() - 1);
                System.out.println("board.getTotalPlayer() = " + board.getTotalPlayer());
                playerRepository.delete(exitPlayer);
                break;
            }
        }
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_EXIT.getDetail(), new BoardDto(board)));

        // board에서 totalplayer를 한명 줄였을 때 남은 인원이 한명이라면 게임 종료
        if(board.getTotalPlayer() == 1){
            endGame(board);
        }

    }

    // 보드에 남은 인원이 1명일 때, 게임이 끝났을 때(마지막 라운드까지 간 경우, 한명을 제외한 플레이어들이 FOLD인 경우)일 때 호출
    public void endGame(Board board) {
        // 1) 플레이어들이 FOLD일 때 -> 우승자를 찾아야함
        // 2) 보드에 남은 인원이 1명일 때, 한명을 제외한 플레이어들이 FOLD일 때 -> 카드 공개(showdown)

        int foldCount = 0;
        List<Player> players = board.getPlayers();

        for (Player player : players) {
            if (player.getStatus() == PlayerStatus.FOLD) {
                foldCount++;
            }
        }

        if (foldCount == board.getTotalPlayer() - 1) {
            // TODO 우승자 정해주는 메소드
        } else {
            // TODO showdown 메소드
        }
    }

}