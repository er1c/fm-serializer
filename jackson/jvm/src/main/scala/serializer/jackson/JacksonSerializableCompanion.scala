package serializer.jackson

import serializer.SerializableCompanion
import serializer.jackson.json.{JsonNode, JsonObject}
import serializer.validation.ValidationResult

/**
 * Usage Pattern:
 *
 * {{
 * import serializer.SimpleSerializer
 * import serializer.protobuf.{JacksonSerializableCompanion, JacksonSerializableInstance}
 *
 * object Foo extends ProtobufSerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 *
 * final case class Foo(bar: String) extends JacksonSerializableInstance[Foo] {
 *   protected def companion: JacksonSerializableCompanion[Foo] = Foo
 * }
 * }}
 */
trait JacksonSerializableCompanion[A] extends SerializableCompanion[A] {
  //
  // Jackson JsonNode Methods
  //
  def fromJsonNode(node: JsonNode): A = Jackson.fromJsonNode(node)(serializer)
  def toJsonNode(v: A): JsonNode = Jackson.toJsonNode(v)(serializer)
  def toJsonNodeWithoutNulls(v: A): JsonNode = Jackson.toJsonNodeWithoutNulls(v)(serializer)
  def validateJsonNode(node: JsonNode): ValidationResult = Jackson.validate(node)(serializer)

  //
  // Jackson JsonObject Methods
  //
  def fromJsonObject(node: JsonObject): A = Jackson.fromJsonNode(node)(serializer)
  def toJsonObject(v: A): JsonObject = Jackson.toJsonObject(v)(serializer)
  def toJsonObjectWithoutNulls(v: A): JsonObject = Jackson.toJsonObjectWithoutNulls(v)(serializer)
  def validateJsonObject(node: JsonObject): ValidationResult = Jackson.validate(node)(serializer)
}