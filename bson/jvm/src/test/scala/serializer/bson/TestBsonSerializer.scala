/*
 * Copyright 2016 Frugal Mechanic (http://frugalmechanic.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
package serializer.bson

import org.bson.types.ObjectId
import org.bson.{BsonDocument, BsonDocumentReader, RawBsonDocument}
import serializer.{Deserializer, Serializer, TestSerializer}

final class TestBsonDocument extends TestBsonSerializer[BsonDocument] {
  override def supportsRawCollections: Boolean = false
  override def supportsPrimitives: Boolean = false
  def serialize[T](v: T)(implicit ser: Serializer[T]): BsonDocument = BSON.toBsonDocument(v)
  def deserialize[T](doc: BsonDocument)(implicit deser: Deserializer[T]): T = BSON.fromBsonDocument[T](doc)
  def makeInput(bson: BsonDocument): BSONInput = new BSONInput(new BsonDocumentReader(bson))
}

final class TestBsonBytes extends TestBsonSerializer[Array[Byte]] {
  override def supportsRawCollections: Boolean = false
  override def supportsPrimitives: Boolean = false
  def serialize[T](v: T)(implicit ser: Serializer[T]): Array[Byte] = BSON.toBsonBytes(v)
  def deserialize[T](bson: Array[Byte])(implicit deser: Deserializer[T]): T = BSON.fromBsonBytes[T](bson)
  def makeInput(bson: Array[Byte]): BSONInput = new BSONInput(new BsonDocumentReader(new RawBsonDocument(bson)))
}

object TestBsonSerializer {
  case class BsonTypes(
    objectId: ObjectId = new ObjectId(),
    objectIdNull: ObjectId = null,
    someObjectId: Option[ObjectId] = Some(new ObjectId()),
    noneObjectId: Option[ObjectId] = None,
    objectIds: Vector[ObjectId] = Vector(new ObjectId(), new ObjectId(), new ObjectId(), new ObjectId())
    // Nulls within collections aren't fully supported yet
    //objectIdsWithNulls: Vector[ObjectId] = Vector(new ObjectId(), null, new ObjectId(), null)
  )
}

trait TestBsonSerializer[BYTES] extends TestSerializer[BYTES] {
  import TestBsonSerializer._

  test("BsonTypes") {
    val bsonTypes: BsonTypes = BsonTypes()

    val bytes: BYTES = serialize(bsonTypes)
    val bsonTypes2: BsonTypes = deserialize[BsonTypes](bytes)

    bsonTypes shouldBe bsonTypes2
  }
}
*/