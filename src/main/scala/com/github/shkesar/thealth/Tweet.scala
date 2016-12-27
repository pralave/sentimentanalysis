package com.github.shkesar.thealth

case class Tweet(primeTag: String,
                 primeSentiment: String,
                 text: String,
                 year: Int,
                 month: java.time.Month,
                 week: Int,
                 day: Int,
                 hour: Int)