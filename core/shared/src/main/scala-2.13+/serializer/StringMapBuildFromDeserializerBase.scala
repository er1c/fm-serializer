package serializer

trait StringMapBuildFromDeserializerBase  {
  protected def buildFromVector[V]: collection.BuildFrom[_,(String,V),Vector[(String, V)]] =
    Vector.empty[(String,V)].iterableFactory
}