/*
 * Copyright 2019 Frugal Mechanic (http://frugalmechanic.com)
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
package serializer.jackson.json

import java.math.{BigDecimal, BigInteger}

/**
 * Experimental implicits with the goal of making it easier to build up Json Objects/Arrays
 *
 * Subject to change
 */
object Implicits extends Implicits {

}

trait Implicits {
  implicit def toJsonNode(value: Null): JsonNull.type = JsonNull
  implicit def toJsonNode(value: String): JsonString = JsonString(value)
  implicit def toJsonNode(value: Boolean): JsonBoolean = JsonBoolean(value)
  implicit def toJsonNode(value: Int): JsonInt = JsonInt(value)
  implicit def toJsonNode(value: Long): JsonLong = JsonLong(value)
  implicit def toJsonNode(value: Float): JsonFloat = JsonFloat(value)
  implicit def toJsonNode(value: Double): JsonDouble = JsonDouble(value)
  implicit def toJsonNode(value: BigDecimal): JsonBigDecimal = JsonBigDecimal(value)
  implicit def toJsonNode(value: BigInteger): JsonBigInteger = JsonBigInteger(value)
}