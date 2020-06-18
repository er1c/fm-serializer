package serializer

trait PackageBase {
  private[serializer] type IterableOnce[+A] = collection.GenTraversableOnce[A]

  private[serializer] implicit class IterableOnceOps[A](private val self: IterableOnce[A]) {
    def iterator: Iterator[A] = self.toIterator
  }

  private[serializer] type Growable[-A] = scala.collection.generic.Growable[A]

  private[serializer] val JavaConverters = scala.collection.JavaConverters
}