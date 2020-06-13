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

import com.fasterxml.jackson.core.{JsonGenerator, JsonParseException, JsonParser, JsonPointer, JsonToken}
import java.io.{ByteArrayOutputStream, StringWriter}
import java.math.{BigDecimal, BigInteger}
import java.util.{Base64, NoSuchElementException}
import scala.collection.mutable
import scala.util.Try
import serializer.jackson.Jackson

object JsonBinary extends JsonNodeParseFactory[Array[Byte], JsonBinary] {
  def apply(data: Array[Byte], offset: Int, len: Int): JsonBinary = {
    JsonBinary(java.util.Arrays.copyOfRange(data, offset, offset + len))
  }

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Array[Byte] = {
    currentTokenOrAdvance(parser)
    val bos: ByteArrayOutputStream = new ByteArrayOutputStream()
    parser.readBinaryValue(bos)
    bos.toByteArray
  }
}

final case class JsonBinary(value: Array[Byte]) extends JsonValue {
  override def asString: String = Base64.getEncoder.encodeToString(value)
  override def asToken: JsonToken = JsonToken.VALUE_STRING // This is serialized as Base64
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeBinary(value)
}

object JsonString extends JsonNodeParseFactory[String, JsonString] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): String = {
    val token: JsonToken = currentTokenOrAdvance(parser)
    requireParser(parser, token.isScalarValue, s"Expected a Scalar JSON Value but got $token")
    if (token == JsonToken.VALUE_NULL) "" else parser.getValueAsString()
  }
}

final case class JsonString(value: String) extends JsonValue {
  override def asString: String = value
  override def asToken: JsonToken = JsonToken.VALUE_STRING
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeString(value)
}

object JsonDouble extends JsonNodeParseFactory[Double, JsonDouble] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Double = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT => parser.getDoubleValue
      case JsonToken.VALUE_STRING => parser.getValueAsString.toDouble
      case _ => throw new JsonParseException(parser, s"Unable to parse $token as Double")
    }
  }
}

/**
 * Note: JsonNode.parse will parse all floating point numbers into BigDecimal but this can be used for explicit doubles
 */
final case class JsonDouble(value: Double) extends JsonFloatingPoint {
  override def asString: String = String.valueOf(value)
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = BigDecimal.valueOf(value)
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.DOUBLE
  override def isZero: Boolean = value == 0D
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)

  override def isFloat: Boolean = {
    val float: Float = value.toFloat
    Float.NegativeInfinity != float && Float.PositiveInfinity != float
  }

  override def toFloat: Float = if (isFloat) value.toFloat else throw new ArithmeticException(s"Cannot convert Double to Float: $value")
  override def toFloatOption: Option[Float] = if (isFloat) Some(value.toFloat) else None

  override def isDouble: Boolean = true
  override def toDouble: Double = value
  override def toDoubleOption: Option[Double] = Some(value)
}

object JsonFloat extends JsonNodeParseFactory[Float, JsonFloat] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Float = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT => parser.getFloatValue
      case JsonToken.VALUE_STRING => parser.getValueAsString.toFloat
      case _ => throw new JsonParseException(parser, s"Unable to parse $token as Flaot")
    }
  }
}

/**
 * Note: JsonNode.parse will parse all floating point numbers into BigDecimal but this can be used for explicit floats
 */
final case class JsonFloat(value: Float) extends JsonFloatingPoint {
  override def asString: String = String.valueOf(value)
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = BigDecimal.valueOf(value.toDouble)
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.FLOAT
  override def isZero: Boolean = value == 0F
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)

  override def isFloat: Boolean = true
  override def toFloat: Float = value
  override def toFloatOption: Option[Float] = Some(value)

  override def isDouble: Boolean = true
  override def toDouble: Double = value.toDouble
  override def toDoubleOption: Option[Double] = Some(value.toDouble)
}

object JsonInt extends JsonNodeParseFactory[Int, JsonInt] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Int = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT => parser.getIntValue
      case JsonToken.VALUE_STRING => parser.getValueAsString.toInt
      case _ => throw new JsonParseException(parser, s"Unable to parse $token as Int")
    }
  }
}

