package com.github.shkesar.thealth.classification

import com.github.shkesar.thealth.classification.NaiveBayes._
import org.scalatest.FlatSpec

class NaiveBayesTest extends FlatSpec {
  val dictPath = "dict.txt"
  val emotions = Array("Happy", "Sad", "Angry")

  "NaiveBayes" should "train a NaiveBayesModel" in {
    val nvModel = train(dictPath, emotions)
    assert(nvModel.vocabSize > 0)
  }

  "NaiveBayesModel" should "contain vocabulary words of all class" in {
    val nvModel = train(dictPath, emotions)
    assert(nvModel.priorProbabilities.filter(_ > 0).length > 0)
  }

  it should "predict sample tweets correctly" in {
    val nvModel = train(dictPath, emotions)
    val sampleTweets =
      """|so much hate you
        |we had a great party
        |it is love
        |hate this so annoy
        |depress
        |so happy and that was cheerful
        |hate you and hate you
        |angry
        |bored upset gloomy
        |gloomy
        |upset gloomy sad""".stripMargin.lines.toArray

    // these results are obtained from the NaiveBayesImplementation previously written in Python
    // the results were are prominent based on human reading but they were not thoroughly tested
    // algorithm used by this program is just a scala port of that code
    val neededResult =
      """|Angry
        |Happy
        |Happy
        |Angry
        |Sad
        |Happy
        |Angry
        |Angry
        |Sad
        |Sad
        |Sad""".stripMargin.lines.toArray

    val calculatedResult = sampleTweets.map(nvModel.predict(_))
    assert(neededResult.deep == calculatedResult.deep)
  }
}