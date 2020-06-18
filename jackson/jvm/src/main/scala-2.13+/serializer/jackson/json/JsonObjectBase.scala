package serializer.jackson.json

import scala.collection.immutable.ArraySeq

trait JsonObjectBase {
  final protected def unsafeWrapArray[T](array: Array[T]): IndexedSeq[T] = ArraySeq.unsafeWrapArray(array)
}