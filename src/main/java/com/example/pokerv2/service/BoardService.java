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
import java.time.LocalDateTime;
import java.util.*;

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

        board = boardRepository.save(board);

        Player player = buyIn(board, user, requestBb);
        sitIn(board, player);

        playerRepository.save(player);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_JOIN.getDetail(), new BoardDto(board)));

//        if(board.getTotalPlayer() > 1 && board.getPhaseStatus().equals(PhaseStatus.WAITING)){
//            board = startGame(board.getId());
//        }

        System.out.println("board.getTotalPlayer() = " + board.getTotalPlayer());
        // sitout test(성공)
//        if(board.getTotalPlayer() == 2){
//            sitOut(new BoardDto(board), principal);
//            // 남은 플레이어의 status는 그대로 유지됨 -> board 초기화 할때 한번에 손대기
//        }

        return new BoardDto(board);
    }

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

    @Transactional
    public Board startGame(Long boardId){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        setBtnExistPlayer(board);
        takeAnte(board);
        setFirstActionPos(board);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        dealCard(board);

        for(Player player : board.getPlayers()){
            player.setStatus(PlayerStatus.PLAY);
        }

        boardRepository.save(board);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardId, new MessageDto(MessageType.GAME_START.getDetail(), new BoardDto(board)));
        return board;
    }

    //
    public void setBtnExistPlayer(Board board){
       List<Player> players = board.getPlayers();

       int nextBtn = (board.getBtn() + 1 ) % MAX_PLAYER;

       while(true){
           boolean isExist = false;
           for(int i=0; i<board.getTotalPlayer(); i++){
               Player player = players.get(i);
               if(player.getPosition().getPosNum() == nextBtn){
                   board.setBtn(nextBtn);
                   isExist = true;
               }

           }
           if(isExist == true) {
               break;
           }
            nextBtn = (nextBtn+1) % MAX_PLAYER;
       }

    }

    public void takeAnte(Board board){
        List<Player> players = board.getPlayers();
        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());

        if(btnPlayerIdx != -1){
            if(board.getTotalPlayer() == 2){ //
                Player player = players.get(btnPlayerIdx);
                if(player.getMoney() > board.getBlind()){
                    player.setMoney((int)(player.getMoney() - 0.5 * board.getBlind()));
                    player.setPhaseCallSize((int)(board.getBlind() * 0.5));
                    board.setBettingPos(player.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get(btnPlayerIdx + 1);
                if(player.getMoney() > board.getBlind()){
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
            }else{
                Player player = players.get(btnPlayerIdx + 1);
                if(player.getMoney() > board.getBlind()){
                    player.setMoney((int)(player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int)(board.getBlind() * 0.5));
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
                player = players.get(btnPlayerIdx + 2);
                Player firstActionPlayer = players.get(btnPlayerIdx + 3);
                if(player.getMoney() > board.getBlind()){
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                    board.setBettingPos(firstActionPlayer.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
            }
            board.setLastActionTime(LocalDateTime.now());
        }
    }

    private int getPlayerIdxByPos(Board board, int posNum){ // posNum은 btn
        List<Player> players = board.getPlayers();

        for(int i=0; i<board.getTotalPlayer(); i++){
            Player player = players.get(i);
            if(player.getPosition().getPosNum() == posNum){
                return i;
            }
        }
        return -1;
    }

    private void setFirstActionPos(Board board){

        if(board.getTotalPlayer() == 2){
            board.setActionPos(board.getBtn());
        } else{
            board.setActionPos(board.getBettingPos());
        }
    }

    @Transactional
    public void nextPhase(Board board){
//        beforeNextPhase(board);

        if(board.getPhaseStatus() == PhaseStatus.PRE_FLOP){
            board.setPhaseStatus(PhaseStatus.FLOP);
        } else if(board.getPhaseStatus() == PhaseStatus.FLOP){
            board.setPhaseStatus(PhaseStatus.TURN);
        } else if(board.getPhaseStatus() == PhaseStatus.TURN){
            board.setPhaseStatus(PhaseStatus.RIVER);
        } else if(board.getPhaseStatus() == PhaseStatus.RIVER){
            board.setPhaseStatus(PhaseStatus.SHOWDOWN);
        }
    }

    //
//    private Board beforeNextPhase(Board board){
//        List<Player> players = board.getPlayers();
//
//
//
//    }

    // 플레이어들의 카드 지정
    public void dealCard(Board board){
        Set<Integer> cards = new HashSet<>();
        Random random = new Random();
        int cardSize = board.getTotalPlayer() * 2 + 5;

//        for(int i=0; i<cardSize; i++){
//            int randonNum = random.nextInt(52);
//            cards.add(randonNum);
//        }
        // set이라서 for문 해버리면 만약 중복된 카드가 나왔을 때 add가 안되어서 카드 부족 -> 오류 발생

        while(cards.size() < cardSize){
            int randomNum = random.nextInt(52);
            cards.add(randomNum);
        }

        List<Integer> card = new ArrayList<>(cards);
        board.setCommunityCard1(card.get(0));
        board.setCommunityCard2(card.get(1));
        board.setCommunityCard3(card.get(2));
        board.setCommunityCard4(card.get(3));
        board.setCommunityCard5(card.get(4));

        List<Player> players = board.getPlayers();

        int idx = 5;
        for(Player player : players){
            player.setCard1(card.get(idx));
            player.setCard2(card.get(idx+1));

            idx += 2;
        }

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

    // 보드 및 카드 초기화
    public void initBoard(Board board){

    }
}