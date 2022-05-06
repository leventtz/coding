package pt

import akka.actor.ActorSystem

import java.io.File
import scala.io.Source


object Main {
  implicit val dispatcher = ParallelHash.system.dispatcher
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage inputFileName")
    } else {
      val inputFileName = new File(args(0))
      val outputFileName = new File("hash_" + inputFileName)
      outputFileName.createNewFile();
      val result = ParallelHash.processFile(inputFileName.getAbsolutePath, outputFileName.getAbsolutePath)
      result.onComplete(a => Source.fromFile(outputFileName).getLines().foreach(println))
    }
  }
}
