package serializer

/**
 * Usage Pattern:
 *
 * {{{
 * import serializer.{SerializableCompanion, SimpleSerializer}
 * 
 * object Foo extends SerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 * }}
 *
 * final case class Foo(bar: String) extends SerializableInstance[Foo] {
 *   protected def companion: SerializableCompanion[Foo] = Foo
 * }}}
 */
trait SerializableCompanion[A] {
  protected def makeSerializer[T](): SimpleObjectSerializer[T] = macro Macros.makeSimpleObjectSerializer[T]
  
  protected val serializer: SimpleSerializer[A]
}

/**
 * Usage Pattern:
 *
 * {{{
 * import serializer.{SerializableCompanion, SerializableInstance, SimpleSerializer}
 * 
 * object Foo extends SerializableCompanion[Foo] {
 *   protected val serializer: SimpleSerializer[Foo] = makeSerializer[Foo]
 * }
 * 
 * final case class Foo(bar: String) extends SerializableInstance[Foo] {
 *   protected def companion: SerializableCompanion[Foo] = Foo
 * }
 * }}}
 */
trait SerializableInstance[A] { self: A =>
  type CompanionType <: SerializableCompanion[A]
  protected def companion: CompanionType
}
