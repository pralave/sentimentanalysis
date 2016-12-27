package com.github.shkesar.thealth.classification

import scalax.io.{LongTraversable, Resource}

object NaiveBayes {
  def train(dictionaryPath:String, emotions: Array[String]): NaiveBayesModel = {
    val dictionaryLines = Resource.fromFile(dictionaryPath).lines()
    val totalLines = dictionaryLines.size

    val priorProb = emotions.map(getEmotionLineCount(dictionaryLines, _).toDouble / totalLines)
    val vocabEmotion = emotions.map(genEmotionWords(dictionaryLines, _).toSeq)
    val vocabEmotionLength = vocabEmotion.map(_.length)
    val vocabSize = vocabEmotion.flatMap(_.toList.distinct).size

    new NaiveBayesModel(vocabSize, vocabEmotion, vocabEmotionLength, emotions, priorProb)
  }

  // helper functions
  private def genEmotionWords(dictionaryLines: LongTraversable[String], emotion: String) =
    dictionaryLines.map(_.split(",")).filter(_(0) == emotion).flatMap(_(1).split(" "))

  private def getEmotionLineCount(dictionaryLines: LongTraversable[String], emotion: String) =
    dictionaryLines.map(_.split(",")(0)).filter(_ == emotion).size
}
