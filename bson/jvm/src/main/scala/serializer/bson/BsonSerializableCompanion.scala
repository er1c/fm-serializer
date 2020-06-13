package serializer.bson

import org.bson.{BsonDocument, RawBsonDocument}
import serializer._

/**
 * Usage Pattern:
 *
 * {{
 * import serializer.SimpleSerializer
 * import serializer.bson.{BsonSerializableCompanion, BsonSerializableInstance}
 *
 * object Foo extends BsonSerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 *
 * final case class Foo(bar: String) extends BsonSerializableInstance[Foo] {
 *   protected def companion: BsonSerializableCompanion[Foo] = Foo
 * }
 * }}
 */
trait BsonSerializableCompanion[A] extends SerializableCompanion[A] {
  def toBsonBytes(v: A): Array[Byte] = BSON.toBsonBytes(v)(serializer)
  def fromBsonBytes(bytes: Array[Byte]): A = BSON.fromBsonBytes(bytes)(serializer)

  def toRawBsonDocument(v: A): RawBsonDocument = BSON.toRawBsonDocument(v)(serializer)
  def toBsonDocument(v: A): BsonDocument = BSON.toBsonDocument(v)(serializer)
  def addToBsonDocument(v: A, doc: BsonDocument): Unit = BSON.addToBsonDocument(v, doc)(serializer)

  def fromBsonDocument(doc: BsonDocument): A = BSON.fromBsonDocument(doc)(serializer)

  // TODO: Add BsonSerializableCompanion.validateBsonDocument
  //def validateBsonDocument(doc: BsonDocument): ValidationResult = BSON.validate(doc)(serializer)
}

/**
 * Usage Pattern:
 *
 * import serializer.{SerializableCompanion, SerializableInstance, SimpleSerializer}
 *
 * object Foo extends SerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 *
 * final case class Foo(bar: String) extends SerializableInstance[Foo] {
 *   protected def companion: SerializableCompanion[Foo] = Foo
 * }
 */
trait BsonSerializableInstance[A] extends SerializableInstance[A] { self: A =>
  type CompanionType = BsonSerializableCompanion[A]
  protected def companion: BsonSerializableCompanion[A]

  final def toRawBsonDocument: RawBsonDocument = companion.toRawBsonDocument(this)
  final def toBsonDocument: BsonDocument = companion.toBsonDocument(this)
  final def addToBsonDocument(doc: BsonDocument): Unit = companion.addToBsonDocument(this, doc)
}