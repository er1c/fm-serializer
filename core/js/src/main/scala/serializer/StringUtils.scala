package serializer

import java.nio.charset.StandardCharsets.UTF_8

// TODO: There are scala bugs in this java port
// TODO: There are scala bugs in this java port
// TODO: There are scala bugs in this java port

/**
 * Scala conversions of some non-public helpers from java.lang.{Integer,Long}
 */
object StringUtils {
  /**
   * All possible chars for representing a number as a String
   */
  private[serializer] val Digits = Array(
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f', 'g', 'h',
    'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z'
  )

  private[serializer] val DigitTens: Array[Char] = Array(
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
  )

  private[serializer] val DigitOnes: Array[Char] = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
  )

  private[serializer] val IntMinValueBytes: Array[Byte]  = Int.MinValue.toString.getBytes(UTF_8)
  private[serializer] val LongMinValueBytes: Array[Byte] = Long.MinValue.toString.getBytes(UTF_8)

  /**
   * Write the characters that make up this integer into the buffer
   *
   * Note: index is the ending location in the buffer.  Bytes will be written backwards into the buffer.
   */
  def writeIntChars(value: Int, index: Int, buf: Array[Byte]): Unit = {
    if (value == Int.MinValue) System.arraycopy(IntMinValueBytes, 0, buf, index - IntMinValueBytes.length, IntMinValueBytes.length)
    else getIntChars(value, index, buf)
  }

  /**
   * Write the characters that make up this long into the buffer.
   *
   * Note: index is the ending location in the buffer.  Bytes will be written backwards into the buffer.
   */
  def writeLongChars(value: Long, index: Int, buf: Array[Byte]): Unit = {
    if (value == Long.MinValue) System.arraycopy(LongMinValueBytes, 0, buf, index - LongMinValueBytes.length, LongMinValueBytes.length)
    else getLongChars(value, index, buf)
  }

  /**
   * Places characters representing the integer i into the
   * character array buf. The characters are placed into
   * the buffer backwards starting with the least significant
   * digit at the specified index (exclusive), and working
   * backwards from there.
   *
   * Will fail if i == Integer.MIN_VALUE
   */
  private[serializer] def getIntChars(value: Int, index: Int, buf: Array[Byte]): Unit = {
    var i: Int = value
    var q: Int = 0
    var r: Int = 0
    var charPos: Int = index
    var sign: Int = 0

    if (i < 0) {
      sign = '-'
      i = -i
    }

    // Generate two digits per iteration
    while (i >= 65536) {
      q = i / 100
      // really: r = i - (q * 100);
      r = i - ((q << 6) + (q << 5) + (q << 2))
      i = q

      charPos -= 1
      buf(charPos) = DigitOnes(r).toByte

      charPos -= 1
      buf(charPos) = DigitTens(r).toByte
    }
    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);

    var keepLooping = true
    while (keepLooping) {
      q = (i * 52429) >>> (16 + 3)
      r = i - ((q << 3) + (q << 1)) // r = i-(q*10) ...

      charPos -= 1
      buf(charPos) = Digits(r).toByte

      i = q

      if (i == 0) {
        keepLooping = false
        return;
      }
    }

    if (sign != 0) {
      charPos -= 1
      buf(charPos) = sign.toByte
    }
  }

  private[serializer] val sizeTable: Array[Int] = Array(9, 99, 999, 9999, 99999, 999999,
                                                        9999999, 99999999, 999999999, Int.MaxValue)

  /**
   * Modified to work with negative numbers
   */
  def stringSize(i: Int): Int = {
    if (i < 0) stringSizeImpl(-i) + 1
    else       stringSizeImpl(i)
  }

  // Requires positive x
  private def stringSizeImpl(x: Int): Int = {
    var i: Int = 0
    while(x > sizeTable(i)) i += 1
    i
  }

  /**
   * Places characters representing the integer i into the
   * character array buf. The characters are placed into
   * the buffer backwards starting with the least significant
   * digit at the specified index (exclusive), and working
   * backwards from there.
   *
   * Will fail if i == Long.MIN_VALUE
   */
  def getLongChars(value: Long, index: Int, buf: Array[Byte]): Unit = {
    var i = value
    var q = 0L
    var r = 0
    var charPos = index
    var sign = 0.toChar

    if (i < 0) {
      sign = '-'
      i = -i
    }

    // Get 2 digits/iteration using longs until quotient fits into an int
    while (i > Int.MaxValue) {
      q = i / 100
      r = (i - ((q << 6) + (q << 5) + (q << 2))).toInt
      i = q

      charPos -= 1
      buf(charPos) = DigitOnes(r).toByte

      charPos -= 1
      buf(charPos) = DigitOnes(r).toByte
    }
    // Get 2 digits/iteration using ints
    var q2 = 0
    var i2 = i.toInt

    while (i2 >= 65536) {
      q2 = i2 / 100
      // really: r = i2 - (q * 100);
      r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2))
      i2 = q2

      charPos -= 1
      buf(charPos) = DigitOnes(r).toByte
      charPos -= 1
      buf(charPos) = DigitTens(r).toByte
    }
    // assert(i2 <= 65536, i2);

    var keepLooping = true
    while (keepLooping) {
      q2 = (i2 * 52429) >>> (16 + 3)
      r = i2 - ((q2 << 3) + (q2 << 1)) // r = i2-(q2*10) ...

      charPos -= 1
      buf(charPos) = Digits(r).toByte

      i2 = q2

      if (i2 == 0) {
        keepLooping = false
      }
    }

    if (sign != 0) {
      charPos -= 1
      buf(charPos) = sign.toByte
    }
  }

  /**
   * Modified to work with negative numbers
   */
  def stringSize(i: Long): Int = {
    if (i < 0) stringSizeImpl(-i) + 1
    else stringSizeImpl(i)
  }

  // Requires positive x
  // Original from java.lang.Long
  private  def stringSizeImpl(x: Long): Int = {
    var p: Long = 10L
    var i: Int = 1

    while (i < 19 && x < p) {
      p = 10L * p
      i += 1
    }

    i
  }
}
