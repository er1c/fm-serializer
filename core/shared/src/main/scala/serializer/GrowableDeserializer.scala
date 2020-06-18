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
package serializer

import scala.collection.generic.Growable

final class GrowableDeserializer[Elem,Col <: Growable[Elem]](newInstance: => Col)(implicit elemDeser: Deserializer[Elem]) extends CollectionDeserializerBase[Col] {
  def defaultValue: Col = newInstance

  protected def readCollection(input: CollectionInput): Col = {
    val col: Col = newInstance
    
    while (input.hasAnotherElement) {
      col += elemDeser.deserializeNested(input)
    }
    
    col
  }
}