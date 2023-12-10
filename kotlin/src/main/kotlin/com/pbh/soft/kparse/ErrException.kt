package com.pbh.soft.kparse

/**
 * DO NOT USE THIS CLASS!  Only exposed due to inline functions.
 *
 * Used internally to control execution within a [parser] block.
 */
class ErrException internal constructor(val err: Result.Err) : Exception(null as String?) {
  override fun fillInStackTrace(): Throwable = this
}