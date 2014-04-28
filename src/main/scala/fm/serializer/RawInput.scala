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
 * RAW Input
 * 
 * See documentation for Input/Output traits
 */
trait RawInput {  
  // Basic Types
  def readRawBool(): Boolean
  def readRawFloat(): Float
  def readRawDouble(): Double
  def readRawString(): String
  
  // Bytes
  def readRawByteArray(): Array[Byte]
  
  // Ints  
  def readRawInt(): Int
  def readRawUnsignedInt(): Int
  def readRawSignedInt(): Int
  def readRawFixedInt(): Int
  
  // Longs
  def readRawLong(): Long
  def readRawUnsignedLong(): Long
  def readRawSignedLong(): Long
  def readRawFixedLong(): Long
    
  // Objects
  def readRawObject[T](f: FieldInput => T): T
  
  // Collections
  def readRawCollection[T](f: CollectionInput => T): T
}