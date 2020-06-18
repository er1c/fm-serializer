Scala Serializer
================

[![Build Status](https://travis-ci.org/er1c/scala-serializer.svg?branch=main)](https://travis-ci.org/er1c/scala-serializer)


Scala macros that generate serializers/deserializers for **case classes**, **POJOs**, **Scala Collections**, **Java Collections**, etc for multiple serializer/deserializer formats.

Features
--------

* **FAST** (TODO: Publish modified jvm-serializers code and results)
* Automatic generation of serializers/deserializers at compile time (via Scala Macros)
* No Boilerplate
* Support for **case classes**, **POJOs**, **Scala Collections**, **Java Collections**, etc...
* Scala.js 1.0
* Scala 2.11/2.12/2.13
* Java 8/11/14
* Write custom type Serializers/Deserializers

Formats
-------

* Protocol Buffers
* JSON (Scala implementation that supports `Array[Byte]` and Json `String`s)
* Jackson Json (includes scala Jackson Json library for manipulating Jackson's JsonObject/JsonNode/etc)
* BSON using MongoDB library
* YAML (coming soon)

## Acknowledgments

Forked from [Frugal Mechanic Serializer](https://github.com/frugalmechanic/fm-serializer)

Key changes:

* Splits serializer codecs into separate importable projects with separate dependencies
* Minimize external dependencies
* Scala 2.13 Support
* Scala.js 1.0 Support

Usage
-----

    val serializerVersion = "1.0.0-M1"
    libraryDependencies ++= Seq(
      "io.github.er1c" %% "scala-serializer-core" % serializerVersion,
      "io.github.er1c" %% "scala-serializer-commontypes" % serializerVersion,
      "io.github.er1c" %% "scala-serializer-json" % serializerVersion,
      "io.github.er1c" %% "scala-serializer-jackson" % serializerVersion,
      "io.github.er1c" %% "scala-serializer-bson" % serializerVersion,
    )

**Protocol Buffers**

    import serializer.protobuf.Protobuf
    case class Hello(name: String, list: List[Int])
    val hello = Hello("World", List(1,2,3,4,5))

    val bytes: Array[Byte] = Protobuf.toBytes(hello)
    val hello2: Hello = Protobuf.fromBytes[Hello](bytes)

    require(hello == hello2)

**JSON**

    import serializer.json.JSON
    case class Hello(name: String, list: List[Int])
    val hello = Hello("World", List(1,2,3,4,5))

    val json: String = JSON.toJSON(hello)
    val hello2: Hello = JSON.fromJSON[Hello](json)

    require(hello == hello2)

**BSON**

    import serializer.bson.BSON
    case class Hello(name: String, list: List[Int])
    val hello = Hello("World", List(1,2,3,4,5))

    val bsonDoc: BsonDocument = BSON.toBsonDocument(hello)
    val hello2: Hello = BSON.fromBsonDocument[Hello](bsonDoc)

    require(hello == hello2)

**Custom Serializer**

    import java.util.Date
    import serializer.{MappedSimpleSerializer, Primitive}
    implicit val javaDate: MappedSimpleSerializer[Long,Date] = Primitive.long.map(_.getTime, new Date(_), null)

More complex examples are in [TestSerializerBase.scala](https://github.com/er1c/scala-serializer/blob/main/core/shared/src/test/scala/serializer/TestSerializerBase.scala)


License
-------

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
