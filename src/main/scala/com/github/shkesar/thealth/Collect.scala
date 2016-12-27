package com.github.shkesar.thealth

import java.io.File

import com.google.gson.{JsonParser, Gson}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Collect at least the specified number of tweets into json text files.
 */
object Collect {
  private var numTweetsCollected = 0L
  val gson = new Gson()
  val jsonParser = new JsonParser

  def main(args: Array[String]) {
    // Process program arguments and set properties
    if (args.length < 3) {
      System.err.println("Usage: " + this.getClass.getSimpleName +
        "<outputDirectory> <numTweetsToCollect> <intervalInSeconds> <partitionsEachInterval>")
      System.exit(1)
    }
    val Array(outputDirectoryName, Utils.IntParam(numTweetsToCollect),  Utils.IntParam(intervalSecs), Utils.IntParam(partitionsEachInterval)) =
      Utils.parseCommandLineWithTwitterCredentials(args)
    val outputDir = new File(outputDirectoryName.toString)
    if (outputDir.exists()) {
      System.err.println("ERROR - %s already exists: delete or specify another directory".format(
        outputDirectoryName))
      System.exit(1)
    }
    outputDir.mkdirs()

    println("Initializing Streaming Spark Context...")
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(intervalSecs))
    val sqlContext = new SQLContext(sc)

    val tweetStream: DStream[String] = TwitterUtils.createStream(ssc, Utils.getAuth)
      .map(gson.toJson(_))

    tweetStream.foreachRDD((rdd: RDD[String], time) => {
      if (rdd.count > 0) {
        val outputRDD = rdd.map { tweetString =>
          val tweetObject = jsonParser.parse(tweetString).getAsJsonObject
          if (tweetObject.get("user").getAsJsonObject.get("lang").getAsString.equals("en"))
            tweetObject.get("text").getAsString.replace("\n", " ") // convert multi-line to a single-line tweet
          else
            ""
        } filter (_ != "") repartition partitionsEachInterval

        val count = outputRDD.count()
        numTweetsCollected += count
        outputRDD.saveAsTextFile(outputDirectoryName.toString + "/part_" + time.milliseconds.toString)
        println(raw"Collected $numTweetsCollected")
        if (numTweetsCollected > numTweetsToCollect) {
          ssc.stop(stopSparkContext = true, stopGracefully = true)
          System.exit(0)
        }
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
