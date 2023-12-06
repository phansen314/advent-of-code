package com.pbh.soft.day5

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day5.RangeKind.LOCATION
import com.pbh.soft.day5.RangeKind.SEED
import mu.KLogging

object Day5Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.almanacP.parse(text).onSuccess { almanac ->
    val answer = almanac.seeds
      .mapIndexed { index, seed ->
        var current = SEED to seed
        do {
          val dest = almanac.follow(current)
          current = dest
        } while (dest.first != LOCATION)
        current.second
      }
      .min()

    return answer.toString()
  }

  override fun solveP2(text: String): String = Parsing.almanacP.parse(text).onSuccess { almanac ->
    val answer = almanac.seeds.asSequence().chunked(2).flatMap { (start, length) -> start ..< start + length }
      .map { seed ->
        var current = seed
        for ((_, mapping) in almanac.mappings) {
          val dest = mapping.follow(current)
          current = dest
        }
//        if (current % 100000L == 0L) logger.debug { current.toString() }
        current
      }
      .minOrNull()

    return answer.toString()
//    val seedRanges = almanac.seeds.chunked(2).map { (start, length) -> start..<start + length }
//    println("number of seeds: ${almanac.seeds.chunked(2).map { it[1] }.sum()}")
//
//    val reversedMappings = almanac.mappings.map { (_, existing) ->
//      val swappedRanges = existing.ranges.map { it.copy(srcRange = it.destRange, destRange = it.srcRange) }.sortedBy { it.srcRange.first }
//      existing.to to existing.copy(from = existing.to, to = existing.from, ranges = swappedRanges)
//    }.reversed().toMap(linkedMapOf())
//
//    for (location in 0..<Long.MAX_VALUE) {
//      var current = LOCATION to location
//      do {
//        val dest = reversedMappings.follow(current)
//        current = dest
//      } while (dest.first != SEED)
//      if (seedRanges.search(current.second) != null) {
//        return current.second.toString()
//      } else if (location % 100000 == 0L) {
//        logger.debug { "$location" }
//      }
//    }
//
//    return "todo"
  }

  // list must be sorted!
  private fun List<Ranges>.search(value: Long): Ranges? {
    var (l, r) = 0 to size - 1
    while (l <= r) {
      val m = (l + r).ushr(1)
      val rangesSpec = this[m]
      if (value in rangesSpec.srcRange) return rangesSpec
      if (value > rangesSpec.srcRange.last) l = m + 1
      else r = m - 1
    }
    return null
  }

  private fun List<LongRange>.search(value: Long): LongRange? {
    var (l, r) = 0 to size - 1
    while (l <= r) {
      val m = (l + r).ushr(1)
      val rangesSpec = this[m]
      if (value in rangesSpec) return rangesSpec
      if (value > rangesSpec.last) l = m + 1
      else r = m - 1
    }
    return null
  }

  private fun Almanac.follow(location: Pair<RangeKind, Long>): Pair<RangeKind, Long> {
    return mappings.follow(location)
//    val (inKind, inValue) = location
//    val mapping = this.mappings[inKind] ?: throw IllegalStateException("no mappings defined for $inKind!!")
//    val outValue = mapping.ranges.search(inValue)?.let { inValue - it.srcRange.first + it.destRange.first } ?: inValue
//    return mapping.to to outValue
  }

  private fun Map<RangeKind, Mapping>.follow(location: Pair<RangeKind, Long>): Pair<RangeKind, Long> {
    val (inKind, inValue) = location
    val mapping = this[inKind] ?: throw IllegalStateException("no mappings defined for $inKind!!")
    val outValue = mapping.ranges.search(inValue)?.let { inValue - it.srcRange.first + it.destRange.first } ?: inValue
    return mapping.to to outValue
  }

  private fun Mapping.follow(inValue: Long): Long {
    return ranges.search(inValue)?.let { inValue - it.srcRange.first + it.destRange.first } ?: inValue
  }
}

object Parsing {
  val newlineP = string("\r\n")
  val rangeSpecP = parser {
    val destinationStart = integer(); whitespace1()
    val sourceStart = integer(); whitespace1()
    val length = integer()
    val destinationEnd: Long = destinationStart + length
    val sourceEnd: Long = sourceStart + length
    Ranges(sourceStart..<sourceEnd, destinationStart..<destinationEnd, length)
  }
  val rangeKindP = oneOf(*RangeKind.entries.map { e -> string(e.textName).map { e } }.toTypedArray<Parser<RangeKind>>())
  val mappingP = parser {
    val from = rangeKindP(); string("-to-");
    val to = rangeKindP(); string(" map:\r\n")
    val rangesSpecs = chain(rangeSpecP, newlineP).terms.sortedBy { it.srcRange.first }
    Mapping(from, to, rangesSpecs)
  }

  val almanacP = parser {
    string("seeds:"); whitespace1()
    val seeds = chain(integer, whitespace1).terms; many(newlineP)
    val mappings = chain(mappingP, cc.ekblad.konbini.many(newlineP)).terms.associateByTo(linkedMapOf()) { it.from }
    Almanac(seeds, mappings)
  }
}

typealias Seed = Long

enum class RangeKind(val textName: String) {
  SEED("seed"),
  SOIL("soil"),
  FERTILIZER("fertilizer"),
  WATER("water"),
  LIGHT("light"),
  TEMPERATURE("temperature"),
  HUMIDITY("humidity"),
  LOCATION("location"),
}

data class Ranges(val srcRange: LongRange, val destRange: LongRange, val length: Long) : ClosedRange<Long> by srcRange
data class Mapping(val from: RangeKind, val to: RangeKind, val ranges: List<Ranges>)
data class Almanac(
  val seeds: List<Seed>,
  val mappings: Map<RangeKind, Mapping>,
//  val seedToSoil: Mapping,
//  val soilToFertilizer: Mapping,
//  val fertilizerToWater: Mapping,
//  val waterToLight: Mapping,
//  val lightToTemperature: Mapping,
//  val temperatureToHumidity: Mapping,
//  val humidityToLocation: Mapping,
)