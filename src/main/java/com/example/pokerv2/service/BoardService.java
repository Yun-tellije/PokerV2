package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.*;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.PlayerRepository;
import com.example.pokerv2.repository.UserRepository;
import com.example.pokerv2.utils.HandCalculatorUtils;
import com.example.pokerv2.utils.PotDistributorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    private static final int MAX_PLAYER = 6;

    /**
     * join - 24/01/04 chan
     * 1. 플레이 가능한 보드를 찾는다.
     * 2. money를 bb로 환전한다.
     * 3. Player 입장.
     */

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<BoardDto> getBoardList(int blind) {
        List<Board> boardList = boardRepository.findByBlind(blind);
        List<BoardDto> boardDtoList = new ArrayList<>();

        for (Board board : boardList) {
            boardDtoList.add(new BoardDto(board));
        }

        return boardDtoList;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto joinRandom(int blind, int requestBb, Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board;

        List<Board> playableBoard = boardRepository.findFirstPlayableBoard(user.getId(), blind, PageRequest.of(0, 1));

        if (playableBoard.size() != 0)
            board = playableBoard.get(0);

        else board = Board.builder().blind(blind).phaseStatus(PhaseStatus.WAITING).gameSeq(0L).build();
        Player player = buyIn(board, user, requestBb);
        sitIn(board, player);
        boardRepository.save(board);
        return new BoardDto(board);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto join(Long boardId, int requestBb, Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();

        for (Player player : players) {
            if(user.equals(player.getUser()))
                throw new CustomException(ErrorCode.ALREADY_PLAYING_BOARD);
        }

        if(board.getTotalPlayer() >= MAX_PLAYER) {
            throw new CustomException(ErrorCode.MAX_PLAYER_SIZE);
        }

        Player player = buyIn(board, user, requestBb);
        sitIn(board, player);
        boardRepository.save(board);
        return new BoardDto(board);
    }

    public Player buyIn(Board board, User user, int bb) {

        int money = user.getMoney();
        int blind = board.getBlind();
        if (money < blind * bb)
            throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

        user.setMoney(user.getMoney() - blind * bb);
        Player player = Player.builder().money(blind * bb).board(board).status(PlayerStatus.FOLD).user(user).build();

        return playerRepository.save(player);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Board setNextAction(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        int nextActionPos = getNextActionPos(board);
        board.setActionPos(nextActionPos);
        board.setLastActionTime(LocalDateTime.now());
        return board;
    }

    public boolean isPhaseEnd(Board board) {
        if (board.getBettingPos() == board.getActionPos()) {
            return true;
        }
        return false;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public String getCurrentActionUserId(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Player player = board.getPlayers().get(getPlayerIdxByPos(board, board.getActionPos()));

        return player.getUser().getUserId();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public boolean isActionPlayerConnect(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        int actionPlayerIdx = getPlayerIdxByPos(board, board.getActionPos());
        Player player = players.get(actionPlayerIdx);

        if (player.getStatus().getStatusNum() >= PlayerStatus.FOLD.getStatusNum())
            return true;

        return false;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public BoardDto getBoard(Long boardId) {
        return new BoardDto(boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST)));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Board saveBoardChanges(BoardDto boardDto, String option, String userId) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        if (!isSeatInBoard(board, userId))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        for (PlayerDto playerDto : boardDto.getPlayers()) {
            Player p = playerRepository.findById(playerDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
            if (p.getUser().getUserId().equals(userId)) {

                p.setMoney(playerDto.getMoney());
                p.setPhaseCallSize(playerDto.getPhaseCallSize());

                if (p.getStatus().getStatusNum() < PlayerStatus.FOLD.getStatusNum()) {
                    if (option.equals(PlayerAction.FOLD.getActionDetail())) {
                        p.setStatus(PlayerStatus.DISCONNECT_FOLD);
                    } else if (option.equals(PlayerAction.ALL_IN_CALL.getActionDetail()) || option.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
                        p.setStatus(PlayerStatus.DISCONNECT_ALL_IN);
                    } else if (option.equals(PlayerAction.CALL.getActionDetail()) || option.equals(PlayerAction.RAISE.getActionDetail())) {
                        p.setStatus(PlayerStatus.DISCONNECT_PLAYED);
                    }
                } else {
                    if (option.equals(PlayerAction.FOLD.getActionDetail())) {
                        p.setStatus(PlayerStatus.FOLD);
                    } else if (option.equals(PlayerAction.ALL_IN_CALL.getActionDetail()) || option.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
                        p.setStatus(PlayerStatus.ALL_IN);
                    } else if (option.equals(PlayerAction.CALL.getActionDetail()) || option.equals(PlayerAction.RAISE.getActionDetail())) {
                        p.setStatus(PlayerStatus.PLAY);
                    }
                }
                break;
            }
        }
        saveBoardChanges(board, boardDto);
        return board;
    }


    public void saveBoardChanges(Board board, BoardDto boardDto) {
        board.setPot(boardDto.getPot());
        board.setBettingPos(boardDto.getBettingPos());
        board.setActionPos(boardDto.getActionPos());
        board.setBettingSize(boardDto.getBettingSize());
        boardRepository.save(board);
    }

    public int getNextActionPos(Board board) {
        List<Player> players = board.getPlayers();
        int currentActPlayerIdx = getPlayerIdxByPos(board, board.getActionPos());
        for (int i = 1; i < players.size(); i++) {
            Player player = players.get((currentActPlayerIdx + i) % players.size());
            PlayerStatus status = player.getStatus();

            if (player.getPosition().getPosNum() == board.getBettingPos()) {
                return -1;
            }

            if (status == PlayerStatus.PLAY || status == PlayerStatus.DISCONNECT_PLAYED) {
                return player.getPosition().getPosNum();
            }
        }

        return -1;
    }

    /**
     * GameEnd를 만족하는 상황인지 체크한다.
     *
     * @param boardId
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public boolean isGameEnd(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        int actionableCount = 0;
        for (Player player : board.getPlayers()) {
            if (player.getStatus() == PlayerStatus.PLAY || player.getStatus() == PlayerStatus.DISCONNECT_PLAYED) {
                actionableCount++;
            }
        }

        if (actionableCount < 2) {
            return true;
        }

        return false;
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void initBoard(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        board.setPot(0);
        board.setBettingSize(0);
        board.setBettingPos(0);
        board.setActionPos(0);
        board.setPhaseStatus(PhaseStatus.WAITING);
        board.setCommunityCard1(0);
        board.setCommunityCard2(0);
        board.setCommunityCard3(0);
        board.setCommunityCard4(0);
        board.setCommunityCard5(0);
        for (Player player : players) {
            player.setCard1(0);
            player.setCard2(0);
            player.setPhaseCallSize(0);
            player.setStatus(PlayerStatus.FOLD);
        }
        List<Integer> totalCallSizeList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));
        board.setTotalCallSize(totalCallSizeList);
        boardRepository.save(board);
    }

    @Transactional
    public void dropDisconnectPlayers(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        List<Player> disConnectedPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getStatus().getStatusNum() < PlayerStatus.FOLD.getStatusNum()) {
                User user = player.getUser();
                user.setMoney(user.getMoney() + player.getMoney());
                disConnectedPlayers.add(player);
            }
        }
        players.removeAll(disConnectedPlayers);
        board.setTotalPlayer(players.size());
        playerRepository.deleteAll(disConnectedPlayers);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public List<PlayerDto> chargeMoney(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        List<PlayerDto> unChargePlayerList = new ArrayList<>();

        for (Player player : players) {
            if(player.getMoney() <= board.getBlind() * 100) {
                int money = board.getBlind() * 100 - player.getMoney();
                User user = player.getUser();
                if(user.getMoney() < money) {
                    unChargePlayerList.add(new PlayerDto(player));
                    continue;
                }
                user.setMoney(user.getMoney() - money);
                player.setMoney(player.getMoney() + money);
            }
        }

        return unChargePlayerList;
    }

    @Transactional
    public void dropMoneyLessPlayers(Long boardId, List<PlayerDto> moneyLessPlayers) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        for (PlayerDto moneyLessPlayer : moneyLessPlayers) {
            Player player = playerRepository.findById(moneyLessPlayer.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
            User user = player.getUser();
            user.setMoney(user.getMoney() + player.getMoney());
        }

        board.setTotalPlayer(board.getTotalPlayer() - moneyLessPlayers.size());
    }

    @Transactional
    public boolean isShowDown(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        int foldCount = 0;
        List<Player> players = board.getPlayers();
        for (Player player : players) {
            if(player.getStatus().equals(PlayerStatus.FOLD) || player.getStatus().equals(PlayerStatus.DISCONNECT_FOLD)) {
                foldCount++;
            }
        }

        if(foldCount == board.getTotalPlayer() - 1) {
            return false;
        }

        return true;
    }

    @Transactional
    public void refundOverBet(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        int bettingPlayerIdx = getPlayerIdxByPos(board, board.getBettingPos());
        int bettingSize = board.getBettingSize();
        int maxCallSize = -1;
        List<Player> players = board.getPlayers();


        for (int i = bettingPlayerIdx + 1; i < bettingPlayerIdx + 1 + board.getTotalPlayer(); i++) {
            Player player = players.get(i % board.getTotalPlayer());
            if (player.getPhaseCallSize() == bettingSize)
                return;
            else if (maxCallSize < player.getPhaseCallSize()) {
                maxCallSize = player.getPhaseCallSize();
            }
        }

        Player overBetPlayer = players.get(bettingPlayerIdx);
        overBetPlayer.setMoney(overBetPlayer.getMoney() + bettingSize - maxCallSize);
        overBetPlayer.setPhaseCallSize(maxCallSize);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto winOnePlayer(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        board.setPhaseStatus(PhaseStatus.END_GAME);
        List<Player> players = board.getPlayers();
        BoardDto boardDto = new BoardDto(board);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            PlayerDto playerDto = boardDto.getPlayers().get(i);
            if (player.getStatus() != PlayerStatus.FOLD && player.getStatus() != PlayerStatus.DISCONNECT_FOLD) {
                playerDto.setGameResult(
                        GameResultDto.builder().isWinner(true)
                                .earnedMoney(board.getPot())
                                .build()
                );
                player.setMoney(player.getMoney() + board.getPot());

            } else {
                playerDto.setGameResult(
                        GameResultDto.builder()
                                .isWinner(false).build());
            }
        }

        boardRepository.save(board);
        return boardDto;
    }

    /**
     * 쇼다운
     * <p>
     * 1. 오버벳 반환
     * 2. 승자 가리기
     * 3. 팟 분배하기 (사이드 팟 생각)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto showDown(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        board.setPhaseStatus(PhaseStatus.SHOWDOWN);
//        refundOverBet(board);
        initPhase(boardId);
        BoardDto boardDto = determineWinner(board);
        PotDistributorUtils.distribute(boardDto);

        for (PlayerDto playerDto : boardDto.getPlayers()) {
            GameResultDto gameResult = playerDto.getGameResult();
            if(gameResult.isWinner()) {
                Player player = players.get(getPlayerIdxByPos(board, playerDto.getPosition()));
                player.setMoney(player.getMoney() + gameResult.getEarnedMoney());
            }
        }
        playerRepository.saveAll(players);
        return boardDto;
    }
    private static BoardDto determineWinner(Board board) {
        List<Player> players = board.getPlayers();
        List<Integer> communityCards = new ArrayList<>(List.of(board.getCommunityCard1(), board.getCommunityCard2(), board.getCommunityCard3(), board.getCommunityCard4(), board.getCommunityCard5()));
        GameResultDto gameResultDto;
        BoardDto boardDto = new BoardDto(board);

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);

            if (player.getStatus() == PlayerStatus.FOLD) {
                gameResultDto = GameResultDto.builder().isWinner(false).build();
            } else {
                ArrayList<Integer> cardPool = new ArrayList<>(List.copyOf(communityCards));
                cardPool.add(player.getCard1());
                cardPool.add(player.getCard2());
                gameResultDto = HandCalculatorUtils.calculateValue(cardPool);
            }
            boardDto.getPlayers().get(i).setGameResult(gameResultDto);
        }
        return boardDto;
    }

    /**
     * initBet
     * <p>
     * 페이즈가 끝났을 때 각 플레이어들의 베팅을 팟에 모음.
     *
     * @param boardId
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void initPhase(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        List<Integer> totalCallSize = board.getTotalCallSize();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            totalCallSize.set(i, totalCallSize.get(i) + player.getPhaseCallSize());
            board.setPot(board.getPot() + player.getPhaseCallSize());
            player.setPhaseCallSize(0);
        }

        board.setBettingSize(0);
    }

    /**
     * 다음 페이즈를 시작한다.
     *
     * @param boardId
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto nextPhase(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (board.getPhaseStatus() == PhaseStatus.PRE_FLOP) {
            prepareNextPhase(board);
            board.setPhaseStatus(PhaseStatus.FLOP);
        } else if (board.getPhaseStatus() == PhaseStatus.FLOP) {
            prepareNextPhase(board);
            board.setPhaseStatus(PhaseStatus.TURN);
        } else if (board.getPhaseStatus() == PhaseStatus.TURN) {
            prepareNextPhase(board);
            board.setPhaseStatus(PhaseStatus.RIVER);
        } else if (board.getPhaseStatus() == PhaseStatus.RIVER) {
            board.setPhaseStatus(PhaseStatus.SHOWDOWN);
        }
        boardRepository.save(board);
        return new BoardDto(board);
    }


    /**
     * 다음 페이즈에 진입하기 전에 보드 상태를 초기화한다.
     *
     * @return
     */
    private Board prepareNextPhase(Board board) {
        List<Player> players = board.getPlayers();
        int betPos = players.get((getPlayerIdxByPos(board, board.getBtn()) + 1) % players.size()).getPosition().getPosNum();
        initPhase(board.getId());
        board.setActionPos(betPos);
        board.setBettingPos(betPos);
        board.setLastActionTime(LocalDateTime.now());
        return boardRepository.save(board);
    }

    private void saveAction(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        PlayerDto actPlayer = players.get(boardDto.getActionPos());
        if (actPlayer.getStatus() == PlayerStatus.FOLD.ordinal()) {
            //fold

        } else if (actPlayer.getStatus() == PlayerStatus.ALL_IN.ordinal()) {
            //all-in
        } else if (actPlayer.getStatus() == PlayerStatus.PLAY.ordinal() && boardDto.getActionPos() == boardDto.getBettingPos()) {
            //bet
        } else if (actPlayer.getStatus() == PlayerStatus.PLAY.ordinal() && actPlayer.getPhaseCallSize() == boardDto.getBettingSize()) {
            //call
        } else throw new CustomException(ErrorCode.BAD_REQUEST);
    }


    /**
     * 첫번째 베팅 순서를 어떻게 정할까?
     * 1. 프리플랍 : BB 다음 포지션
     * 2. 프리플랍 이후 : btn 다음 포지션
     * <p>
     * 주의 해야 할 것.
     * 각 포지션이 모두 차있다고 생각하지 말 것.
     * 예를들어서 btn 포지션에 무조건 사람이 앉아있는 것이 아님.
     *
     * @param boardId
     * @return
     */

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public BoardDto startGame(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        setBtnExistPlayer(board);
        takeAnte(board);
        setFirstActionPos(board);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        dealCard(board);

        for(int i = 0; i < board.getPlayers().size(); i++) {
            Player player = board.getPlayers().get(i);
            player.setStatus(PlayerStatus.PLAY);
        }

        board.setGameSeq(board.getGameSeq() + 1);
        return new BoardDto(boardRepository.saveAndFlush(board));
    }


    public void setBtnExistPlayer(Board board) {
        List<Player> players = board.getPlayers();

        int nextBtn = (board.getBtn() + 1) % MAX_PLAYER;

        while (true) {
            boolean isExist = false;
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if (player.getPosition().getPosNum() == nextBtn) {
                    board.setBtn(nextBtn);
                    isExist = true;
                }
            }
            if (isExist) {
                break;
            }
            nextBtn = (nextBtn + 1) % MAX_PLAYER;
        }
    }

    public void takeAnte(Board board) {
        List<Player> players = board.getPlayers();
        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        if (btnPlayerIdx != -1) {
            if (board.getTotalPlayer() != 2) {
                Player player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 2) % players.size());
                Player firstActionPlayer = players.get((btnPlayerIdx + 3) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                    board.setBettingPos(firstActionPlayer.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

            } else {
                Player player = players.get(btnPlayerIdx);
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                    board.setBettingPos(player.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
            }
            board.setBettingSize(board.getBlind());
            board.setLastActionTime(LocalDateTime.now());
        } else throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);

    }

    public int getPlayerIdxByPos(Board board, int posNum) {
        List<Player> players = board.getPlayers();

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == posNum)
                return i;
        }

        return -1;
    }

    private void setFirstActionPos(Board board) {

        if (board.getTotalPlayer() == 2) {
            board.setActionPos(board.getBtn());
        } else {
            board.setActionPos(board.getBettingPos());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public BoardDto get(Long boardId, Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boolean isAuthenticated = false;
        for (Player p : board.getPlayers()) {
            if (p.getUser().equals(user)) {
                isAuthenticated = true;
                break;
            }
        }

        if (!isAuthenticated)
            return null;
        return new BoardDto(board);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<BoardDto> getContext(Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> playerList = user.getPlayerList();
        List<BoardDto> context = new ArrayList<>();
        for (Player player : playerList) {
            context.add(new BoardDto(player.getBoard()));
        }
        return context;
    }

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
                players.add(joinPlayer);
                board.setTotalPlayer(board.getTotalPlayer() + 1);
                break;
            }
        }
    }
    public void setBtnPrevPlayer(Board board) {

        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        int prevPlayerIdx;
        if (btnPlayerIdx - 1 < 0) {
            prevPlayerIdx = board.getPlayers().size() - 1;
        } else {
            prevPlayerIdx = btnPlayerIdx - 1;
        }
        Position prevPlayerPos = board.getPlayers().get(prevPlayerIdx).getPosition();
        board.setBtn(prevPlayerPos.getPosNum());
    }

    public void setBettingPosNextPlayer(Board board) {

        int betPlayerIdx = getPlayerIdxByPos(board, board.getBettingPos());
        int nextPlayerIdx;
        if (betPlayerIdx + 1 <= board.getTotalPlayer()) {
            nextPlayerIdx = 0;
        } else {
            nextPlayerIdx = betPlayerIdx + 1;
        }

        Position updateBetPos = board.getPlayers().get(nextPlayerIdx).getPosition();
        board.setBettingPos(updateBetPos.getPosNum());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sitOut(BoardDto boardDto, String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();

        Optional<Player> exitPlayer = Optional.empty();
        for (Player player : players) {
            if (player.getUser().equals(user)) {

                exitPlayer = Optional.of(playerRepository.findById(player.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST)));
                break;
            }
        }

        if (exitPlayer.isPresent()) {
            Player player = exitPlayer.get();
            if (player.getPosition().getPosNum() == board.getBtn()) {
                setBtnPrevPlayer(board);
            }

            if (board.getBettingPos() == player.getPosition().getPosNum()) {
                setBettingPosNextPlayer(board);
            }

            user.setMoney(user.getMoney() + player.getMoney());
            if (board.getPhaseStatus() != PhaseStatus.WAITING && player.getPhaseCallSize() != 0) {
                board.setPot(board.getPot() + player.getPhaseCallSize());
            }

            players.remove(player);
            board.setPlayers(players);
            board.setTotalPlayer(board.getTotalPlayer() - 1);
            playerRepository.delete(player);
            boardRepository.save(board);
        }

    }

    @Transactional
    public boolean isSeatInBoard(Board board, String userId) {
        List<Player> players = board.getPlayers();
        for (Player player : players) {
            if (player.getUser().getUserId().equals(userId))
                return true;
        }
        return false;
    }

    private void dealCard(Board board) {
        Set<Integer> cards = new HashSet<>();
        Random random = new Random();
        int playerCardIdxPrefix = 5;
        int cardSize = board.getTotalPlayer() * 2 + 5;

        while (cards.size() < cardSize) {
            int randomNumber = random.nextInt(52);
            cards.add(randomNumber);
        }

        List<Integer> cardList = new ArrayList<>(cards);
        board.setCommunityCard1(cardList.get(0));
        board.setCommunityCard2(cardList.get(1));
        board.setCommunityCard3(cardList.get(2));
        board.setCommunityCard4(cardList.get(3));
        board.setCommunityCard5(cardList.get(4));

        List<Player> players = board.getPlayers();

        for (Player player : players) {
            int playerCard = cardList.get(playerCardIdxPrefix);
            int playerCard2 = cardList.get(playerCardIdxPrefix + 1);
            player.setCard1(playerCard);
            player.setCard2(playerCard2);
            playerCardIdxPrefix += 2;
        }
    }
}