final case class JsonInt(value: Int) extends JsonInteger {
  override def asString: String = String.valueOf(value)
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = BigDecimal.valueOf(value)
  override def asBigInteger: BigInteger = BigInteger.valueOf(value)
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.INT
  override def isZero: Boolean = value == 0
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)

  override def isInt: Boolean = true
  override def toInt: Int = value
  override def toIntOption: Option[Int] = Some(value)

  override def isLong: Boolean = true
  override def toLong: Long = value
  override def toLongOption: Option[Long] = Some(value)
}

object JsonLong extends JsonNodeParseFactory[Long, JsonLong] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Long = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT => parser.getLongValue
      case JsonToken.VALUE_STRING => parser.getValueAsString.toLong
      case _ => throw new JsonParseException(parser, s"Unable to parse $token as Long")
    }
  }
}

final case class JsonLong(value: Long) extends JsonInteger {
  override def asString: String = String.valueOf(value)
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = BigDecimal.valueOf(value)
  override def asBigInteger: BigInteger = BigInteger.valueOf(value)
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.LONG
  override def isZero: Boolean = value == 0L
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)

  override def isInt: Boolean = value >= Int.MinValue && value <= Int.MaxValue
  override def toInt: Int = if (isInt) value.toInt else throw new ArithmeticException(s"Cannot convert $value to an int")
  override def toIntOption: Option[Int] = if (isInt) Some(value.toInt) else None

  override def isLong: Boolean = true
  override def toLong: Long = value
  override def toLongOption: Option[Long] = Some(value)
}

object JsonBigInteger extends JsonNodeParseFactory[BigInteger, JsonBigInteger] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): BigInteger = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NUMBER_INT => parser.getBigIntegerValue
      case JsonToken.VALUE_NUMBER_FLOAT => parser.getDecimalValue.toBigIntegerExact
      case _ => throw new JsonParseException(parser, s"Expected JsonToken.VALUE_NUMBER_INT or JsonToken.VALUE_NUMBER_FLOAT but got: $token")
    }
  }
}

final case class JsonBigInteger(value: BigInteger) extends JsonInteger {
  override def asString: String = value.toString
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = new BigDecimal(value)
  override def asBigInteger: BigInteger = value
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.BIG_INTEGER
  override def isZero: Boolean = value == BigInteger.ZERO
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)

  override def isBigInteger: Boolean = true
  override def toBigInteger: BigInteger = value
  override def toBigIntegerOption: Option[BigInteger] = Some(value)
}

object JsonBigDecimal extends JsonNodeParseFactory[BigDecimal, JsonBigDecimal] {
  override protected def parseImpl(parser: JsonParser, options: JsonOptions): BigDecimal = {
    val token: JsonToken = currentTokenOrAdvance(parser)
    requireParser(parser, token.isNumeric, "Expected JsonToken.VALUE_NUMBER_INT or JsonToken.VALUE_NUMBER_FLOAT")
    parser.getDecimalValue
  }
}

final case class JsonBigDecimal(value: BigDecimal) extends JsonFloatingPoint {
  override def asString: String = value.toString
  override def asNumber: Number = value
  override def asBigDecimal: BigDecimal = value
  override def numberType: JsonParser.NumberType = JsonParser.NumberType.BIG_DECIMAL
  override def isZero: Boolean = value == BigDecimal.ZERO
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNumber(value)
}

sealed abstract class JsonInteger extends JsonNumber {
  def asBigInteger: BigInteger
  final override def asToken: JsonToken = JsonToken.VALUE_NUMBER_INT
}

sealed abstract class JsonFloatingPoint extends JsonNumber {
  final override def asToken: JsonToken = JsonToken.VALUE_NUMBER_FLOAT
}

object JsonNumber {
  /**
   * Given a BigDecimal find the JsonNumber type that is the best fit (e.g. JsonInt, JsonLong, JsonBigInteger or JsonBigDecimal)
   */
  def bestFitFor(v: BigDecimal): JsonNumber = {
    Try{ JsonInt(v.intValueExact()) } orElse Try{ JsonLong(v.longValueExact()) } orElse Try{ JsonBigInteger(v.toBigIntegerExact) } getOrElse JsonBigDecimal(v)
  }

  /**
   * Given a BigInteger find the JsonNumber type that is the best fit (e.g. JsonInt, JsonLong, or JsonBigInteger)
   */
  def bestFitFor(v: BigInteger): JsonNumber = {
    Try{ JsonInt(v.intValueExact()) } orElse Try{ JsonLong(v.longValueExact()) } getOrElse JsonBigInteger(v)
  }
}

