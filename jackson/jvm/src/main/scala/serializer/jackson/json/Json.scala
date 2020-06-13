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
package serializer.jackson.json

import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator, JsonParser, JsonToken}
import java.io.StringWriter

object Json {

  def toCompactJsonString(json: String)(implicit jsonFactory: JsonFactory): String = toCompactJsonString(jsonFactory.createParser(json))

  def toCompactJsonString(parser: JsonParser)(implicit jsonFactory: JsonFactory): String = {
    val sw: StringWriter = new StringWriter()
    val generator: JsonGenerator = jsonFactory.createGenerator(sw)
    pipe(parser, generator)
    generator.close()
    sw.toString
  }

  /**
   * Pipe a JsonParser to a JsonGenerator
   * @param parser
   * @param generator
   */
  def pipe(parser: JsonParser, generator: JsonGenerator): Unit = {
    var token: JsonToken = if (!parser.hasCurrentToken) parser.nextToken() else parser.currentToken()

    while (null != token) {
      token match {
        case JsonToken.VALUE_NULL => generator.writeNull()
        case JsonToken.VALUE_TRUE => generator.writeBoolean(true)
        case JsonToken.VALUE_FALSE => generator.writeBoolean(false)
        case JsonToken.VALUE_STRING => if (parser.hasTextCharacters) generator.writeString(parser.getTextCharacters, parser.getTextOffset, parser.getTextLength) else generator.writeString(parser.getText)
        case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT =>
          parser.getNumberType match {
            case JsonParser.NumberType.BIG_DECIMAL | JsonParser.NumberType.DOUBLE | JsonParser.NumberType.FLOAT => generator.writeNumber(parser.getDecimalValue)
            case JsonParser.NumberType.BIG_INTEGER => generator.writeNumber(parser.getBigIntegerValue)
            case JsonParser.NumberType.INT => generator.writeNumber(parser.getIntValue)
            case JsonParser.NumberType.LONG => generator.writeNumber(parser.getLongValue)
          }
        case JsonToken.START_ARRAY => generator.writeStartArray()
        case JsonToken.START_OBJECT => generator.writeStartObject()
        case JsonToken.END_ARRAY => generator.writeEndArray()
        case JsonToken.END_OBJECT => generator.writeEndObject()
        case JsonToken.FIELD_NAME => generator.writeFieldName(parser.getCurrentName)
        case JsonToken.VALUE_EMBEDDED_OBJECT | JsonToken.NOT_AVAILABLE => throw new IllegalStateException(s"Unexpected JsonToken: $token")
      }

      token = parser.nextToken()
    }
  }
}
