package serializer

import serializer.json.JSON
import serializer.validation.ValidationResult

/**
 * Usage Pattern:
 *
 * {{
 * import  serializer.SimpleSerializer
 * import serializer.json.{JSONSerializableCompanion, JSONSerializableInstance}
 *
 * object Foo extends JSONSerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 *
 * final case class Foo(bar: String) extends JSONSerializableInstance[Foo] {
 *   protected def companion: JSONSerializableCompanion[Foo] = Foo
 * }
 * }}
 */
trait JSONSerializableCompanion[A] extends SerializableCompanion[A] {
  def toJSON(v: A): String = JSON.toJSON(v)(serializer)
  def toJSONWithoutNulls(v: A): String = JSON.toJSONWithoutNulls(v)(serializer)
  def toMinimalJSON(v: A): String = JSON.toMinimalJSON(v)(serializer)
  def toPrettyJSON(v: A): String = JSON.toPrettyJSON(v)(serializer)
  def toPrettyJSONWithoutNulls(v: A): String = JSON.toPrettyJSONWithoutNulls(v)(serializer)
  def fromJSON(json: String): A = JSON.fromJSON(json)(serializer)
  def validateJson(json: String): ValidationResult = JSON.validate(json)(serializer)
}