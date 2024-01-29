package com.example.pokerv2.utils;


import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.enums.HandValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class HandCalculatorUtils {

    private static final long ROYAL_FLUSH_VALUE_PREFIX = 90000000000L;
    private static final long STRAIGHT_FLUSH_VALUE_PREFIX = 80000000000L;
    private static final long FOUR_OF_A_KIND_VALUE_PREFIX = 70000000000L;
    private static final long FULL_HOUSE_VALUE_PREFIX = 60000000000L;
    private static final long FLUSH_VALUE_PREFIX = 50000000000L;
    private static final long STRAIGHT_VALUE_PREFIX = 40000000000L;
    private static final long THREE_OF_A_KIND_VALUE_PREFIX = 30000000000L;
    private static final long TWO_PAIR_VALUE_PREFIX = 20000000000L;
    private static final long ONE_PAIR_VALUE_PREFIX = 10000000000L;


    /**
     * 24/01/15 chan
     * <p>
     * 핸드의 세기를 계산해주는 유틸클래스.
     * 계산한 핸드의 세기와, 족보를 이루는 카드리스트들을 GameResultDto에 담아준다.
     *
     * <p>
     *
     * <p>
     * 함수가 너무 긺. 나중에 리팩토링 하기.
     */

    private HandCalculatorUtils() {
    }

    public static GameResultDto calculateValue(List<Integer> cards) {
        List<Integer> jokBoList = new ArrayList<>();
        cards.sort(CardUtils.rankComparator());
        long handValue;

        handValue = evaluateRoyalStraightFlush(cards, jokBoList);
        if (handValue == -1L) {
            jokBoList.clear();
            handValue = evaluateStraightFlush(cards, jokBoList);
            if (handValue == -1L) {
                jokBoList.clear();
                handValue = evaluateFourOfAKind(cards, jokBoList);
                if (handValue == -1L) {
                    jokBoList.clear();
                    handValue = evaluateFullHouse(cards, jokBoList);
                    if (handValue == -1L) {
                        jokBoList.clear();
                        handValue = evaluateFlush(cards, jokBoList);
                        if (handValue == -1L) {
                            jokBoList.clear();
                            handValue = evaluateStraight(cards, jokBoList);
                            if (handValue == -1L) {
                                jokBoList.clear();
                                handValue = evaluateThreeOfAKind(cards, jokBoList);
                                if (handValue == -1L) {
                                    jokBoList.clear();
                                    handValue = evaluateTwoPair(cards, jokBoList);
                                    if (handValue == -1L) {
                                        jokBoList.clear();
                                        handValue = evaluateOnePair(cards, jokBoList);
                                        if (handValue == -1L) {
                                            jokBoList.clear();
                                            handValue = evaluateHighCard(cards, jokBoList);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return GameResultDto.builder().handValue(handValue).jokBo(jokBoList).handContext(getHandContextByValue(handValue)).build();
    }



    public static String getHandContextByValue(long value) {
        String handContext;
        if (value == ROYAL_FLUSH_VALUE_PREFIX) {
            handContext = HandValue.ROYAL_STRAIGHT_FLUSH.getDetail();
        } else if (value >= STRAIGHT_FLUSH_VALUE_PREFIX) {
            handContext = HandValue.STRAIGHT_FLUSH.getDetail();
        } else if (value >= FOUR_OF_A_KIND_VALUE_PREFIX) {
            handContext = HandValue.FOUR_OF_A_KIND.getDetail();
        } else if (value >= FULL_HOUSE_VALUE_PREFIX) {
            handContext = HandValue.FULL_HOUSE.getDetail();
        } else if (value >= FLUSH_VALUE_PREFIX) {
            handContext = HandValue.FLUSH.getDetail();
        } else if (value >= STRAIGHT_VALUE_PREFIX) {
            handContext = HandValue.STRAIGHT.getDetail();
        } else if (value >= THREE_OF_A_KIND_VALUE_PREFIX) {
            handContext = HandValue.THREE_OF_A_KIND.getDetail();
        } else if (value >= TWO_PAIR_VALUE_PREFIX) {
            handContext = HandValue.TWO_PAIR.getDetail();
        } else if (value >= ONE_PAIR_VALUE_PREFIX) {
            handContext = HandValue.ONE_PAIR.getDetail();
        } else {
            handContext = HandValue.HIGH_CARD.getDetail();
        }

        return handContext;
    }

    /**
     * highCardValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 하이카드의 밸류 (5장의 하이카드)
     */
    private static long evaluateHighCard(List<Integer> cards, List<Integer> jokBo) {
        cards.sort(CardUtils.rankComparator());

        jokBo.clear();
        int cnt = 0;
        for (Integer card : cards) {
            jokBo.add(card);
            cnt++;
            if (cnt == 5) {
                break;
            }
        }

        return (jokBo.get(0) % 13 + 1) * 100000000L + (jokBo.get(1) % 13 + 1) * 1000000L + (jokBo.get(2) % 13 + 1) * 10000L + (jokBo.get(3) % 13 + 1) * 100L + (jokBo.get(4) % 13 + 1);
    }

    /**
     * onePairValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 원페어의 밸류 (원페어의 하이카드, 나머지 3장의 하이카드)
     */
    private static long evaluateOnePair(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int pairRank = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                jokBo.clear();
                pairRank = rank;
                for (Integer pairCard : cards) {
                    if (pairCard % 13 == pairRank) {
                        jokBo.add(pairCard);

                    }
                }
            }
        }

        cards.sort(CardUtils.rankComparator());

        int cnt = 0;
        if (pairRank != -1) {
            for (Integer card : cards) {
                if (card % 13 != pairRank) {
                    if (cnt++ < 3) {
                        jokBo.add(card);
                    } else break;
                }
            }

            return ONE_PAIR_VALUE_PREFIX + (pairRank + 1) * 1000000L + (jokBo.get(2) % 13 + 1) * 10000L + (jokBo.get(3) % 13 + 1) * 100L + (jokBo.get(4) % 13 + 1);
        }

        return -1;
    }

    /**
     * twoPairValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 투 페어의 밸류 (두 페어의 rank + 나머지 1개 하이카드)
     */
    private static long evaluateTwoPair(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int firstPairRank = -1;
        int secondPairRank = -1;
        int highCard = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                if (firstPairRank == -1) {
                    firstPairRank = rank;
                } else {
                    secondPairRank = rank;
                }
            }
        }

        if (firstPairRank != -1 && secondPairRank != -1) {
            jokBo.clear();
            for (Integer card : cards) {
                int rank = card % 13;
                if (rank == firstPairRank || rank == secondPairRank) {
                    jokBo.add(card);
                } else {
                    if (highCard % 13 < rank) {
                        highCard = card;
                    }
                }
            }
            jokBo.add(highCard);
            return TWO_PAIR_VALUE_PREFIX + (Math.max(firstPairRank, secondPairRank) + 1) * 10000L + (Math.min(firstPairRank, secondPairRank) + 1) * 100L + (highCard % 13 + 1);
        }

        return -1;
    }

    /**
     * threeOfAKindValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 트리플의 밸류 (트리플의 rank + 족보의 나머지 2개 하이카드)
     */
    private static long evaluateThreeOfAKind(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int threeOfAKindValue = -1;
        int firstHighCard = -1;
        int secondHighCard = -1;
        boolean isThreeOfAKind = false;
        int tripleRank = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 3) {
                jokBo.clear();
                tripleRank = rank;
                for (Integer tripleCard : cards) {
                    if (tripleCard % 13 == tripleRank) {
                        jokBo.add(tripleCard);
                    }
                }

                isThreeOfAKind = true;
                threeOfAKindValue = tripleRank;
            }
        }


        if (isThreeOfAKind) {
            for (Integer card : cards) {
                int rank = card % 13;
                if (rank != tripleRank) {
                    if (secondHighCard % 13 < rank) {
                        if (firstHighCard % 13 < rank) {
                            secondHighCard = firstHighCard;
                            firstHighCard = card;
                        } else {
                            secondHighCard = card;
                        }
                    }
                }
            }

            jokBo.add(firstHighCard);
            jokBo.add(secondHighCard);
            return THREE_OF_A_KIND_VALUE_PREFIX + (threeOfAKindValue + 1) * 10000L + (firstHighCard % 13 + 1) * 100L + (secondHighCard % 13 + 1);
        }

        return -1;
    }


    /**
     * straightValue
     *
     * @param cards rank 내림차 순으로 정렬된 7장의 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 스트레이트의 밸류 (스트레이트를 이루는 족보중 하이카드)
     */
    private static long evaluateStraight(List<Integer> cards, List<Integer> jokBo) {
        cards.sort(CardUtils.rankComparator());

        int lastValue = -1;
        int consecutiveCount = 0;
        int topidx = 0;

        for (int i = 0; i < cards.size(); i++) {
            int currentCard = cards.get(i);
            int currentValue = currentCard % 13;

            if (lastValue == -1 || lastValue % 13 == currentValue) {
                lastValue = currentValue;
                continue;
            }

            if (lastValue - 1 == currentValue) {
                consecutiveCount++;
                jokBo.add(currentCard);

                if (consecutiveCount == 4) {
                    if(jokBo.size() != 5) {
                        jokBo.add(cards.get(topidx));
                    }
                    return STRAIGHT_VALUE_PREFIX + lastValue + 1;
                }
            } else {
                consecutiveCount = 0;
                topidx = i + 1;
                jokBo.clear();
                jokBo.add(currentCard);
            }

            lastValue = currentValue;
        }

        // Check BackStraight
        if (consecutiveCount == 3 && cards.get(cards.size() - 1) % 13 == 0 && cards.get(0) % 13 == 12) {
            jokBo.add(cards.get(0));
            return STRAIGHT_VALUE_PREFIX + 4;
        }

        return -1;
    }


    /**
     * flushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 플러시의 밸류. (플러시를 이루는 카드의 하이카드 순서)
     */

    private static long evaluateFlush(List<Integer> cards, List<Integer> jokBo) {
        cards.sort(Comparator.reverseOrder());

        int consecutiveCount = 1;
        int lastSuit = -1;
        int highestRank = -1;

        for (Integer card : cards) {
            int currentSuit = card / 13;

            if (lastSuit != -1 && lastSuit == currentSuit) {
                consecutiveCount++;
                jokBo.add(card);

                if (consecutiveCount == 5) {
                    highestRank = card % 13 + 4;
                    break;
                }
            } else {
                consecutiveCount = 1;
                jokBo.clear();
                jokBo.add(card);
            }

            lastSuit = currentSuit;
        }

        if (highestRank != -1) {
            jokBo.sort(CardUtils.rankComparator());

            long flushValue = 0L;
            long valuePrefix = 100000000L;
            for (int i = 0; i < jokBo.size(); i++) {
                flushValue = flushValue + ((jokBo.get(i) % 13) + 1) * valuePrefix;
                valuePrefix /= 100L;
            }
            return FLUSH_VALUE_PREFIX + flushValue;
        }

        return -1;
    }

    /**
     * fullHouseValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 풀하우스의 밸류. (트리플의 밸류(우선순위) && 페어의 밸류)
     */
    private static long evaluateFullHouse(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        boolean hasThreeOfAKind = false;
        boolean hasTwoOfAKind = false;
        int threeOfKindValue = -1;
        int twoOfKindValue = -1;

        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int count = entry.getValue();
            int rank = entry.getKey();

            if (count == 3) {
                threeOfKindValue = rank;
                hasThreeOfAKind = true;
            } else if (count == 2) {
                twoOfKindValue = rank;
                hasTwoOfAKind = true;
            }
        }

        if (hasThreeOfAKind && hasTwoOfAKind) {
            jokBo.clear();
            for (Integer card : cards) {
                int rank = card % 13;
                if (rank == threeOfKindValue) {
                    jokBo.add(card);
                }
            }

            for (Integer card : cards) {
                int rank = card % 13;
                if (rank == twoOfKindValue) {
                    jokBo.add(card);
                    if (jokBo.size() == 5) break;
                }
            }
            return FULL_HOUSE_VALUE_PREFIX + (threeOfKindValue + 1) * 100 + twoOfKindValue + 1;
        }

        return -1L;
    }

    /**
     * fourOfAKindValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 포카드의 밸류. (포카드의 밸류 && 포카드를 제외한 나머지 카드중 하이카드)
     */
    private static long evaluateFourOfAKind(List<Integer> cards, List<Integer> jokBo) {
        int[] ranks = new int[13];
        for (int card : cards) {
            ranks[card % 13]++;
        }

        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 4) {
                int fourAKindValue = -1;
                int highCard = -1;
                for (int j = 0; j < cards.size(); j++) {
                    int card = cards.get(j);
                    if (card % 13 == i) {
                        fourAKindValue = i;
                        jokBo.add(card);
                    }
                }

                for (Integer card : cards) {
                    if (card % 13 != fourAKindValue && highCard % 13 < card % 13) {
                        highCard = card;
                    }
                }

                jokBo.add(highCard);
                return FOUR_OF_A_KIND_VALUE_PREFIX + (fourAKindValue + 1) * 100 + highCard % 13 + 1;
            }
        }

        return -1;
    }

    /**
     * straightFlushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 스트레이트 플러시의 밸류. (스티플의 하이카드)
     */
    private static long evaluateStraightFlush(List<Integer> cards, List<Integer> jokBo) {
        cards.sort(Comparator.reverseOrder());

        int value;
        for (int i = 0; i <= cards.size() - 5; i++) {
            boolean isStraightFlush = true;
            value = cards.get(i) % 13;
            int consecutiveCount = 0;
            for (int j = 0; j < 4; j++) {
                int currentCard = cards.get(i + j);
                int nextCard = cards.get(i + j + 1);

                if ((currentCard % 13) - 1 != nextCard % 13 || currentCard / 13 != nextCard / 13) {
                    jokBo.clear();
                    isStraightFlush = false;
                    value = -1;
                    consecutiveCount = 0;
                    break;
                }
                consecutiveCount++;
                jokBo.add(currentCard);
                if (j == 3) {
                    jokBo.add(nextCard);
                }

                if(consecutiveCount == 3 && nextCard % 13 == 0){
                    if(cards.contains((nextCard / 13) * 13 + 12)){
                        jokBo.add(nextCard);
                        jokBo.add((nextCard / 13) * 13 + 12);
                        return STRAIGHT_FLUSH_VALUE_PREFIX + 4;
                    }
                }
            }



            if (isStraightFlush) {
                return STRAIGHT_FLUSH_VALUE_PREFIX + value;
            }
        }
        return -1L;
    }

    /**
     * royalStraightFlushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 로티플 true -> 0. false -> -1
     */
    private static long evaluateRoyalStraightFlush(List<Integer> cards, List<Integer> jokBo) {
        for (int i = 0; i <= cards.size() - 5; i++) {
            boolean isRoyalFlush = true;

            for (int j = 0; j < 4; j++) {
                int currentCard = cards.get(i + j);
                int nextCard = cards.get(i + j + 1);

                if (currentCard % 13 != 13 - 1 - j || (nextCard % 13 + 1) % 13 != currentCard % 13 || currentCard / 13 != nextCard / 13) {
                    isRoyalFlush = false;
                    break;
                }

                jokBo.add(currentCard);

                if (j == 3) {
                    jokBo.add(nextCard);
                }
            }

            if (isRoyalFlush) {
                return ROYAL_FLUSH_VALUE_PREFIX;
            }

            jokBo.clear();
        }

        return -1L;
    }
}