sealed abstract class JsonNumber extends JsonValue {
  def asNumber: Number
  def asBigDecimal: BigDecimal
  def numberType: JsonParser.NumberType

  /** Is this number zero? */
  def isZero: Boolean

  final def isNotZero: Boolean = !isZero

  /** Can this value be converted to an int? */
  def isInt: Boolean = Try{ asBigDecimal.intValueExact() }.isSuccess

  /** Convert this value to an int throwing an exception if exact conversion is not possible */
  def toInt: Int = asBigDecimal.intValueExact()
  
  /** Optionally convert this value to an int */
  def toIntOption: Option[Int] = Try{ asBigDecimal.intValueExact() }.toOption

  /** Can this value be converted to an long? */
  def isLong: Boolean = Try{ asBigDecimal.longValueExact() }.isSuccess

  /** Convert this value to an long throwing an exception if exact conversion is not possible */
  def toLong: Long = asBigDecimal.longValueExact()

  /** Optionally convert this value to an long */
  def toLongOption: Option[Long] = Try{ asBigDecimal.longValueExact() }.toOption

  /** Can this value be converted to an float? */
  def isFloat: Boolean = {
    val float: Float = asBigDecimal.floatValue()
    Float.NegativeInfinity != float && Float.PositiveInfinity != float
  }

  /** Convert this value to an float throwing an exception if exact conversion is not possible */
  def toFloat: Float = if (isFloat) asBigDecimal.floatValue else throw new ArithmeticException(s"Cannot convert Double to Float: $asBigDecimal")

  /** Optionally convert this value to an float */
  def toFloatOption: Option[Float] = if (isFloat) Some(toFloat) else None

  /** Can this value be converted to an double? */
  def isDouble: Boolean = {
    val double: Double = asBigDecimal.doubleValue()
    Double.NegativeInfinity != double && Double.PositiveInfinity != double
  }

  /** Convert this value to an double throwing an exception if exact conversion is not possible */
  def toDouble: Double = asBigDecimal.doubleValue

  /** Optionally convert this value to an double */
  def toDoubleOption: Option[Double] = if (isDouble) Some(toDouble) else None

  /** Can this value be converted to an bigInteger? */
  def isBigInteger: Boolean = Try{ asBigDecimal.toBigIntegerExact() }.isSuccess

  /** Convert this value to an bigInteger throwing an exception if exact conversion is not possible */
  def toBigInteger: BigInteger = asBigDecimal.toBigIntegerExact()

  /** Optionally convert this value to an bigInteger */
  def toBigIntegerOption: Option[BigInteger] = Try{ asBigDecimal.toBigIntegerExact() }.toOption
}

object JsonTrue extends JsonTrue with JsonNodeParseFactory[Boolean, JsonTrue] {
  def apply(value: Boolean): JsonTrue = {
    require(value)
    this
  }

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Boolean = {
    val value: Boolean = JsonBoolean.parse(parser, options)
    requireParser(parser, value, s"Expected true value but got: $value")
    value
  }
}

sealed abstract class JsonTrue extends JsonBoolean(true) {
  override def asString: String = "true"
  override def asToken: JsonToken = JsonToken.VALUE_TRUE
}

object JsonFalse extends JsonFalse with JsonNodeParseFactory[Boolean, JsonFalse] {
  def apply(value: Boolean): JsonFalse = {
    require(!value)
    this
  }

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Boolean = {
    val value: Boolean = JsonBoolean.parse(parser, options)
    requireParser(parser, !value, s"Expected false value but got: $value")
    value
  }
}

sealed abstract class JsonFalse extends JsonBoolean(false) {
  override def asString: String = "false"
  override def asToken: JsonToken = JsonToken.VALUE_FALSE
}

object JsonBoolean extends JsonNodeParseFactory[Boolean, JsonBoolean] {
  def apply(value: Boolean): JsonBoolean = if (value) JsonTrue else JsonFalse

  def unapply(v: JsonNode): Option[Boolean] = {
    v match {
      case node: JsonBoolean => Some(node.value)
      case _ => None
    }
  }

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Boolean = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_FALSE => false
      case JsonToken.VALUE_TRUE => true
      case JsonToken.VALUE_NUMBER_INT =>
        val num: Int = parser.getIntValue()
        requireParser(parser, num == 0 || num == 1, s"Cannot convert $num to Boolean")
        num == 1

