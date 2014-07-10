package com.abdulradi.redikka.core.api

sealed trait RedikkaCommand

/*
 * Base class for all operations that targets a specific key
 */
sealed trait KeyCommand extends RedikkaCommand {
  def key: String
}
 /*
  * Base trait for all operations that may mutate data
  */
sealed trait KeyRWCommand extends KeyCommand

/*
 * Set: Stores the value into an actor identified by the `key`
 * value can be a String, Integer, or Double. TODO: let the compiler check this
 * Response is always Ok. Or Error
 */
@SerialVersionUID(1L)
final case class Set(key: String, value: String) extends KeyRWCommand

@SerialVersionUID(1L)
final case class Get(key: String) extends KeyCommand

sealed trait RedikkaResponse

sealed trait RedikkaSuccess extends RedikkaResponse

@SerialVersionUID(1L)
case object Ok extends RedikkaResponse

@SerialVersionUID(1L)
final case class Value(value: Option[String]) extends RedikkaResponse

sealed trait RedikkaFailure extends RedikkaResponse {
  def code: String
  def message: String
}
