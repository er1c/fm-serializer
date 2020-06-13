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

trait JsonValueFactory {
  def apply(value: Null): JsonNull.type = JsonNull
  def apply(value: String): JsonString = JsonString(value)
  def apply(value: Boolean): JsonBoolean = JsonBoolean(value)
  def apply(value: Int): JsonInt = JsonInt(value)
  def apply(value: Long): JsonLong = JsonLong(value)
  def apply(value: Float): JsonFloat = JsonFloat(value)
  def apply(value: Double): JsonDouble = JsonDouble(value)
  def apply(value: BigDecimal): JsonBigDecimal = JsonBigDecimal(value)
  def apply(value: BigInteger): JsonBigInteger = JsonBigInteger(value)
}
