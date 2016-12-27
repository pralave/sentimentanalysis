package com.github.shkesar.thealth.classification

import org.apache.spark.rdd.RDD

class NaiveBayesModel(val vocabSize: Long,
                      private val vocabEmotion: Array[Seq[String]],
                      private val vocabEmotionLength: Array[Int],
                      val emotions: Array[String],
                      val priorProbabilities: Array[Double]) {

  def predict(tweet: String): String = {
    val words = tweet.split(" ")
    val emotionScore = (priorProbabilities zip (0 to 2)).map { case (priorProb: Double, index: Int) =>
      words.foldLeft(priorProb)((sum, word) => {
        sum * wordCondProbability(word, vocabEmotionLength(index), vocabSize, vocabEmotion(index))
      })
    }.zip(emotions).toSeq

    println(tweet + " -> " + emotionScore.mkString(" "))

    val prominentEmotion = emotionScore.max(maxEmotion)._2
    prominentEmotion
  }

  def predict(tweetRDD: RDD[String]): RDD[String] = {
    tweetRDD.map(predict)
  }

  private def wordCondProbability(word: String, vocabEmotionSize: Long, vocabSize: Long, vocabEmotion: Seq[String]): Double = {
    val termLength = vocabEmotion.count(_ equals word)
    ((termLength / vocabEmotionSize.toDouble) + 1.0) / (vocabSize + 1)
  }

  private def maxEmotion = new Ordering[(Double, String)] {
    override def compare(x: (Double, String), y: (Double, String)): Int = {
      val diff = x._1 - y._1
      if (diff > 0) diff.ceil.toInt else diff.floor.toInt // convert a floating difference result to an integer keeping the Ordering API the same
    }
  }

}
