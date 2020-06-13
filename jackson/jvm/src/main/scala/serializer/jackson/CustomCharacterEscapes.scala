package serializer.jackson

import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.{CharacterEscapes, SerializedString}

/**
 * The JSON Spec seems to say '/' should be escaped however Jackson does not do this by default so let's add this
 * so we can more easily compare serializer.json to fm.serializer.jackson
 */
final class CustomCharacterEscapes extends CharacterEscapes {
  private val asciiEscapes: Array[Int] = CharacterEscapes.standardAsciiEscapesForJSON()
  asciiEscapes('/') = CharacterEscapes.ESCAPE_CUSTOM

  private val customEscape: SerializedString = new SerializedString("\\/")

  def getEscapeCodesForAscii(): Array[Int] = asciiEscapes
  def getEscapeSequence(i: Int): SerializableString = if (i == ('/': Int)) customEscape else null
}