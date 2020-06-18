package serializer

trait PackageBase {
  private[serializer] type Growable[-A] = scala.collection.mutable.Growable[A]

  private[serializer] val JavaConverters = scala.jdk.CollectionConverters
}