      case JsonToken.VALUE_STRING =>
        val s: String = parser.getValueAsString()
        s match {
          case "true" | "1" => true
          case "false" | "0" => false
          case _ => throw new JsonParseException(parser, s"Unable to parse $s as Boolean")
        }

      case _ => throw new JsonParseException(parser, s"Unable to parse $token as Boolean")
    }
  }
}

sealed abstract class JsonBoolean(val value: Boolean) extends JsonValue {
  final override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeBoolean(value)
}

object JsonNull extends JsonNull with JsonNodeParseFactory[Null, JsonNull] {
  def apply(n: Null): JsonNull = this

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): Null = {
    currentTokenOrAdvance(parser)
    requireToken(parser, JsonToken.VALUE_NULL)
    null
  }
}

sealed abstract class JsonNull extends JsonValue {
  override def asString: String = "null"
  override def asToken: JsonToken = JsonToken.VALUE_NULL
  override def write(gen: JsonGenerator, options: JsonOptions): Unit = gen.writeNull()
  override def isJsonNull: Boolean = false
}

object JsonValue extends JsonValueFactory with JsonNodeParseFactory[JsonValue, JsonValue] {
  def apply(node: JsonValue): JsonValue = node

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): JsonValue = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NULL => JsonNull
      case JsonToken.VALUE_TRUE => JsonTrue
      case JsonToken.VALUE_FALSE => JsonFalse
      case JsonToken.VALUE_STRING => JsonString(parser.getValueAsString)
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT =>
        parser.getNumberType match {
          case JsonParser.NumberType.BIG_DECIMAL | JsonParser.NumberType.DOUBLE | JsonParser.NumberType.FLOAT => JsonBigDecimal(parser.getDecimalValue)
          case JsonParser.NumberType.BIG_INTEGER => JsonBigInteger(parser.getBigIntegerValue)
          case JsonParser.NumberType.INT => JsonInt(parser.getIntValue)
          case JsonParser.NumberType.LONG => JsonLong(parser.getLongValue)
        }
      case _ => throw new JsonParseException(parser, s"Cannot parse $token as JsonValue")
    }
  }
}

sealed abstract class JsonValue extends JsonNode {
  def asString: String
  override def isJsonValue: Boolean = true
  override def isJsonContainer: Boolean = false
  override def isJsonArray: Boolean = false
  override def isJsonObject: Boolean = false
  override protected def atImpl(ptr: JsonPointer): Option[JsonNode] = None
}

object JsonObject extends JsonNodeParseFactory[IndexedSeq[(String, JsonNode)], JsonObject] {
  def apply(members: (String, JsonNode)*): JsonObject = JsonObject(members.toIndexedSeq)
  def apply(members: Iterable[(String, JsonNode)]): JsonObject = JsonObject(members.toIndexedSeq)

  /** De-dupe the members by the field name.  The last dupe wins. */
  private def deduped(members: IndexedSeq[(String, JsonNode)]): JsonObject = {
    val arr: Array[(String, JsonNode)] = new Array(members.length)
    var idx: Int = 0

    val nameToIdx: mutable.HashMap[String, Int] = new mutable.HashMap()

    members.foreach { pair: (String, JsonNode) =>
      nameToIdx.get(pair._1) match {
        case Some(existingIdx) => arr(existingIdx) = pair
        case None => arr(idx) = pair; nameToIdx(pair._1) = idx; idx += 1
      }
    }

    if (idx == arr.length) JsonObject(arr)
    else JsonObject(java.util.Arrays.copyOf(arr, idx))
  }

