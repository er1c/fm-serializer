package serializer

import scala.collection.JavaConverters._

// JVM-specific common tests
trait TestSerializer[BYTES] extends TestSerializerBase[BYTES] {

  //===============================================================================================
  // Java Bean Container
  //===============================================================================================

  test("FooJavaBeanContainer") {
    val container: FooJavaBeanContainer = new FooJavaBeanContainer()
    container.setChildren(new java.util.ArrayList())

    val bytes: BYTES = serialize(container)
    val container2: FooJavaBeanContainer = deserialize[FooJavaBeanContainer](bytes)

    container shouldBe container2
  }

  //===============================================================================================
  // Java Beans
  //===============================================================================================
  test("FooJavaBean") {
    val foo: FooJavaBean = new FooJavaBean()
    foo.setName("Hello World")
    foo.setNumber(123)
    foo.setBool(true)
    foo.setFooEnum(FooEnum.Bar)
    foo.setChildren(Vector({
      val child: FooJavaBean = new FooJavaBean()
      child.setName("Hello World Child")
      child
    }).asJava)
    foo.setList(Vector("aa", "bb", "cc").asJava)
    foo.getListWithoutSetter().addAll(Vector("One", "Two", "Three").asJava)
    foo.setIgnoredField1("ignored1")
    foo.setIgnoredField2("ignored2")
    foo.setIgnoredField4("ignored4")
    foo.setShadowedInterfaceMethod("not transient")

    val bytes: BYTES = serialize(foo)
    val foo2: FooJavaBean = deserialize[FooJavaBean](bytes)

    foo2.getName should equal (foo.getName)
    foo2.getNumber should equal (foo.getNumber)
    foo2.isBool should equal (foo.isBool)
    foo2.getFooEnum should equal (foo.getFooEnum)
    foo2.getChildren.get(0).getName() should equal(foo.getChildren.get(0).getName())
    foo2.getList.asScala should equal (foo.getList.asScala)
    foo2.getListWithoutSetter.asScala should equal (foo.getListWithoutSetter.asScala)
    foo2.getIgnoredField1 should equal (null)
    foo2.getIgnoredField2 should equal (null)
    foo2.getIgnoredField4 should equal (null)

    foo2.getShadowedInterfaceMethod should equal("not transient")

  }

  //===============================================================================================
  // Java Beans Immutable
  //===============================================================================================
  test("FooJavaBeanImmutable") {
    val foo: FooJavaBeanImmutable = new FooJavaBeanImmutable("Hello World", 123, true, FooEnum.Bar, Vector("aa", "bb", "cc").asJava)

    val bytes: BYTES = serialize(foo)
    val foo2: FooJavaBeanImmutable = deserialize[FooJavaBeanImmutable](bytes)

    foo2.getName should equal (foo.getName)
    foo2.getNumber should equal (foo.getNumber)
    foo2.isBool should equal (foo.isBool)
    foo2.getFooEnum should equal (foo.getFooEnum)
    foo2.getList.asScala should equal (foo.getList.asScala)
  }
}