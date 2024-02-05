package com.example.pokerv2.utils;

import com.example.pokerv2.dto.GameResultDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SameJokBoCompareTest {

    private static final int CARD_SIZE = 7;
    private static final long JOKBO_DIVIDE_CONSTANT = 10000000000L;

    @Test
    @DisplayName("같은 스트레이트 플러시일 때 더 높은 스트레이트를 가진 플레이어가 승자가 되는지 테스트")
    void compareStraightFlushValue() {

        //given
        ArrayList<Integer> winStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("9","d"),
                CardUtils.getCardValue("8","d"),
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d")
        ));

        ArrayList<Integer> loseStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d"),
                CardUtils.getCardValue("5","d"),
                CardUtils.getCardValue("4","d"),
                CardUtils.getCardValue("3","d")
        ));


        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winStraightFlushJokBo);
        Collections.sort(loseStraightFlushJokBo);

        winPlayerCardList.addAll(winStraightFlushJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2","s"));
        winPlayerCardList.add(CardUtils.getCardValue("3","s"));

        losePlayerCardList.addAll(loseStraightFlushJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2","s"));
        losePlayerCardList.add(CardUtils.getCardValue("3","s"));


        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT).isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 포카드일 때 더 높은 포카드의 랭크를 가진 플레이어의 핸드가치가 더 높게 평가되는지 테스트")
    void compareRankWithSameFourOfAKind() {

        //given
        ArrayList<Integer> winStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("10","c"),
                CardUtils.getCardValue("10","h"),
                CardUtils.getCardValue("10","s"),
                CardUtils.getCardValue("J","d")
        ));

        ArrayList<Integer> loseStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("7","c"),
                CardUtils.getCardValue("7","h"),
                CardUtils.getCardValue("7","s"),
                CardUtils.getCardValue("A","d")
        ));


        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winStraightFlushJokBo);
        Collections.sort(loseStraightFlushJokBo);

        winPlayerCardList.addAll(winStraightFlushJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2","s"));
        winPlayerCardList.add(CardUtils.getCardValue("3","s"));

        losePlayerCardList.addAll(loseStraightFlushJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2","s"));
        losePlayerCardList.add(CardUtils.getCardValue("3","s"));


        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT).isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 포카드일 때 더 높은 하이카드를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareHighCardWithSameFourOfAKind() {

        //given
        ArrayList<Integer> winStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("10","c"),
                CardUtils.getCardValue("10","h"),
                CardUtils.getCardValue("10","s"),
                CardUtils.getCardValue("A","d")
        ));

        ArrayList<Integer> loseStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("10","c"),
                CardUtils.getCardValue("10","h"),
                CardUtils.getCardValue("10","s"),
                CardUtils.getCardValue("J","d")
        ));


        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winStraightFlushJokBo);
        Collections.sort(loseStraightFlushJokBo);

        winPlayerCardList.addAll(winStraightFlushJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2","s"));
        winPlayerCardList.add(CardUtils.getCardValue("3","s"));

        losePlayerCardList.addAll(loseStraightFlushJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2","s"));
        losePlayerCardList.add(CardUtils.getCardValue("3","s"));


        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT).isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 풀하우스일 때 더 높은 트리플의 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareTripleRankWithSameFullHouse() {

        //given
        ArrayList<Integer> winFullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> loseFullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7", "d"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("7", "h"),
                CardUtils.getCardValue("6", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winFullHouseJokBo);
        Collections.sort(loseFullHouseJokBo);

        winPlayerCardList.addAll(winFullHouseJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseFullHouseJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 풀하우스일 때 더 높은 페어의 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void comparePairRankWithSameFullHouse() {

        //given
        ArrayList<Integer> winFullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> loseFullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("6", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winFullHouseJokBo);
        Collections.sort(loseFullHouseJokBo);

        winPlayerCardList.addAll(winFullHouseJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseFullHouseJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 플러시 일 때 더 높은 플러시를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareRankWithSameFlush() {

        //given
        ArrayList<Integer> winFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "d"),
                CardUtils.getCardValue("8", "d"),
                CardUtils.getCardValue("7", "d"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> loseFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7", "d"),
                CardUtils.getCardValue("6", "d"),
                CardUtils.getCardValue("5", "d"),
                CardUtils.getCardValue("4", "d"),
                CardUtils.getCardValue("3", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winFlushJokBo);
        Collections.sort(loseFlushJokBo);

        winPlayerCardList.addAll(winFlushJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseFlushJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }



    @Test
    @DisplayName("같은 스트레이트 일 때 더 높은 스트레이트를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareStraightWithSameStraight() {

        //given
        ArrayList<Integer> winStraightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> loseStraightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7", "d"),
                CardUtils.getCardValue("6", "c"),
                CardUtils.getCardValue("5", "h"),
                CardUtils.getCardValue("4", "s"),
                CardUtils.getCardValue("3", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winStraightJokBo);
        Collections.sort(loseStraightJokBo);

        winPlayerCardList.addAll(winStraightJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseStraightJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 트리플 일 때 더 높은 트리플의 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareTripleRankWithSameTriple() {

        //given
        ArrayList<Integer> winTripleJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("8", "d")
        ));

        ArrayList<Integer> loseTripleJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("7", "d"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("7", "h"),
                CardUtils.getCardValue("6", "s"),
                CardUtils.getCardValue("8", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winTripleJokBo);
        Collections.sort(loseTripleJokBo);

        winPlayerCardList.addAll(winTripleJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseTripleJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 트리플 일 때 더 높은 하이카드를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareHighCardWithSameTriple() {

        //given
        ArrayList<Integer> winTripleJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("9", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> loseTripleJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winTripleJokBo);
        Collections.sort(loseTripleJokBo);

        winPlayerCardList.addAll(winTripleJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseTripleJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 투페어 일 때 더 높은 첫번째 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareFirstRankWithSameTwoPair() {

        //given
        ArrayList<Integer> winTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> loseTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("9", "d"),
                CardUtils.getCardValue("9", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winTwoPairJokBo);
        Collections.sort(loseTwoPairJokBo);

        winPlayerCardList.addAll(winTwoPairJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseTwoPairJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 투페어 일 때 더 높은 두번째 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareSecondRankWithSameTwoPair() {

        //given
        ArrayList<Integer> winTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("9", "h"),
                CardUtils.getCardValue("9", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> loseTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winTwoPairJokBo);
        Collections.sort(loseTwoPairJokBo);

        winPlayerCardList.addAll(winTwoPairJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseTwoPairJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 투페어 일 때 더 높은 하이카드를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareHighCardWithSameTwoPair() {

        //given
        ArrayList<Integer> winTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("A", "d")
        ));

        ArrayList<Integer> loseTwoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winTwoPairJokBo);
        Collections.sort(loseTwoPairJokBo);

        winPlayerCardList.addAll(winTwoPairJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseTwoPairJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 원페어 일 때 더 높은 원페어의 랭크를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareOnePairRankWithSameOnePair() {

        //given
        ArrayList<Integer> winOnePairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> loseOnePairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("8", "d"),
                CardUtils.getCardValue("8", "c"),
                CardUtils.getCardValue("7", "h"),
                CardUtils.getCardValue("5", "s"),
                CardUtils.getCardValue("4", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winOnePairJokBo);
        Collections.sort(loseOnePairJokBo);

        winPlayerCardList.addAll(winOnePairJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseOnePairJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);


        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 원페어 일 때 더 높은 하이카드를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareHighCardWithSameOnePair() {

        //given
        ArrayList<Integer> winOnePairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("A", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> loseOnePairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("J", "h"),
                CardUtils.getCardValue("7", "s"),
                CardUtils.getCardValue("6", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winOnePairJokBo);
        Collections.sort(loseOnePairJokBo);

        winPlayerCardList.addAll(winOnePairJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseOnePairJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    @Test
    @DisplayName("같은 하이카드 일 때 더 높은 하이카드를 가진 플레이어의 핸드가치가 높게 평가되는지 테스트")
    void compareHighCardValueWithSameHighCard() {

        //given
        ArrayList<Integer> winHighCardJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A", "d"),
                CardUtils.getCardValue("J", "c"),
                CardUtils.getCardValue("8", "h"),
                CardUtils.getCardValue("6", "s"),
                CardUtils.getCardValue("4", "d")
        ));

        ArrayList<Integer> loseHighCardJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("J", "d"),
                CardUtils.getCardValue("Q", "c"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("5", "d")
        ));

        ArrayList<Integer> winPlayerCardList = new ArrayList<>(CARD_SIZE);
        ArrayList<Integer> losePlayerCardList = new ArrayList<>(CARD_SIZE);

        Collections.sort(winHighCardJokBo);
        Collections.sort(loseHighCardJokBo);

        winPlayerCardList.addAll(winHighCardJokBo);
        winPlayerCardList.add(CardUtils.getCardValue("2", "s"));
        winPlayerCardList.add(CardUtils.getCardValue("3", "s"));

        losePlayerCardList.addAll(loseHighCardJokBo);
        losePlayerCardList.add(CardUtils.getCardValue("2", "s"));
        losePlayerCardList.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto winPlayerGameResult = HandCalculatorUtils.calculateValue(winPlayerCardList);
        List<Integer> winPlayerJokBo = winPlayerGameResult.getJokBo();
        Collections.sort(winPlayerJokBo);

        GameResultDto losePlayerGameResult = HandCalculatorUtils.calculateValue(losePlayerCardList);
        List<Integer> losePlayerJokBo = losePlayerGameResult.getJokBo();
        Collections.sort(losePlayerJokBo);

        //then
        assertThat(winPlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT)
                .isEqualTo(losePlayerGameResult.getHandValue() / JOKBO_DIVIDE_CONSTANT);
        assertThat(winPlayerGameResult.getHandValue()).isGreaterThan(losePlayerGameResult.getHandValue());

        log.info("\n Win Player Details\n");
        printTestDetails(winPlayerCardList, winPlayerJokBo, winPlayerGameResult);

        log.info("\n Lose Player Details\n");
        printTestDetails(losePlayerCardList, losePlayerJokBo, losePlayerGameResult);
    }

    private static void printTestDetails(List<Integer> cards, List<Integer> jokBo, GameResultDto gameResultDto){
        ArrayList<String> cardContexts = new ArrayList<>();
        for (Integer card : cards) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nCard List: {}", cardContexts + "\n");

        cardContexts.clear();

        for (Integer card : jokBo) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nJokBo List: {}", cardContexts + "\n");

        log.info("\nHand Strong Value: {} ", gameResultDto.getHandValue());
    }

}