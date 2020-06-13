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

import com.fasterxml.jackson.core.json.JsonReadContext
import com.fasterxml.jackson.core.{Base64Variant, JsonLocation, JsonParseException, JsonParser, JsonStreamContext, JsonToken, ObjectCodec, Version}
import java.math.{BigDecimal, BigInteger}
import serializer.internal._

object JsonNodeParser {

  private case class ObjectLocation(node: JsonObject, parent: Location, var idx: Int = -1, var isFieldName: Boolean = true) extends Location {
    def increment(): Boolean = {
      if (isEOF) return false

      if (idx < 0 || idx == node.members.size) {
        idx += 1
      } else if (isFieldName) {
        // Same member but switch from JsonToken.FIELD_NAME to the actual value
        isFieldName = false
      } else {
        // Advance to the next member and set back to JsonToken.FIELD_NAME
        idx += 1
        isFieldName = true
      }

      !isEOF
    }

    def currentToken(): JsonToken = {
      if (isEOF) null
      else if (idx < 0) JsonToken.START_OBJECT
      else if (idx == node.members.size) JsonToken.END_OBJECT
      else if (isFieldName) JsonToken.FIELD_NAME
      else currentNode.asToken
    }

    def currentFieldName: String = {
      if (idx >= 0 && idx < node.members.size) node.members(idx)._1
      else if (null != parent) parent.currentFieldName
      else null
    }

    def currentNode: JsonNode = {
      if (idx >= 0 && idx < node.members.size) node.members(idx)._2
      else null
    }

    def isValue: Boolean = !isFieldName
    def isEOF: Boolean = idx > node.members.size
  }

  private case class ArrayLocation(node: JsonArray, parent: Location, var idx: Int = -1) extends Location {
    def increment(): Boolean = {
      if (isEOF) return false
      idx += 1
      !isEOF
    }

    def isEOF: Boolean = idx > node.elements.size

    def currentToken(): JsonToken = {
      if (isEOF) null
      else if (idx < 0) JsonToken.START_ARRAY
      else if (idx == node.elements.size) JsonToken.END_ARRAY
      else currentNode.asToken
    }

    def currentNode: JsonNode = {
      if (idx >= 0 && idx < node.elements.size) node.elements(idx)
      else null
    }

    def currentFieldName: String = {
      if (null != parent) parent.currentFieldName
      else null
    }
  }

  private case class ValueLocation(node: JsonNode) extends Location {
    private var hasBeenRead: Boolean = false

    // Not Applicable for Value Locations
    def parent: Location = null

    def increment(): Boolean = {
      if (!hasBeenRead) {
        hasBeenRead = true
        true
      } else {
        false
      }
    }

    def currentToken(): JsonToken = node.asToken
    def currentFieldName: String = null
    def currentNode: JsonNode = node
  }

  private object Location {
    def makeInitialLocation(node: JsonNode): Location = {
      node match {
        case n: JsonValue => ValueLocation(n)
        case n: JsonObject => ObjectLocation(n, null, -2)
        case n: JsonArray => ArrayLocation(n, null, -2)
      }
    }
  }

  private sealed abstract class Location {
    def parent: Location
    def node: JsonNode
    def increment(): Boolean
    def currentToken(): JsonToken
    def currentFieldName: String
    def currentNode: JsonNode
  }
}

/**
 * This creates a Jackson JsonParser for a given JsonNode.
 *
 * This is a quick and dirty implementation that probably needs some work.
 *
 * Note: If you are trying to parse a JsonNode from a JsonParser then use the JsonNode.parse() method.
 *
 * @param node The JsonNode to create a JsonParser for
 */
