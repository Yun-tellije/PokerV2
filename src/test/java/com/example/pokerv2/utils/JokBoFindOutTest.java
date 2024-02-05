package com.example.pokerv2.utils;

import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.enums.HandValue;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class JokBoFindOutTest {

    private static final int CARD_SIZE = 7;

    /**
     * 백스트레이트 검증 로직도 짜야함.
     */

    @Test
    @DisplayName("로얄 스트레이트 플러시 검증 테스트")
    void evaluateRoyalStraightFlushTest() {

        //given
        ArrayList<Integer> royalStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A","d"),
                CardUtils.getCardValue("K","d"),
                CardUtils.getCardValue("Q","d"),
                CardUtils.getCardValue("J","d"),
                CardUtils.getCardValue("10","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(royalStraightFlushJokBo);
        cards.addAll(royalStraightFlushJokBo);

        cards.add(CardUtils.getCardValue("2","s"));
        cards.add(CardUtils.getCardValue("3","s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.ROYAL_STRAIGHT_FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(royalStraightFlushJokBo);

        printTestDetails(cards, jokBo);

    }

    @Test
    @DisplayName("스트레이트 플러시 검증 테스트 1. 일반")
    void evaluateStraightFlushTest1() {

        //given
        ArrayList<Integer> straightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("9","d"),
                CardUtils.getCardValue("8","d"),
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightFlushJokBo);
        cards.addAll(straightFlushJokBo);

        cards.add(CardUtils.getCardValue("2","s"));
        cards.add(CardUtils.getCardValue("3","s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT_FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(straightFlushJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("스트레이트 플러시 검증 테스트 2. 스트레이트를 이루는 카드 중 원페어가 포함되어 있을 때")
    void evaluateStraightFlushTest2() {

        //given
        ArrayList<Integer> straightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("9","d"),
                CardUtils.getCardValue("8","d"),
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightFlushJokBo);
        cards.addAll(straightFlushJokBo);

        cards.add(CardUtils.getCardValue("9","s"));
        cards.add(CardUtils.getCardValue("3","s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT_FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(straightFlushJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("스트레이트 플러시 검증 테스트 3. 스트레이트를 이루는 카드 중 투페어가 포함되어 있을 때")
    void evaluateStraightFlushTest3() {

        //given
        ArrayList<Integer> straightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("9","d"),
                CardUtils.getCardValue("8","d"),
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightFlushJokBo);
        cards.addAll(straightFlushJokBo);

        cards.add(CardUtils.getCardValue("8","s"));
        cards.add(CardUtils.getCardValue("7","s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT_FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(straightFlushJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("백 스트레이트 플러시 검증 테스트")
    void evaluateBackStraightFlushTest() {

        //given
        ArrayList<Integer> straightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A","d"),
                CardUtils.getCardValue("3","d"),
                CardUtils.getCardValue("2","d"),
                CardUtils.getCardValue("4","d"),
                CardUtils.getCardValue("5","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightFlushJokBo);
        cards.addAll(straightFlushJokBo);

        cards.add(CardUtils.getCardValue("7","s"));
        cards.add(CardUtils.getCardValue("8","s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT_FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(straightFlushJokBo);

        printTestDetails(cards, jokBo);
    }


    @Test
    @DisplayName("포카드 검증 테스트")
    void evaluateFourOfAKindTest() {

        //given
        ArrayList<Integer> fourOfAKindJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("10", "s"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("A", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(fourOfAKindJokBo);
        cards.addAll(fourOfAKindJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FOUR_OF_A_KIND.getDetail());
        assertThat(jokBo).isEqualTo(fourOfAKindJokBo);

        printTestDetails(cards, jokBo);

    }

    @Test
    @DisplayName("풀하우스 검증 테스트")
    void evaluateFullHouseTest() {

        //given
        ArrayList<Integer> fullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("10", "s"),
                CardUtils.getCardValue("3", "c"),
                CardUtils.getCardValue("3", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(fullHouseJokBo);
        cards.addAll(fullHouseJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("5", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FULL_HOUSE.getDetail());
        assertThat(jokBo).isEqualTo(fullHouseJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("플러시 검증 테스트")
    void evaluateFlushTest() {

        //given
        ArrayList<Integer> flushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A", "h"),
                CardUtils.getCardValue("K", "h"),
                CardUtils.getCardValue("Q", "h"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("7", "h")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(flushJokBo);
        cards.addAll(flushJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FLUSH.getDetail());
        assertThat(jokBo).isEqualTo(flushJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("스트레이트 검증 테스트. 1. 일반 스트레이트 일 때")
    void evaluateStraightTest() {

        //given
        ArrayList<Integer> straightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightJokBo);
        cards.addAll(straightJokBo);

        cards.add(CardUtils.getCardValue("A", "s"));
        cards.add(CardUtils.getCardValue("K", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT.getDetail());
        assertThat(jokBo).isEqualTo(straightJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("스트레이트 검증 테스트. 2. 원페어가 포함된 스트레이트 일 때")
    void evaluateStraightTest2() {

        //given
        ArrayList<Integer> straightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightJokBo);
        cards.addAll(straightJokBo);

        cards.add(CardUtils.getCardValue("9", "s"));
        cards.add(CardUtils.getCardValue("K", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT.getDetail());
        assertThat(jokBo).isEqualTo(straightJokBo);

        printTestDetails(cards, jokBo);
    }



    @Test
    @DisplayName("스트레이트 검증 테스트. 3. 투페어가 포함된 스트레이트 일 때")
    void evaluateStraightTest3() {

        //given
        ArrayList<Integer> straightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightJokBo);
        cards.addAll(straightJokBo);

        cards.add(CardUtils.getCardValue("9", "s"));
        cards.add(CardUtils.getCardValue("10", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT.getDetail());
        assertThat(jokBo).isEqualTo(straightJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("백 스트레이트 검증 테스트")
    void evaluateBackStraightTest() {

        //given
        ArrayList<Integer> straightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A", "d"),
                CardUtils.getCardValue("2", "h"),
                CardUtils.getCardValue("4", "s"),
                CardUtils.getCardValue("3", "c"),
                CardUtils.getCardValue("5", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightJokBo);
        cards.addAll(straightJokBo);

        cards.add(CardUtils.getCardValue("8", "s"));
        cards.add(CardUtils.getCardValue("9", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT.getDetail());
        assertThat(jokBo).isEqualTo(straightJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("트리플 검증 테스트")
    void evaluateTripleTest() {

        //given
        ArrayList<Integer> threeOfAKindJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("10", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("8", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(threeOfAKindJokBo);
        cards.addAll(threeOfAKindJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("6", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.THREE_OF_A_KIND.getDetail());
        assertThat(jokBo).isEqualTo(threeOfAKindJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("투페어 검증 테스트")
    void evaluateTwoPairTest() {

        //given
        ArrayList<Integer> twoPairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("8", "c"),
                CardUtils.getCardValue("7", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(twoPairJokBo);
        cards.addAll(twoPairJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("6", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.TWO_PAIR.getDetail());
        assertThat(jokBo).isEqualTo(twoPairJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("원페어 검증 테스트")
    void evaluateOnePairTest() {

        //given
        ArrayList<Integer> onePairJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(onePairJokBo);
        cards.addAll(onePairJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.ONE_PAIR.getDetail());
        assertThat(jokBo).isEqualTo(onePairJokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("하이카드 검증 테스트")
    void evaluateHighCardTest() {

        //given
        ArrayList<Integer> highCardJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A", "d"),
                CardUtils.getCardValue("K", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(highCardJokBo);
        cards.addAll(highCardJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.HIGH_CARD.getDetail());
        assertThat(jokBo).isEqualTo(highCardJokBo);

        printTestDetails(cards, jokBo);
    }

    private static void printTestDetails(List<Integer> cards, List<Integer> jokBo){
        ArrayList<String> cardContexts = new ArrayList<>();
        for (Integer card : cards) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nCard List: {}", cardContexts);

        cardContexts.clear();

        for (Integer card : jokBo) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nJokBo List: {}", cardContexts);
    }

}