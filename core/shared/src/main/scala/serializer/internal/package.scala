package serializer

package object internal {
  implicit class RichString(val s: String) extends AnyVal {
    def isNullOrBlank: Boolean = {
      if (null == s) return true

      var i: Int = 0
      val len: Int = s.length()
      while (i < len) {
        if (!Character.isWhitespace(s.charAt(i))) return false
        i += 1
      }

      true
    }

    def isNotNullOrBlank: Boolean = !isNullOrBlank
  }

  implicit class RichAnyRef(val v: AnyRef) extends AnyVal {
    def isNull: Boolean = {
      if (null == v) true
      else false
    }

    def isNotNull: Boolean = !isNull
  }
}
