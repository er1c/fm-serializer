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
package fm.serializer

/**
 * A combined Serializer/Deserializer that works on the same type
 */
trait SimpleSerializer[@specialized A] extends Serializer[A] with Deserializer[A] {
  final def map[B](ser: B => A, deser: A => B): MappedSimpleSerializer[A,B] = {
    map[B](ser, deser, deser(defaultValue))
  }
  
  final def map[B](ser: B => A, deser: A => B, default: => B): MappedSimpleSerializer[A,B] = {
    new MappedSimpleSerializer[A,B](this, new Mapper[A,B]{
      final def defaultValue: B = default
      final def serialize(obj: B): A = ser(obj)
      final def deserialize(value: A): B = deser(value)
    })
  }
  
  final def map[B](default: => B)(ser: B => A)(deser: A => B): MappedSimpleSerializer[A,B] = map(ser, deser, default)
}