/*
 * Copyright 2019 Frugal Mechanic (http://frugalmechanic.com)
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
package serializer.jackson

import serializer.jackson.json.{Json, JsonNode}
import serializer.{Deserializer, Serializer}
import serializer.json.{JSON, JSONDeserializerOptions, TestJSONSerializer}
import serializer.jackson.Implicits._

final class TestJackson extends TestJSONSerializer {
  import serializer.TestSerializerBase.Foo

  override protected def allowsUnquotedStringValues: Boolean = false

  def serialize[T](v: T)(implicit ser: Serializer[T]): String = Jackson.toJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = Jackson.fromJSON[T](json)
  def makeInput(json: String): JsonParserInput = new JsonParserInput(Jackson.defaultJsonFactory.createParser(json), JSONDeserializerOptions.default)

  test("JsonNode") {
    val foo: Foo = Foo()

    val jsonNode: JsonNode = Jackson.toJsonNode(foo)

    // Serialized JsonNode should match Jackson.toJSON/toPrettyJSON
    jsonNode.toCompactJson() shouldBe Jackson.toJSON(foo)
    jsonNode.toPrettyJson() shouldBe Jackson.toPrettyJSON(foo)

    // Should be able to deserialize back to a Foo instance
    Jackson.fromJsonNode[Foo](jsonNode) shouldBe foo

    // These do not work due to parsing of floating points into BigDecimal instead of Floats by default.
//    JsonNode.parse(jsonNode.toCompactJson()) shouldBe jsonNode
//    JsonNode.parse(jsonNode.toPrettyJson()) shouldBe jsonNode
  }

  test("serializer.jackson <=> serializer.json") {
    val foo: Foo = Foo()

    // These should match
    val fromJackson = Jackson.toJSON(foo)
    val fromJson = JSON.toJSON(foo)
    fromJackson shouldBe fromJson

    // The normal toJSON should match the "compact" representation
    Jackson.toJSON(foo) shouldBe Json.toCompactJsonString(Jackson.toPrettyJSON(foo))
    JSON.toJSON(foo) shouldBe Json.toCompactJsonString(JSON.toPrettyJSON(foo))

    // Both serializer.jackson and serializer.json should serialize to the same compact string
    Json.toCompactJsonString(Jackson.toPrettyJSON(foo)) shouldBe Json.toCompactJsonString(JSON.toPrettyJSON(foo))
  }
}