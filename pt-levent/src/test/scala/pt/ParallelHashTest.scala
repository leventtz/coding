package pt

import akka.stream.scaladsl.FileIO
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._

import java.io.{File, PrintWriter}
import java.nio.file.{Paths, StandardOpenOption}
import java.util.UUID
import scala.io.Source
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ParallelHashTest extends AnyFlatSpec with should.Matchers with BeforeAndAfterAll {
  "ParallelHash" should "check if given string is a valid url" in {
    ParallelHash.isUrlValid("http://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("https://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("ftp://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("sftp://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("ftp://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("lpfs://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("file://www.protegotrust.com") should be(true)
    ParallelHash.isUrlValid("ww://www.protegotrust.com") should be(false)
    ParallelHash.isUrlValid("http:www.protegotrust.com") should be(false)
    ParallelHash.isUrlValid("http//:www.protegotrust.com") should be(false)
    ParallelHash.isUrlValid("http") should be(false)
  }

  "ParallelHash" should "return md5 hash value of given string" in {
    ParallelHash.mdHash("test").toUpperCase() should be("098F6BCD4621D373CADE4E832627B4F6")
    ParallelHash.mdHash("https//:www.protegotrust.com").toUpperCase should be("A146282BA24754A46FA3BF57BF74A4E7")
    ParallelHash.mdHash("https//:www.google.com").toUpperCase should be("6859CBC36D91A30C1B76DDCF453BC84F")
  }

  it should "throw IllegalArgumentException if null or empty url given" in {
    a[IllegalArgumentException] should be thrownBy {
      ParallelHash.mdHash(null)
    }
    a[IllegalArgumentException] should be thrownBy {
      ParallelHash.mdHash("")
    }
  }

  it should "throw IllegalArgumentException if null or empty file name given" in {
    a[IllegalArgumentException] should be thrownBy {
      ParallelHash.processFile(null, null)
    }
    a[IllegalArgumentException] should be thrownBy {
      ParallelHash.processFile("", "")
    }
  }
  it should "throw IllegalArgumentException if file does not exist" in {
    a[IllegalArgumentException] should be thrownBy {
      ParallelHash.processFile("inTest.txt", "outTest.txt")
    }
  }

  val inputFile = File.createTempFile(UUID.randomUUID().toString, ".txt");
  val outputFile = File.createTempFile(UUID.randomUUID().toString, ".txt");

  it should "process the file" in {
    val inputFileContent = "https://www.google.com\nhttps://www.microsoft.com\nthis_should_be_invalid\nhttps://www.yahoo.com"
    val outputFileContent = Array("8ffdefbdec956b595d257f0aaeefd623", "d696f975bb3df45472669f8d56aef437", "Invalid URL", "99e8a2cb4638bb798cf9167e5af5b83b");

    val writer = new PrintWriter(inputFile)
    writer.write(inputFileContent)
    writer.close()
    val sink = FileIO.toPath(Paths.get(outputFile.getAbsolutePath), Set(StandardOpenOption.WRITE))
    val result = ParallelHash.processFile(inputFile.getAbsolutePath, outputFile.getAbsolutePath)
    Await.ready(result,100 milliseconds)

    assert(Source.fromFile(outputFile).getLines().length == outputFileContent.length)
    assert(Source.fromFile(outputFile).getLines().zip(outputFileContent).toList.filter(a => a._1 != a._2).isEmpty)
  }

  override def afterAll() {
    inputFile.delete();
    outputFile.delete();
  }
}