  val empty: JsonObject = JsonObject()

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): IndexedSeq[(String, JsonNode)] = {
    currentTokenOrAdvance(parser)
    requireToken(parser, JsonToken.START_OBJECT)
    parser.nextToken() // Advance past the START_OBJECT token

    val builder = Vector.newBuilder[(String, JsonNode)]

    while (!hasTokenOrAdvance(parser, JsonToken.END_OBJECT)) {
      if (!parser.hasToken(JsonToken.FIELD_NAME)) throw new JsonParseException(parser, s"Expected JsonToken.FIELD_NAME but got: ${parser.currentToken()}")
      parser.nextToken() // Advance past the FIELD_NAME token

      val fieldName: String = parser.getCurrentName
      val fieldValue: JsonNode = JsonNode.parse(parser, options)

      if (shouldIncludeField(fieldName, fieldValue, options)) {
        builder += ((fieldName, fieldValue))
      }
    }

    builder.result
  }

  private[json] def shouldIncludeField(pair: (String, JsonNode), options: JsonOptions): Boolean = {
    shouldIncludeField(pair._1, pair._2, options)
  }

  private[json] def shouldIncludeField(name: String, value: JsonNode, options: JsonOptions): Boolean = {
    value match {
      case n: JsonNumber => options.includeZeros || n.isNotZero
      case b: JsonBoolean => options.includeFalse || b.value
      case o: JsonObject => options.includeEmptyObjects || o.members.exists{ shouldIncludeField(_, options) }
      case a: JsonArray => options.includeEmptyArrays || a.isNotEmpty
      case JsonNull => options.includeNulls
      case _ => true
    }
  }
}

final case class JsonObject(members: IndexedSeq[(String, JsonNode)]) extends JsonContainerNode {
  def fieldNames: IndexedSeq[String] = members.map{ _._1 }

  override def size: Int = members.size
  override def apply(index: Int): JsonNode = members(index)._2
  override def get(index: Int): Option[JsonNode] = if (index < 0 || index >= members.length) None else Some(members(index)._2)

  override def isJsonArray: Boolean = false
  override def isJsonObject: Boolean = true

  def apply(fieldName: String): JsonNode = get(fieldName).getOrElse{ throw new NoSuchElementException(s"No $fieldName member") }
  def get(fieldName: String): Option[JsonNode] = members.find{ _._1 == fieldName }.map{ _._2 }

  override def write(gen: JsonGenerator, options: JsonOptions): Unit = {
    gen.writeStartObject()

    members.foreach{ pair: (String, JsonNode) =>
      if (JsonObject.shouldIncludeField(pair, options)) {
        gen.writeFieldName(pair._1)
        pair._2.write(gen, options)
      }
    }

    gen.writeEndObject()
  }

  def toJsonObject(options: JsonOptions): JsonObject = toJsonNode(options).asInstanceOf[JsonObject]

  override def asToken: JsonToken = JsonToken.START_OBJECT
  override protected def atImpl(ptr: JsonPointer): Option[JsonNode] = get(ptr.getMatchingProperty)

  def ++(other: JsonObject): JsonObject = ++(other.members)
  def ++(otherMembers: IndexedSeq[(String, JsonNode)]): JsonObject = JsonObject.deduped(members ++ otherMembers)

  def :+(node: (String, JsonNode)): JsonObject = JsonObject.deduped(members :+ node)
}

object JsonArray extends JsonNodeParseFactory[IndexedSeq[JsonNode], JsonArray] {
  def apply(elements: JsonNode*): JsonArray = JsonArray(elements.toIndexedSeq)
  def apply(elements: Iterable[JsonNode]): JsonArray = JsonArray(elements.toIndexedSeq)

  val empty: JsonArray = JsonArray()

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): IndexedSeq[JsonNode] = {
    currentTokenOrAdvance(parser)
    requireToken(parser, JsonToken.START_ARRAY)
    parser.nextToken() // Advance past the START_ARRAY token

    val builder = Vector.newBuilder[JsonNode]

    while (!hasTokenOrAdvance(parser, JsonToken.END_ARRAY)) {
      builder += JsonNode.parse(parser, options)
    }

    builder.result
  }
}

final case class JsonArray(elements: IndexedSeq[JsonNode]) extends JsonContainerNode {
  override def size: Int = elements.size
  override def apply(index: Int): JsonNode = elements(index)
  override def get(index: Int): Option[JsonNode] = if (index < 0 || index >= elements.length) None else Some(elements(index))

  override def isJsonArray: Boolean = true
  override def isJsonObject: Boolean = false

  override def write(gen: JsonGenerator, options: JsonOptions): Unit = {
    gen.writeStartArray(size)
    elements.foreach{ _.write(gen, options) }
    gen.writeEndArray()
  }

  override def asToken: JsonToken = JsonToken.START_ARRAY
  override protected def atImpl(ptr: JsonPointer): Option[JsonNode] = get(ptr.getMatchingIndex)

  def ++(other: JsonArray): JsonArray = ++(other.elements)
  def ++(otherElements: IndexedSeq[JsonNode]): JsonArray = JsonArray(elements ++ otherElements)

  def :+(node: JsonNode): JsonArray = JsonArray(elements :+ node)
}

