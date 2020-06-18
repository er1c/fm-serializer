package serializer.jackson.json

trait JsonObjectBase {
  // TODO: Is there a 2.11/2.12 unsafeWrapArray?
  final protected def unsafeWrapArray[T](array: Array[T]): IndexedSeq[T] = array.toIndexedSeq
}