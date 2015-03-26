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
package fm.serializer.json

import fm.serializer.{Deserializer, Serializer}
import java.io.{ByteArrayInputStream, InputStreamReader, OutputStreamWriter, Reader, StringReader}
import java.nio.charset.StandardCharsets.UTF_8

object JSON {
  private[this] val defaultJSONOutput: ThreadLocal[JSONOutput] = new ThreadLocal[JSONOutput]{
    override protected def initialValue: JSONOutput = new JSONOutput()
  }
  
  private[this] val minimalJSONOutput: ThreadLocal[JSONOutput] = new ThreadLocal[JSONOutput]{
    override protected def initialValue: JSONOutput = new JSONOutput(outputNulls = false, outputFalse = false)
  }
  
  private[this] val prettyJSONOutput: ThreadLocal[JSONOutput] = new ThreadLocal[JSONOutput]{
    override protected def initialValue: JSONOutput = new JSONOutput(prettyFormat = true)
  }
  
  def toJSON[@specialized T](v: T)(implicit serializer: Serializer[T]): String = {
    toJSON(v, defaultJSONOutput.get)
  }
  
  def toMinimalJSON[@specialized T](v: T)(implicit serializer: Serializer[T]): String = {
    toJSON(v, minimalJSONOutput.get)
  }
  
  def toPrettyJSON[@specialized T](v: T)(implicit serializer: Serializer[T]): String = {
    toJSON(v, prettyJSONOutput.get)
  }

  def toJSON[@specialized T](v: T, out: JSONOutput)(implicit serializer: Serializer[T]): String = {
    val bytes: Array[Byte] = toBytes[T](v, out)(serializer)
    new String(bytes, UTF_8)
  }
  
  def toBytes[@specialized T](v: T)(implicit serializer: Serializer[T]): Array[Byte] = {
    toBytes(v, defaultJSONOutput.get)
  }
  
  def toBytes[@specialized T](v: T, out: JSONOutput)(implicit serializer: Serializer[T]): Array[Byte] = {
    serializer.serializeRaw(out, v)
    val ret: Array[Byte] = out.toByteArray
    out.reset()
    ret
  }
  
  def fromJSON[@specialized T](json: String)(implicit deserializer: Deserializer[T]): T = {
    deserializer.deserializeRaw(new JSONCharSequenceInput(json))
  }
  
  def fromBytes[@specialized T](bytes: Array[Byte])(implicit deserializer: Deserializer[T]): T = {
    deserializer.deserializeRaw(new JSONByteArrayInput(bytes))
  } 
  
  def fromReader[@specialized T](reader: Reader)(implicit deserializer: Deserializer[T]): T = {
    deserializer.deserializeRaw(new JSONReaderInput(reader))
  }
}