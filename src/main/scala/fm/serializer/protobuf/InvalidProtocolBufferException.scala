// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package fm.serializer.protobuf

import java.io.IOException

final class InvalidProtocolBufferException(msg: String) extends IOException(msg)

object InvalidProtocolBufferException {
  def truncatedMessage() = new InvalidProtocolBufferException(
      "While parsing a protocol message, the input ended unexpectedly " +
      "in the middle of a field.  This could mean either than the " +
      "input has been truncated or that an embedded message " +
      "misreported its own length.")
  
  def negativeSize() = new InvalidProtocolBufferException(
      "CodedInputStream encountered an embedded string or message " +
      "which claimed to have negative size.") 
  
  def malformedVarint() = new InvalidProtocolBufferException(
      "CodedInputStream encountered a malformed varint.")
  
  def invalidTag() = new InvalidProtocolBufferException(
      "Protocol message contained an invalid tag (zero).")
  
  def invalidEndTag() = new InvalidProtocolBufferException(
      "Protocol message end-group tag did not match expected tag.")
  
  def invalidWireType(wireType: Int) = new InvalidProtocolBufferException(
      "Protocol message tag had invalid wire type: "+wireType)

  def unexpectedWireType(wireType: Int, expectedWireType: Int) = new InvalidProtocolBufferException(
    "Protocol message tag had unexpected wire type.  Expected: "+expectedWireType+"  Got: "+wireType)

  def recursionLimitExceeded() = new InvalidProtocolBufferException(
      "Protocol message had too many levels of nesting.  May be malicious.  " +
      "Use CodedInputStream.setRecursionLimit() to increase the depth limit.")
  
  def sizeLimitExceeded() = new InvalidProtocolBufferException(
      "Protocol message was too large.  May be malicious.  " +
      "Use CodedInputStream.setSizeLimit() to increase the size limit.")
}

