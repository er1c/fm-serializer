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

import java.io.Reader
import java.lang.{StringBuilder => JavaStringBuilder}
import fm.serializer.{CollectionInput, FieldInput, NestedInput, Input}
import fm.serializer.base64.Base64

abstract class JSONInput(options: JSONOptions) extends Input {
  def allowStringMap: Boolean = true
  
  private[this] var inObject: Boolean = false
  private[this] var inArray: Boolean = false
  private[this] var isFirst: Boolean = false
  
  //
  // HELPERS
  //
  
  /** Peek at the next character without consuming it */
  protected def peek: Int
  
  /** Is there a next character?  (i.e. is peek valid?) */
  protected def hasNext: Boolean = -1 != peek
  
  /** Return peek and advance to the next character */
  protected def next: Char
  
  /** Skip any whitespace chars that can appear between JSON tokens */
  private def skipWhitespace(): Unit = {
    while (hasNext && Character.isWhitespace(peek)) next
  }
  
  /** Read contiguous chars that make up non-string types (e.g. 123.456, false, true, null) that can be optionally quoted */
  private def readOptionallyQuotedContiguousChars(): String = {
    skipWhitespace()
    
    if (peek == '"') {
      next // Eat the leading quote
      val res: String = readContiguousChars()
      expectNextChar('"') // We expect a trailing quote
      res
    } else {
      readContiguousChars()
    }
  }
  
  /** Read contiguous chars that make up non-string types (e.g. 123.456, false, true, null) */
  private def readContiguousChars(): String = {
    skipWhitespace()
    require(isValidStartOfContiguousChars(), s"Invalid Start of Contiguous Chars (e.g. for a number, boolean, or null) ${peek.toChar}")
    val sb: JavaStringBuilder = new JavaStringBuilder()
    while (!isEndOfContiguousChars()) sb.append(next)
    val res: String = sb.toString
    require(res.length > 0, "Expected a non-empty string")
    res
  }
  
  private def isValidStartOfContiguousChars(): Boolean = {
    peek match {
      case '{' | '}' => false
      case '[' | ']' => false
      case '"'       => false
      case _         => true
    }
  }
  
  /** Is the current peek character the end of the field? */
  private def isEndOfContiguousChars(): Boolean = {
    peek match {
      case '}' => true
      case ']' => true
      case ',' => true
      case ':' => true
      case '"' => true
      case _   => Character.isWhitespace(peek)
    }
  }
  
  def expectNextChar(ch: Int): Unit = {
    skipWhitespace()
    val n: Char = next
    require(n == ch, s"Expected next char to be: ${ch.toChar} but got $n")
  }
  
  def nextValueIsNull: Boolean = {
    skipWhitespace()
    if (peek == 'n') {
      val res: String = readContiguousChars()
      require(res == "null", s"Expected to read null but got: $res")
      true
    } else {
      false
    }
  }
  
  //
  // FIELD Input
  //
  def readFieldNumber(nameToNumMap: Map[String, Int]): Int = {
    val name: String = readFieldName()
    
    if (null == name) return 0
    
    try {
      // Exact name match
      nameToNumMap(name)
    } catch {
      case _: NoSuchElementException =>
        // TODO: possibly require that the map be pre-populated with the lower case versions so we don't have to search through it
        val lowerName: String = name.toLowerCase
        if (nameToNumMap.contains(lowerName)) nameToNumMap(lowerName)
        else nameToNumMap.find{ case (n,i) => n.toLowerCase == lowerName }.map{ _._2 }.getOrElse(-1)
    }
  }
  
  def readFieldName(): String = {
    handleFieldComma()
    skipWhitespace()
    
    // Check for End Of Object
    if (peek == '}') return null
    
    // Otherwise we expect a field name
    val name: String = readRawString()
    
    // Followed by optional whitespace
    skipWhitespace()
    
    // Followed by a colon
    require(next == ':', "Expected : character after the field name")
    
    name
  }
  
  def skipUnknownField(): Unit = {
    skipWhitespace()
    peek match {
      case '"' => readRawString()
      case '{' => readRawObject(skipRawObjectFun)
      case '[' => readRawCollection(skipRawCollectionFun)
      case _   => readContiguousChars()
    }
  }
  
  // This avoids the object creations in skipUnknownField()
  private[this] val skipRawObjectFun: Function1[FieldInput, AnyRef] = new Function1[FieldInput, AnyRef] {
    def apply(in: FieldInput): AnyRef = {
      while(in.readFieldNumber(Map.empty) != 0) in.skipUnknownField()
      null
    }
  }
  
  // This avoids the object creations in skipUnknownField()
  private[this] val skipRawCollectionFun: Function1[CollectionInput, AnyRef] = new Function1[CollectionInput, AnyRef] {
    def apply(in: CollectionInput): AnyRef = {
      while(in.hasAnotherElement) { handleCollectionComma(); skipUnknownField()};
      null
    }
  }
  
  @inline private def withCommas[T](isObject: Boolean = false, isArray: Boolean = false)(f: => T): T = {
    assert(isObject ^ isArray)
    
    val prevInObject: Boolean = inObject
    val prevInArray: Boolean = inArray
    val prevIsFirst: Boolean = isFirst
    
    inObject = isObject
    inArray = isArray
    isFirst = true
    
    val res: T = f
    
    inObject = prevInObject
    inArray = prevInArray
    isFirst = prevIsFirst
    
    res
  }
  