sealed abstract class JsonContainerNode extends JsonNode {
  override def isJsonValue: Boolean = false
  override def isJsonContainer: Boolean = true

  def apply(index: Int): JsonNode
  def get(index: Int): Option[JsonNode]
  def size: Int

  final def isEmpty: Boolean = size == 0
  final def isNotEmpty: Boolean = !isEmpty
}

object JsonNode extends JsonValueFactory with JsonNodeParseFactory[JsonNode, JsonNode] {
  def apply(node: JsonNode): JsonNode = node

  override protected def parseImpl(parser: JsonParser, options: JsonOptions): JsonNode = {
    val token: JsonToken = currentTokenOrAdvance(parser)

    token match {
      case JsonToken.VALUE_NULL => JsonNull
      case JsonToken.VALUE_TRUE => JsonTrue
      case JsonToken.VALUE_FALSE => JsonFalse
      case JsonToken.VALUE_STRING => JsonString(parser.getValueAsString)
      case JsonToken.VALUE_NUMBER_INT | JsonToken.VALUE_NUMBER_FLOAT =>
        parser.getNumberType match {
          case JsonParser.NumberType.BIG_DECIMAL | JsonParser.NumberType.DOUBLE | JsonParser.NumberType.FLOAT => JsonBigDecimal(parser.getDecimalValue)
          case JsonParser.NumberType.BIG_INTEGER => JsonBigInteger(parser.getBigIntegerValue)
          case JsonParser.NumberType.INT => JsonInt(parser.getIntValue)
          case JsonParser.NumberType.LONG => JsonLong(parser.getLongValue)
        }
      case JsonToken.START_ARRAY => JsonArray.parseNode(parser, options)
      case JsonToken.START_OBJECT => JsonObject.parseNode(parser, options)
      case JsonToken.END_ARRAY | JsonToken.END_OBJECT | JsonToken.FIELD_NAME | JsonToken.NOT_AVAILABLE | JsonToken.VALUE_EMBEDDED_OBJECT =>
        throw new IllegalStateException(s"Unexpected JsonToken: $token")
    }
  }
}

sealed abstract class JsonNode {
  final def at(jsonPointerExpression: String): Option[JsonNode] = at(JsonPointer.valueOf(jsonPointerExpression))

  final def at(ptr: JsonPointer): Option[JsonNode] = {
    if (ptr.matches()) return Some(this)

    atImpl(ptr) match {
      case None => None
      case Some((node: JsonNode)) => node.at(ptr.tail())
    }
  }

  def isJsonValue: Boolean
  def isJsonContainer: Boolean
  def isJsonArray: Boolean
  def isJsonObject: Boolean
  def isJsonNull: Boolean = false

  def asToken: JsonToken
  protected def atImpl(ptr: JsonPointer): Option[JsonNode]
  final def write(gen: JsonGenerator): Unit = write(gen, JsonOptions.default)
  def write(gen: JsonGenerator, options: JsonOptions): Unit

  final override def toString(): String = toCompactJson()

  final def toCompactJson(): String = toJson(JsonOptions.default)
  final def toPrettyJson(): String = toJson(JsonOptions.pretty)
  final def toPrettyJsonWithoutNulls(): String = toJson(JsonOptions.prettyWithoutNulls)
  final def toJsonWithoutNulls(): String = toJson(JsonOptions.defaultWithoutNulls)

  final def toJson(pretty: Boolean): String = {
    toJson(if (pretty) JsonOptions.pretty else JsonOptions.default)
  }

  final def toJson(options: JsonOptions): String = {
    val sw: StringWriter = new StringWriter()
    val gen: JsonGenerator = Jackson.defaultJsonFactory.createGenerator(sw)
    if (options.prettyFormat) gen.setPrettyPrinter(Jackson.defaultJsonPrettyPrinter)
    write(gen, options)
    gen.close()
    sw.toString
  }

  final def toJsonNode(options: JsonOptions): JsonNode = {
    val gen: JsonNodeGenerator = new JsonNodeGenerator(options)
    write(gen, options)
    gen.close()
    gen.result
  }
}
