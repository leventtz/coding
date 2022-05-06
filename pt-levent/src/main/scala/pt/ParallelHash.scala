package pt

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import org.apache.commons.codec.digest.DigestUtils

import java.nio.file.{Paths, StandardOpenOption}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


object ParallelHash {
  implicit val system: ActorSystem = ActorSystem("ParallelHash")
  implicit val dispatcher = system.dispatcher

  //hash is CPU bound. use all available cores
  val parallelism: Int = Runtime.getRuntime.availableProcessors


  def isUrlValid(url: String): Boolean = {
    url.matches("(https?|ftp|sftp|file|ws|lpfs)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
  }

  def mdHash(url: String): String = {
    require(url != null && !url.isEmpty, "empty or null url string.")
    DigestUtils.md5Hex(url);
  }

  def processFile(inputFileName: String, outputFileName: String): Future[IOResult] = {
    require(inputFileName != null && !inputFileName.isEmpty, "empty or null file name.")
    require(Paths.get(inputFileName).toFile().exists(), "file does not exist.")

    val source = FileIO.fromPath(Paths.get(inputFileName))

    val frameFlow = Framing
      .delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 8192, allowTruncation = true)
      .map("" + _.utf8String.trim)
      .filter(!_.isEmpty)
      .mapAsync(parallelism)(
        url =>
          Future(if (isUrlValid(url)) mdHash(url) else "Invalid URL")
      )


    source
      .via(frameFlow)
      .map(a => ByteString.fromString(a + "\n"))
      .runWith(FileIO.toPath(Paths.get(outputFileName)))
      .andThen {
        case _ =>
          system.terminate()
          Await.ready(system.whenTerminated, 100 millisecond)
      }
  }

}
