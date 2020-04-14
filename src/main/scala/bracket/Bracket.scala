package zio.cookbook.bracket

import java.io.FileInputStream
import java.nio.charset.StandardCharsets

import zio.{ Task, UIO }

object BracketLib {

  def closeStream(is: FileInputStream) =
    UIO(is.close())

  // helper method to work around in Java 8
  def readAll(fis: FileInputStream, len: Long): Array[Byte] = {
    val content: Array[Byte] = Array.ofDim(len.toInt)
    fis.read(content)
    content
  }

  def convertBytes(is: FileInputStream /* , len: Long */ ) =
    // Task.effect(println(new String(readAll(is, len), StandardCharsets.UTF_8))) // Java 8
    Task.effect(new String(is.readAllBytes(), StandardCharsets.UTF_8)) // Java 11+

}