final case class JsonNodeParser(node: JsonNode) extends JsonParser {
  import JsonNodeParser._

  private[this] var closed: Boolean = false
  private[this] var location: Location = Location.makeInitialLocation(node)
  private[this] var currentToken: JsonToken = null
  private[this] var lastToken: JsonToken = null

  private[this] val readContext: JsonReadContext = new JsonReadContext(null, null, 0, 0, 0)

  private def currentNode: JsonNode = location.currentNode

  override def getCodec: ObjectCodec = null
  override def setCodec(c: ObjectCodec): Unit = {} // nop
  override def version(): Version = null

  override def close(): Unit = closed = true
  override def isClosed: Boolean = closed

  override def getParsingContext: JsonStreamContext = readContext // dummy value
  override def getTokenLocation: JsonLocation = JsonLocation.NA   // dummy value
  override def getCurrentLocation: JsonLocation = JsonLocation.NA // dummy value

  override def nextToken(): JsonToken = {
    if (closed || null == location) return null

    // Attempt to increment our location.  While we cannot pop up to the parent location.
    while (null != location && !location.increment()) {
      location = location.parent
    }

    if (location.isNull) {
      currentToken = null // EOF
    } else {
      currentToken = location.currentToken()

      if (JsonToken.START_OBJECT == currentToken || JsonToken.START_ARRAY == currentToken) {
        // If we are pointing to a non FIELD_NAME value is that an Object or Array we need to recurse our location into it
        location.currentNode match {
          case null => // Do Nothing
          case n: JsonObject => location = ObjectLocation(n, location)
          case n: JsonArray => location = ArrayLocation(n, location)
          case _ => // Do Nothing
        }
      }
    }

    currentToken
  }

  override def nextValue(): JsonToken = {
    if (closed) return null

    val token: JsonToken = nextToken()

    if (null == token) null
    else if (JsonToken.FIELD_NAME == token) nextToken()
    else token
  }

  override def skipChildren(): JsonParser = {
    location match {
      case _: ValueLocation => this // Do nothing
      case _: ObjectLocation => skipChildrenImpl()
      case _: ArrayLocation => skipChildrenImpl()
    }
  }

  private def skipChildrenImpl(): JsonParser = {
    clearCurrentToken()
    location = location.parent
    this
  }

  override def getCurrentToken: JsonToken = currentToken
  override def getCurrentTokenId: Int = getCurrentToken.id()
  override def hasCurrentToken: Boolean = null != currentToken
  override def hasTokenId(id: Int): Boolean = getCurrentTokenId == id
  override def hasToken(t: JsonToken): Boolean = getCurrentToken == t

  override def clearCurrentToken(): Unit = {
    lastToken = currentToken
    currentToken = null
  }

  override def getLastClearedToken: JsonToken = lastToken

  override def overrideCurrentName(name: String): Unit = {} // nop - Do not think we need this
  override def getCurrentName: String = location.currentFieldName

  override def getText: String = {
    if (getCurrentToken == JsonToken.FIELD_NAME) getCurrentName
    else getValueAsString(null)
  }

  override def getTextCharacters: Array[Char] = getText.toCharArray
  override def getTextLength: Int = getText.length
  override def getTextOffset: Int = 0
  override def hasTextCharacters: Boolean = false // should hopefully cause getTextCharacters not to be called

  override def getNumberValue: Number = {
    currentNode match {
      case number: JsonNumber => number.asNumber
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_INT or JsonToken.NUMBER_VALUE_FLOAT: $currentNode")
    }
  }

  override def getNumberType: JsonParser.NumberType = {
    currentNode match {
      case number: JsonNumber => number.numberType
      case _ => null
    }
  }

  override def getIntValue: Int = {
    currentNode match {
      case number: JsonNumber if number.isInt => number.toInt
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_INT or cannot be converted: $currentNode")
    }
  }

  override def getLongValue: Long = {
    currentNode match {
      case number: JsonNumber if number.isLong => number.toLong
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_INT or cannot be converted: $currentNode")
    }
  }

  override def getBigIntegerValue: BigInteger = {
    currentNode match {
      case number: JsonNumber if number.isBigInteger => number.toBigInteger
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_INT: $currentNode")
    }
  }

  override def getFloatValue: Float = {
    currentNode match {
      case number: JsonNumber if number.isFloat => number.toFloat
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_FLOAT or cannot be converted: $currentNode")
    }
  }

  override def getDoubleValue: Double = {
    currentNode match {
      case number: JsonNumber if number.isDouble => number.toDouble
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_FLOAT or cannot be converted: $currentNode")
    }
  }

  override def getDecimalValue: BigDecimal = {
    currentNode match {
      case number: JsonNumber => number.asBigDecimal
      case _ => throw new JsonParseException(this, s"Current value is not JsonToken.VALUE_NUMBER_FLOAT or cannot be converted: $currentNode")
    }
  }

  override def getBinaryValue(bv: Base64Variant): Array[Byte] = {
    // TODO: Create a JsonBinary implementation for JsonNode that is more efficient?
    bv.decode(getText)
  }

  override def getValueAsString(default: String): String = {
    currentNode match {
      case value: JsonValue => value.asString
      case _ => default
    }
  }
}
