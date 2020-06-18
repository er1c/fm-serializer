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

import com.fasterxml.jackson.core.json.JsonWriteContext
import com.fasterxml.jackson.core.{Base64Variant, JsonGenerator, JsonStreamContext, ObjectCodec, SerializableString, TreeNode, Version}
import java.io.{EOFException, InputStream}
import java.math.{BigDecimal, BigInteger}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

object JsonNodeGenerator {
  private case class JsonObjectBuilder(parent: JsonNodeBuilder, options: JsonOptions) extends JsonNodeBuilder {
    private val builder = Vector.newBuilder[(String, JsonNode)]
    private var currentFieldName: String = null
    def += (fieldName: String): Unit = currentFieldName = fieldName

    def += (node: JsonNode): Unit = {
      if (JsonObject.shouldIncludeField(currentFieldName, node, options)) builder += ((currentFieldName, node))
      currentFieldName = null
    }

    def result: JsonObject = JsonObject(builder.result)
  }

  private case class JsonArrayBuilder(parent: JsonNodeBuilder) extends JsonNodeBuilder {
    private val builder = Vector.newBuilder[JsonNode]

    def += (fieldName: String): Unit = throw new IllegalStateException("Unexpected JaonToken.FIELD_NAME")
    def += (node: JsonNode): Unit = builder += node
    def result: JsonArray = JsonArray(builder.result)
  }

  // Only used as a root JsonNode holder
  private case class JsonRootNodeBuilder() extends JsonNodeBuilder {
    private var node: JsonNode = null

    def += (fieldName: String): Unit = throw new IllegalStateException("Unexpected JaonToken.FIELD_NAME")

    def += (node: JsonNode): Unit = {
      require(null == this.node)
      this.node = node
    }

    def parent: JsonNodeBuilder = null // not applicable
    def result: JsonNode = node
  }

  private sealed abstract class JsonNodeBuilder {
    def parent: JsonNodeBuilder
    def result: JsonNode
    def += (fieldName: String): Unit
    def += (node: JsonNode): Unit
  }

  // Java8 => Java9 Implicit
  implicit class RichInputStream(val is: InputStream) extends AnyVal {
    def readNBytes(len: Int): Array[Byte] = {
      if (len < 0) throw new IllegalArgumentException("len < 0")

      val bytes = new Array[Byte](len)

      @tailrec
      def read(n: Int): Int = {
        val rd = is.read(bytes, len - n, n)

        if (rd == -1) len - n
        else if (rd < n) read(n - rd)
        else len
      }

      val actual: Int = read(len)

      if (actual < len) throw new EOFException("Unexpected end of input stream: " +
        "actual: %d, expected: %d" format(actual, len)
      )

      is.available() // triggers is.ensureOpen()

      bytes
    }
  }
}

/**
 * Produces a JsonNode
 */
final class JsonNodeGenerator(options: JsonOptions) extends JsonGenerator {
  def this() = this(JsonOptions.default)

  import JsonNodeGenerator._

  private[this] val rootBuilder: JsonRootNodeBuilder = JsonRootNodeBuilder()
  private[this] var currentBuilder: JsonNodeBuilder = rootBuilder
  private[this] var objectCodec: ObjectCodec = null

  def result: JsonNode = rootBuilder.result

  private def pop(): Unit = {
    if (null != currentBuilder) {
      val result: JsonNode = currentBuilder.result
      currentBuilder = currentBuilder.parent
      currentBuilder += result
    }
  }

  private def writeValue(node: JsonValue): Unit = currentBuilder += node

  override def setCodec(oc: ObjectCodec): JsonGenerator = {
    objectCodec = oc
    this
  }

  override def getCodec: ObjectCodec = objectCodec
  override def version(): Version = null

  override def enable(f: JsonGenerator.Feature): JsonGenerator = this // nop
  override def disable(f: JsonGenerator.Feature): JsonGenerator = this // nop
  override def isEnabled(f: JsonGenerator.Feature): Boolean = false

  override def getFeatureMask: Int = 0
  override def setFeatureMask(values: Int): JsonGenerator = this // nop
  override def useDefaultPrettyPrinter(): JsonGenerator = this // nop

  override def writeStartArray(): Unit = currentBuilder = new JsonArrayBuilder(currentBuilder)

