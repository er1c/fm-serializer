/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package serializer.commontypes

import java.io.File
import java.util.{Calendar, Date}
import serializer._

/**
 * These are implicit serializers/deserializers for common types that do not require the use of a macro to generate.
 * 
 * Common types that DO require a macro are embedded into the makeSerializer/makeDeserializer via MacroHelpers.tryCommonType()
 * since we can't call macros from here without creating a separate compilation package.
 */
trait ImplicitsBase {

  implicit val javaFile: MappedSimpleSerializer[String,File] = Primitive.string.map(_.toString, new File(_), null)
  implicit val javaDate: MappedSimpleSerializer[Long,Date] = Primitive.long.map(_.getTime, new Date(_), null)
  implicit val javaCalendar: MappedSimpleSerializer[Long,Calendar] = Primitive.long.map(_.getTime.getTime, toCalendar, null)
  
  private def toCalendar(millis: Long): Calendar = {
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTimeInMillis(millis)
    calendar
  }
  
  //
  // javax.xml stuff
  //
  import javax.xml.datatype.{DatatypeFactory, Duration, XMLGregorianCalendar}
  
  // The JavaDocs don't specify if this is thread-safe or not so let's play it safe
  private[this] val xmlDatatypeFactory: ThreadLocal[DatatypeFactory] = new ThreadLocal[DatatypeFactory] { override protected def initialValue(): DatatypeFactory = DatatypeFactory.newInstance() }
  
  implicit val xmlGregorianCalendar: MappedSimpleSerializer[String, XMLGregorianCalendar] = Primitive.string.map(_.toXMLFormat, xmlDatatypeFactory.get.newXMLGregorianCalendar(_), null)
  implicit val xmlDuration: MappedSimpleSerializer[String, Duration] = Primitive.string.map(_.toString, xmlDatatypeFactory.get.newDuration(_), null)

  //
  // java.time
  //
  implicit val javaLocalDate: MappedSimpleSerializer[String, java.time.LocalDate] = Primitive.string.map(_.toString, java.time.LocalDate.parse(_), null)
  implicit val javaLocalDateTime: MappedSimpleSerializer[String, java.time.LocalDateTime] = Primitive.string.map(_.toString, java.time.LocalDateTime.parse(_), null)
}
