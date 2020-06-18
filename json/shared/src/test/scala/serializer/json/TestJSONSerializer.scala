package serializer.json

import java.io.StringReader
import serializer._
/**
 * Special case that tests the toBytes/fromBytes methods
 */

final class TestDefaultJSONBytes extends TestSerializer[Array[Byte]] {
  def serialize[T](v: T)(implicit ser: Serializer[T]): Array[Byte] = JSON.toBytes[T](v)
  def deserialize[T](json: Array[Byte])(implicit deser: Deserializer[T]): T = JSON.fromBytes[T](json)
  def makeInput(json: Array[Byte]): JSONByteArrayInput = new JSONByteArrayInput(json, JSONDeserializerOptions.default)
}

final class TestDefaultJSONReader extends TestJSONSerializer {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromReader[T](new StringReader(json))
  def makeInput(json: String): JSONReaderInput = new JSONReaderInput(new StringReader(json), JSONDeserializerOptions.default)
}

final class TestMinimalJSONReader extends TestJSONSerializer {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toMinimalJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromReader[T](new StringReader(json))
  def makeInput(json: String): JSONReaderInput = new JSONReaderInput(new StringReader(json), JSONDeserializerOptions.default)
  override def ignoreNullRetainTest: Boolean = true
}

final class TestPrettyJSONReader extends TestPrettyJSON {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toPrettyJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromReader[T](new StringReader(json))
  def makeInput(json: String): JSONReaderInput = new JSONReaderInput(new StringReader(json), JSONDeserializerOptions.default)
}

final class TestDefaultJSONCharSequence extends TestJSONSerializer {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromJSON[T](json)
  def makeInput(json: String): JSONCharSequenceInput = new JSONCharSequenceInput(json, JSONDeserializerOptions.default)
}

final class TestMinimalJSONCharSequence extends TestJSONSerializer {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toMinimalJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromJSON[T](json)
  def makeInput(json: String): JSONCharSequenceInput = new JSONCharSequenceInput(json, JSONDeserializerOptions.default)
  override def ignoreNullRetainTest: Boolean = true
}

final class TestPrettyJSONCharSequence extends TestPrettyJSON {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String = JSON.toPrettyJSON[T](v)
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T = JSON.fromJSON[T](json)
  def makeInput(json: String): JSONCharSequenceInput = new JSONCharSequenceInput(json, JSONDeserializerOptions.default)
}

abstract class TestPrettyJSON extends TestJSONSerializer {
  override protected def stringMapJSON: String =
    """
      |{
      |  "foo": 123,
      |  "bar": 321
      |}""".stripMargin.trim

  override protected def intMapJSON: String =
    """
      |[
      |  {
      |    "_1": 123,
      |    "_2": "foo"
      |  },
      |  {
      |    "_1": 312,
      |    "_2": "bar"
      |  }
      |]""".stripMargin.trim

  override protected def alternateFieldNameJSON: String =
    """
      |{
      |  "type": "the_type_field",
      |  "foo": 123
      |}""".stripMargin.trim
}

// JSON-specific common tests
abstract class TestJSONSerializer extends TestSerializer[String] {
  def serialize[T](v: T)(implicit ser: Serializer[T]): String
  def deserialize[T](json: String)(implicit deser: Deserializer[T]): T

  protected def allowsUnquotedStringValues: Boolean = true

  protected def stringMapJSON: String = """{"foo":123,"bar":321}"""
  protected def intMapJSON: String = """[{"_1":123,"_2":"foo"},{"_1":312,"_2":"bar"}]"""
  protected def alternateFieldNameJSON: String = """{"type":"the_type_field","foo":123}"""

  test("String Map - Treated as object") {
    val map: Map[String,Int] = Map("foo" -> 123, "bar" -> 321)
    val json: String = serialize(map)

    json should equal (stringMapJSON)

    deserialize[Map[String,Int]](json) should equal (map)
  }

  test("Int Map - Treated as array of tuples") {
    val map: Map[Int,String] = Map(123 -> "foo", 312 -> "bar")
    val json: String = serialize(map)

    json should equal (intMapJSON)

    deserialize[Map[Int,String]](json) should equal (map)
  }

  case class Unquoted(name: String, int: Int, long: Long)

  test("Unquoted Field Names") {
    deserialize[Unquoted]("""{name:null,int:123,long:123123123123123}""") should equal(Unquoted(null, 123, 123123123123123L))
    deserialize[Unquoted]("""{name:"nullnot",int:123,long:123123123123123}""") should equal(Unquoted("nullnot", 123, 123123123123123L))
    deserialize[Unquoted]("""{name:"foo",int:123,long:123123123123123}""") should equal(Unquoted("foo", 123, 123123123123123L))
    deserialize[Unquoted]("""{name:"foo",int:"123",long:"123123123123123"}""") should equal(Unquoted("foo", 123, 123123123123123L))
  }

  if (allowsUnquotedStringValues) {
    test("Unquoted Field Names and Values") {
      deserialize[Unquoted]("""{name:null,int:123,long:123123123123123}""") should equal(Unquoted(null, 123, 123123123123123L))
      deserialize[Unquoted]("""{name:nullnot,int:123,long:123123123123123}""") should equal(Unquoted("nullnot", 123, 123123123123123L))
      deserialize[Unquoted]("""{name:foo,int:123,long:123123123123123}""") should equal(Unquoted("foo", 123, 123123123123123L))
      deserialize[Unquoted]("""{name:foo,int:"123",long:"123123123123123"}""") should equal(Unquoted("foo", 123, 123123123123123L))
    }
  }

  case class AlternateName(@Field("type") tpe: String, @Field foo: Int)

  test("Alternate Field Name") {
    val instance: AlternateName = AlternateName("the_type_field", 123)
    val json: String = alternateFieldNameJSON

    serialize(instance) should equal (json)
    deserialize[AlternateName](json) should equal (instance)
  }
}