  override def writeEndArray(): Unit = {
    currentBuilder match {
      case arr: JsonArrayBuilder => pop()
      case _ => throw new IllegalStateException("Unexpected JsonToken.END_ARRAY")
    }
  }

  override def writeStartObject(): Unit = currentBuilder = new JsonObjectBuilder(currentBuilder, options)

  override def writeEndObject(): Unit = {
    currentBuilder match {
      case arr: JsonObjectBuilder => pop()
      case _ => throw new IllegalStateException("Unexpected JsonToken.END_OBJECT")
    }
  }

  override def writeFieldName(name: String): Unit = currentBuilder += name
  override def writeFieldName(name: SerializableString): Unit = currentBuilder += name.getValue
  override def writeString(text: String): Unit = currentBuilder += JsonString(text)
  override def writeString(text: Array[Char], offset: Int, len: Int): Unit = writeString(new String(text, offset, len))
  override def writeString(text: SerializableString): Unit = writeString(text.getValue)
  override def writeRawUTF8String(text: Array[Byte], offset: Int, length: Int): Unit = writeString(new String(text, offset, length, StandardCharsets.UTF_8))
  override def writeUTF8String(text: Array[Byte], offset: Int, length: Int): Unit = writeString(new String(text, offset, length, StandardCharsets.UTF_8))

  override def writeRaw(text: String): Unit = throw new UnsupportedOperationException()
  override def writeRaw(text: String, offset: Int, len: Int): Unit = throw new UnsupportedOperationException()
  override def writeRaw(text: Array[Char], offset: Int, len: Int): Unit = throw new UnsupportedOperationException()
  override def writeRaw(c: Char): Unit = throw new UnsupportedOperationException()

  override def writeRawValue(text: String): Unit = throw new UnsupportedOperationException()
  override def writeRawValue(text: String, offset: Int, len: Int): Unit = throw new UnsupportedOperationException()
  override def writeRawValue(text: Array[Char], offset: Int, len: Int): Unit = throw new UnsupportedOperationException()

  override def writeBinary(bv: Base64Variant, data: Array[Byte], offset: Int, len: Int): Unit = {
    writeValue(JsonBinary(data, offset, len))
  }

  override def writeBinary(bv: Base64Variant, data: InputStream, dataLength: Int): Int = {
    //val bytes: Array[Byte]

    val bytes: Array[Byte] = new Array[Byte](dataLength)
    data.readNBytes(dataLength)
    writeValue(JsonBinary(bytes))
    bytes.length
  }

  override def writeNumber(v: Int): Unit = writeValue(JsonInt(v))
  override def writeNumber(v: Long): Unit = writeValue(JsonLong(v))
  override def writeNumber(v: BigInteger): Unit = writeValue(JsonBigInteger(v))
  override def writeNumber(v: Double): Unit = writeValue(JsonDouble(v))
  override def writeNumber(v: Float): Unit = writeValue(JsonFloat(v))
  override def writeNumber(v: java.math.BigDecimal): Unit = writeValue(JsonBigDecimal(v))

  override def writeNumber(encodedValue: String): Unit = {
    val node: JsonBigDecimal = new JsonBigDecimal(new BigDecimal(encodedValue))

    // Attempt to convert to a more optimal representation
    val updatedNode: JsonNumber =
      if (node.isInt) JsonInt(node.toInt)
      else if (node.isLong) JsonLong(node.toLong)
      else if (node.isBigInteger) JsonBigInteger(node.toBigInteger)
      else node

    writeValue(updatedNode)
  }

  override def writeBoolean(state: Boolean): Unit = writeValue(JsonBoolean(state))
  override def writeNull(): Unit = writeValue(JsonNull)

  override def writeObject(pojo: AnyRef): Unit = {
    if (null == pojo) writeNull()
    else if (null != objectCodec) objectCodec.writeValue(this, pojo)
    _writeSimpleObject(pojo)
  }

  override def writeTree(rootNode: TreeNode): Unit = {
    if (null == rootNode) writeNull()
    else if (null != objectCodec) objectCodec.writeTree(this, rootNode)
    else throw new IllegalStateException("No ObjectCodec defined")
  }

  override def getOutputContext: JsonStreamContext = JsonWriteContext.createRootContext(null) // dummy value

  override def flush(): Unit = {} // nop

  override def isClosed: Boolean = false
  override def close(): Unit = {}
}
