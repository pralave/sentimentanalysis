package com.github.shkesar.thealth

case class SentimentCount(var neutralCount: Long,
                      var positiveCount: Long,
                      var negativeCount: Long)

case class FilterAnalysis(filter: String, emotionCount: SentimentCount)