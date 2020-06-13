package serializer.jackson.json

object JsonOptions {
  val default: JsonOptions = JsonOptions()
  val minimal: JsonOptions = JsonOptions(ignoreNulls = true, ignoreFalse = true, ignoreZeros = true, ignoreEmptyObjects = true, ignoreEmptyArrays = true)
  val defaultWithoutNulls: JsonOptions = default.copy(ignoreNulls = true)
  val pretty: JsonOptions = default.copy(prettyFormat = true)
  val prettyWithoutNulls: JsonOptions = pretty.copy(ignoreNulls = true)
}

/**
 *
 * @param ignoreNulls Ignore fields with null values
 * @param ignoreFalse Ignore boolean fields with a value of false
 * @param ignoreZeros Ignore numeric fields with a value of zero
 * @param ignoreEmptyObjects Ignore objects that have no members
 * @param ignoreEmptyArrays Ignore arrays that have no elements
 * @param prettyFormat Use pretty formatting (when applicable).
 */
final case class JsonOptions(
  ignoreNulls: Boolean = false,
  ignoreFalse: Boolean = false,
  ignoreZeros: Boolean = false,
  ignoreEmptyObjects: Boolean = false,
  ignoreEmptyArrays: Boolean = false,
  prettyFormat: Boolean = false
) {
  def includeNulls: Boolean = !ignoreNulls
  def includeFalse: Boolean = !ignoreFalse
  def includeZeros: Boolean = !ignoreZeros
  def includeEmptyObjects: Boolean = !ignoreEmptyObjects
  def includeEmptyArrays: Boolean = !ignoreEmptyArrays
}
