/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
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
package serializer

import scala.collection.mutable.Builder

final class StringMapOpsDeserializer[V,Col](newBuilder: => Builder[(String,V), Col])(implicit valueDeser: Deserializer[V], elemDeser: Deserializer[(String,V)]) extends Deserializer[Col] {
  // If the underlying input doesn't support a StringMap type of input (e.g. Protobuf) then we will use this CanBuildFromDeserializer instead
  private[this] lazy val mapDeserializer: MapOpsDeserializer[String,V, (String,V), Col] = new MapOpsDeserializer[String,V,(String,V),Col](newBuilder)

  // TODO: make this a macro to use a Col.empty method (if one exists)
  def defaultValue: Col = newBuilder.result

  def deserializeRaw(input: RawInput): Col =
    if (input.allowStringMap) input.readRawObject{ readCollection }
    else { mapDeserializer.deserializeRaw(input) }

  def deserializeNested(input: NestedInput): Col =
    if (input.allowStringMap) input.readNestedObject{ readCollection }
    else mapDeserializer.deserializeNested(input)

  private def readCollection(input: FieldInput): Col = {
    val builder: Builder[(String,V),Col] = newBuilder

    var name: String = input.readFieldName()

    while (name != null) {
      builder += ((name, valueDeser.deserializeNested(input)))
      name = input.readFieldName()
    }

    builder.result
  }
}