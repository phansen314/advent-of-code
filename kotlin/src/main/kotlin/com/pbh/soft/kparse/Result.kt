package com.pbh.soft.kparse

/**
 * The result of applying [State] to [Parser]
 *
 * @param A type of value stored in this Result
 */
sealed class Result<out A> {
  /**
   * Indicates parser application failed
   *
   * @property label label of the parser that produced this error
   * @property position position where the error occurred
   * @property message error message
   */
  data class Err(
    val loc: Loc,
    val message: String
  ) : Result<Nothing>()

  /**
   * Indicates parser application succeeded
   *
   * @param A type of value produced by parser application
   * @property value value produced by parser application
   */
  data class Ok<out A>(val value: A) : Result<A>()
}