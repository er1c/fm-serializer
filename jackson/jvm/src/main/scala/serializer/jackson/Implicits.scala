package serializer.jackson

import com.fasterxml.jackson.core.{JsonFactory, PrettyPrinter}

object Implicits extends Implicits {

}

trait Implicits {
  implicit def defaultJsonFactory: JsonFactory = Jackson.defaultJsonFactory
  implicit def defaultJsonPrettyPrinter: PrettyPrinter = Jackson.defaultJsonPrettyPrinter
}