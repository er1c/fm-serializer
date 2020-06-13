package serializer

import serializer.protobuf.Protobuf

/**
 * Usage Pattern:
 *
 * {{
 * import serializer.SimpleSerializer
 * import serializer.protobuf.{ProtobufSerializableCompanion, ProtobufSerializableInstance}
 *
 * object Foo extends ProtobufSerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 *
 * final case class Foo(bar: String) extends ProtobufSerializableInstance[Foo] {
 *   protected def companion: ProtobufSerializableCompanion[Foo] = Foo
 * }
 * }}
 */
trait ProtobufSerializableCompanion[A] extends SerializableCompanion[A] {
  def toBytes(v: A): Array[Byte] = Protobuf.toBytes(v)(serializer)
  def fromBytes(bytes: Array[Byte]): A = Protobuf.fromBytes(bytes)(serializer)
}