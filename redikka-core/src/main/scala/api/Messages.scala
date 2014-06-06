package com.abdulradi.redikka.core.api

sealed trait RedikkaCommand

/*  
 * Base class for all operations that targets a specific key 
 */
sealed trait KeyOperation extends RedikkaCommand {
  def key: String
}

/*  
 * Set: Stores the value into an actor identified by the `key`
 * value can be a String, Integer, or Double. TODO: let the compiler check this
 * Response is always Ok. Or Error
 */
case class Set(key: String, value: AnyVal) extends KeyOperation

case class Get(key: String) extends KeyOperation

sealed trait RedikkaResponse

sealed trait RedikkaSuccess extends RedikkaResponse
case object Ok

sealed trait RedikkaFailure extends RedikkaResponse {
  def code: String
  def message: String
}
