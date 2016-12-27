package com.github.shkesar.thealth

case class ResultFormat(filters: Array[String],
                         filterCount: Int,
                         counts: Array[FilterAnalysis],
                         totalCount: SentimentCount,
                         durationMinutes: Int)

/*
{
  "filters": ["apple", "#google"],
  "filterCount": 2,
  "counts": [
    {
      "filter": "apple",
      "positiveCount": "20",
      "negativeCount": "12",
      "neutralCount": "120"
    },
    {
      "filter": "#google",
      "positiveCount": "20",
      "negativeCount": "12",
      "neutralCount": "120"
    }
  ],
  "totalCount": {
    "positiveCount": "230",
    "negativeCount": "112",
    "neutralCount": "1230"
  },
  "startTime": "", // UTC string
  "endTime": "",
  "duration": "231" // seconds
}
 */
