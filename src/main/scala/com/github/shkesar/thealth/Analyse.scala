package com.github.shkesar.thealth

import java.time.{ZoneId, LocalDateTime}

import classification.NaiveBayes
import com.google.gson.GsonBuilder

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Minutes, StreamingContext}

import scalax.io.Resource

/*
  Analyses the tweets streamed on the basis of filters
  and compares them against the NaiveBayes model
 */
object Analyse {
  val filterEntities = Array("google", "thoughtworks", "#apple")
  val emotions = Array("neutral", "positive", "negative")

  val dictionaryPath = Train.dictionaryOutputPath

  val windowSlideDuration = 1 // minutes
  val gson = new GsonBuilder().setPrettyPrinting().create()

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Minutes(1))

    // Initialise Twitter credentials from command line parameters
    Utils.parseCommandLineWithTwitterCredentials(args)

    val tweetStream = TwitterUtils.createStream(ssc, Utils.getAuth, filterEntities)

    tweetStream.window(Minutes(2), Minutes(windowSlideDuration)).foreachRDD(rdd => {
      val tweetsRDDs = rdd.mapPartitions {
        strings => {
          // non serializable objects are better passed to each node
          val model = NaiveBayes.train(dictionaryPath, emotions)

          strings.filter { status =>
            status.getText.split(" ").exists(filterEntities contains _.toLowerCase)
          } map {
            status => {
              val date = status.getCreatedAt
              val text = status.getText
              val primeEntity = text.split(" ").filter(filterEntities contains _.toLowerCase).head

              val sentiment = model.predict(Utils.processTweet(text))

              val ldt = LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
              val week = (ldt.getDayOfYear.toDouble / 7).ceil.toInt

              Tweet(primeEntity, sentiment, text, ldt.getYear, ldt.getMonth, week, ldt.getDayOfMonth, ldt.getHour)
            }
          }
        }
      }

      val emotionCounts = filterEntities map {
        filterEntity => {
          val count = Utils.cartesianProduct(List(List(filterEntity), emotions.toList)).map { case Seq(entity, sentiment) =>
            tweetsRDDs.filter(tweet => tweet.primeTag.equals(entity) && tweet.primeSentiment.equals(sentiment)).count()
          }
          FilterAnalysis(filterEntity, SentimentCount(count(0), count(1), count(2)))
        }
      }

      val totalEmotionCounts = emotionCounts.reduce((left: FilterAnalysis, right: FilterAnalysis) => {
        FilterAnalysis("", SentimentCount(left.emotionCount.neutralCount + right.emotionCount.neutralCount,
          left.emotionCount.positiveCount + right.emotionCount.positiveCount,
          left.emotionCount.negativeCount + right.emotionCount.negativeCount))
      }).emotionCount

      val filename = "data/result/result" + LocalDateTime.now().toString + ".json"
      val resultFileFormat = ResultFormat(filterEntities, emotions.length, emotionCounts, totalEmotionCounts, windowSlideDuration)
      val resultJson = gson.toJson(resultFileFormat)

      Resource.fromFile(filename).write(resultJson)
      println("Saved file.")
    })

    println("Initialization complete. Starting streaming ...")
    println("A JSON file will be saved every " + windowSlideDuration + " minutes")

    ssc.start()
    ssc.awaitTermination()
  }
}
