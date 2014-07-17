Frugal Mechanic Serializer
==========================

[![Build Status](https://travis-ci.org/frugalmechanic/fm-serializer.svg?branch=master)](https://travis-ci.org/frugalmechanic/fm-serializer)

This is our Scala macro-based serialization library.

Features
--------

* **FAST** (TODO: Publish modified jvm-serializers code and results)
* Automatic generation of serializers/deserializers at compile time (via Scala Macros)
* No Boilerplate
* Support for **case classes**, **POJOs**, **Scala Collections**, **Java Collections**, etc...

Formats
-------

* Modified Protocol Buffers (TODO: Document the modifications)
* JSON

Usage
-----

**Modified Protocol Buffers**

    import fm.serializer.protobuf.Protobuf
    case class Hello(name: String, list: List[Int])
    val hello = Hello("World", List(1,2,3,4,5))
    
    val bytes: Array[Byte] = Protobuf.toBytes(hello)
    val hello2: Hello = Protobuf.fromBytes[Hello](bytes)
    
    require(hello == hello2)

**JSON**

    import fm.serializer.json.JSON
    case class Hello(name: String, list: List[Int])
    val hello = Hello("World", List(1,2,3,4,5))

    val json: String = JSON.toJSON(hello)
    val hello2: Hello = JSON.fromJSON[Hello](json)

    require(hello == hello2)

Authors
-------

Tim Underwood (<a href="https://github.com/tpunder" rel="author">GitHub</a>, <a href="https://www.linkedin.com/in/tpunder" rel="author">LinkedIn</a>, <a href="https://twitter.com/tpunder" rel="author">Twitter</a>, <a href="https://plus.google.com/+TimUnderwood0" rel="author">Google Plus</a>)

Copyright
---------

Copyright [Frugal Mechanic](http://frugalmechanic.com)

License
-------

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)