  def handleFieldComma(): Unit = handleCommaImpl(inObject, '}')
  def handleCollectionComma(): Unit = handleCommaImpl(inArray, ']')
  
  private def handleCommaImpl(cond: Boolean, closingChar: Char): Unit = {
    if (cond) {
      if (isFirst) isFirst = false // Should be no comma
      else {
        skipWhitespace()
        if (peek == ',') next // Skip over the comma
        else require(peek == closingChar, s"Expected either a comma or a $closingChar but got: ${peek.toChar}")
      }
    }
  }
  
  //
  // COLLECTION Input
  //
  def hasAnotherElement: Boolean = {
    skipWhitespace()
    peek != ']'
  }
  
  //
  // RAW Input
  //
  
  // Basic Types
  def readRawBool(): Boolean  = readOptionallyQuotedContiguousChars().toBoolean
  def readRawFloat(): Float   = readOptionallyQuotedContiguousChars().toFloat
  def readRawDouble(): Double = readOptionallyQuotedContiguousChars().toDouble
  
  def readRawString(): String = {
    skipWhitespace()
    
    // If we don't start with a quote then it should be a null value (or an unquoted number/string)
    if (peek != '"') {
      val res: String = readContiguousChars() match {
        case "null" => null
        case s => s
      }
      
      return if (options.internStrings) res.intern() else res
    }
    
    next // Skip over the leading "
    
    // Empty string special case
    if (peek == '"') {
      next // skip over the ending "
      return ""
    }
    
    val sb: JavaStringBuilder = new JavaStringBuilder()
    
    var done = false
    while (!done) {
      val ch: Char = next
      
      if (ch == '"') {
        // End Of String
        done = true
      } else if (ch == '\\') {
        // Escaped character
        next match {
          case '"'  => sb.append('"')
          case '\\' => sb.append('\\')
          case '/'  => sb.append('/')
          case 'b'  => sb.append('\b')
          case 'f'  => sb.append('\f')
          case 'n'  => sb.append('\n')
          case 'r'  => sb.append('\r')
          case 't'  => sb.append('\t')
          case 'u'  => 
            // Next 4 characters are HEX representing the unicode character number
            val unicode: Char = Integer.parseInt(""+next+next+next+next, 16).toChar
            sb.append(unicode)
        }
      } else {
        // Normal character
        sb.append(ch)
      }
    }
    
    val res: String = sb.toString
    if (options.internStrings) res.intern() else res
  }
  
  // Bytes
  def readRawByteArray(): Array[Byte] = {
    val s: String = readRawString()
    if (null == s) null else Base64.decode(s)
  }
  
  // Ints  
  def readRawInt(): Int = readOptionallyQuotedContiguousChars().toInt
  def readRawUnsignedInt(): Int = readRawInt()
  def readRawSignedInt(): Int = readRawInt()
  def readRawFixedInt(): Int = readRawInt()
  
  // Longs
  def readRawLong(): Long = readOptionallyQuotedContiguousChars().toLong
  def readRawUnsignedLong(): Long = readRawLong()
  def readRawSignedLong(): Long = readRawLong()
  def readRawFixedLong(): Long = readRawLong()
    
  // Objects
  def readRawObject[T](f: FieldInput => T): T = {
    if(nextValueIsNull) return null.asInstanceOf[T]
    
    expectNextChar('{')
    val res: T = withCommas(isObject = true){ f(this) }
    expectNextChar('}')
    res
  }
  
  // Collections
  def readRawCollection[T](f: CollectionInput => T): T = {
    if (nextValueIsNull) return null.asInstanceOf[T]
    
    expectNextChar('[')
    val res: T = withCommas(isArray = true){ f(this) }
    expectNextChar(']')
    res
  }
  
  //
  // NESTED Input
  //
  
  // Basic Types
  def readNestedBool(): Boolean = {
    handleCollectionComma()
    readRawBool()
  }
  
  def readNestedFloat(): Float = {
    handleCollectionComma()
    readRawFloat()
  }
  
  def readNestedDouble(): Double = {
    handleCollectionComma()
    readRawDouble()
  }
  
  def readNestedString(): String = {
    handleCollectionComma()
    readRawString()
  }
  
  // Bytes
  def readNestedByteArray(): Array[Byte] = {
    handleCollectionComma()
    readRawByteArray()
  }
  
  // Ints  
  def readNestedInt(): Int = {
    handleCollectionComma()
    readRawInt()
  }
  
  def readNestedUnsignedInt(): Int = readNestedInt()
  def readNestedSignedInt(): Int = readNestedInt()
  def readNestedFixedInt(): Int = readNestedInt()
  
  // Longs
  def readNestedLong(): Long = {
    handleCollectionComma()
    readRawLong()
  }
  
  def readNestedUnsignedLong(): Long = readNestedLong()
  def readNestedSignedLong(): Long = readNestedLong()
  def readNestedFixedLong(): Long = readNestedLong()
    
  // Objects
  def readNestedObject[T](f: FieldInput => T): T = {
    handleCollectionComma()
    readRawObject(f)
  }
  
  // Collections
  def readNestedCollection[T](f: CollectionInput => T): T = {
    handleCollectionComma()
    readRawCollection(f)
  }
}
