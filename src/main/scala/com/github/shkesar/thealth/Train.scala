package com.github.shkesar.thealth

import scalax.io.Resource

object Train {
  val dictionaryOutputPath = "/Users/shubham/projects/thealth/data/dict/sentiment_dictionary.txt"
  val subjectivityDictionaryPath = "/Users/shubham/projects/thealth/data/dict/subjclueslen1-HLTEMNLP05.tff"

  def main(args: Array[String]): Unit = {
    val dictionaryFile = Resource.fromFile(dictionaryOutputPath)

    val dictionary = getDictionary().groupBy(_._2).map(list => list._1 -> list._2.map(_._1))

    dictionaryFile.write(dictionary.map{case (key, values) => key + "," + values.mkString(" ")}.mkString("\n"))
  }

  /*
  Generates a emotion dictionary:
    Map[word -> polarity] where polarity can be [positive, negative, neutral]
  Currently excluding words with priorPolarity = [weakneg, stringneg, both]
   */
  def getDictionary(): Map[String, String] = {
    val DictRegex = """type=(\w+) len=(\d+) word1=([\w-]+) pos1=(\w+) stemmed1=([n|y]{1}) priorpolarity=(positive|negative|neutral)""".r
    val dictionaryFile = Resource.fromFile(subjectivityDictionaryPath)

    val dictionaryMap = dictionaryFile.lines().map {
      case DictRegex(wordType, len, word, pos, stemmed, priorPolarity) => (word -> priorPolarity)
      case x => ("", "")
    }.toMap

    dictionaryMap.filter{ case (x, y) => !x.equals("") && !y.equals("")}
  }
}
