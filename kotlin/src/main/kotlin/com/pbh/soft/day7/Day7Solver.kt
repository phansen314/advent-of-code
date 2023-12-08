package com.pbh.soft.day7

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.newlineP
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.common.parsing.ParsingUtils.parseMap
import com.pbh.soft.day7.Card._J
import com.pbh.soft.day7.HandType.*
import com.pbh.soft.day7.Parsing.problemP
import mu.KLogging

object Day7Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = problemP.parse(text).onSuccess { problem ->
    problem
      .map { ClassifiedHandXBid(it.hand, it.bid, StandardClassifier(it.hand)) }
      .sortedWith(StandardComparator)
      .mapIndexed { i, chb -> (i + 1) * chb.bid }
      .sum()
      .toString()
  }

  override fun solveP2(text: String): String = problemP.parse(text).onSuccess { problem ->
    problem
      .map { ClassifiedHandXBid(it.hand, it.bid, WithJokersClassifier(it.hand)) }
      .sortedWith(WithJokerRuleComparator)
      .mapIndexed { i, chb -> (i + 1) * chb.bid }
      .sum()
      .toString()
  }
}


typealias Hand = List<Card>

data class HandXBid(val hand: Hand, val bid: Long)
data class ClassifiedHandXBid(
  val hand: Hand,
  val bid: Long,
  val type: HandType
)

enum class Card(val chr: Char) {
  _2('2'),
  _3('3'),
  _4('4'),
  _5('5'),
  _6('6'),
  _7('7'),
  _8('8'),
  _9('9'),
  _T('T'),
  _J('J'),
  _Q('Q'),
  _K('K'),
  _A('A');

  companion object {
    val charToCard = entries.associateBy { it.chr }
  }
}

enum class HandType {
  HIGH_CARD,
  ONE_PAIR,
  TWO_PAIR,
  THREE_OF_A_KIND,
  FULL_HOUSE,
  FOUR_OF_A_KIND,
  FIVE_OF_A_KIND
}

interface Classifier : (Hand) -> HandType

object StandardClassifier : Classifier {
  override fun invoke(hand: Hand): HandType {
    val cmap = hand.groupingBy { it }.eachCount()
    return if (cmap.size == 1) FIVE_OF_A_KIND
    else if (cmap.size == 2) {
      val x = cmap.values.first()
      if (x == 1 || x == 4) FOUR_OF_A_KIND
      else FULL_HOUSE
    } else if (cmap.size == 3) {
      if (cmap.values.any { it == 3 }) THREE_OF_A_KIND
      else TWO_PAIR
    } else if (cmap.size == 5) HIGH_CARD
    else ONE_PAIR
  }
}

object WithJokersClassifier : Classifier {
  override fun invoke(hand: Hand): HandType {
    val cmap = hand.groupingBy { it }.eachCount()
    return when (cmap[_J]) {
      null -> StandardClassifier(hand)
      1 -> when (cmap.size) {
        2 -> FIVE_OF_A_KIND
        3 -> if (cmap.values.any { it == 2 }) FULL_HOUSE else FOUR_OF_A_KIND
        4 -> THREE_OF_A_KIND
        else -> ONE_PAIR
      }

      2 -> when (cmap.size) {
        2 -> FIVE_OF_A_KIND
        3 -> FOUR_OF_A_KIND
        else -> THREE_OF_A_KIND
      }

      3 -> if (cmap.size == 2) FIVE_OF_A_KIND else FOUR_OF_A_KIND
      4 -> FIVE_OF_A_KIND
      else -> FIVE_OF_A_KIND
    }
  }
}

object WithJokerRuleComparator : Comparator<ClassifiedHandXBid> {
  val cardComparator: Comparator<Card> = Comparator { c1, c2 ->
    if (c1 == c2) 0
    else if (c1 == _J) -1
    else if (c2 == _J) 1
    else if (c1 < c2) -1
    else 1
  }

  override fun compare(o1: ClassifiedHandXBid, o2: ClassifiedHandXBid): Int {
    return if (o1.type < o2.type) -1
    else if (o1.type > o2.type) 1
    else {
      for (i in o1.hand.indices) {
        val comp = cardComparator.compare(o1.hand[i], o2.hand[i])
        if (comp < 0) return -1
        if (comp > 0) return 1
      }
      0
    }
  }
}

object StandardComparator : Comparator<ClassifiedHandXBid> {
  override fun compare(o1: ClassifiedHandXBid, o2: ClassifiedHandXBid): Int {
    return if (o1.type < o2.type) -1
    else if (o1.type > o2.type) 1
    else {
      for (i in o1.hand.indices) {
        if (o1.hand[i] < o2.hand[i]) return -1
        if (o1.hand[i] > o2.hand[i]) return 1
      }
      0
    }
  }
}

object Parsing {
  val handXbidP = parser {
    val hand = many1(parseMap(Card.charToCard)); whitespace1();
    val bid = integer()
    HandXBid(hand, bid)
  }
  val problemP = chain(handXbidP, newlineP).map { it.terms }
}