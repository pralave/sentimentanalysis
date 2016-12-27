package com.github.shkesar.thealth

import org.apache.commons.cli.{Options, ParseException, PosixParser}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

object Utils {

  val numFeatures = 1000
  val tf = new HashingTF(numFeatures)

  val CONSUMER_KEY = "consumerKey"
  val CONSUMER_SECRET = "consumerSecret"
  val ACCESS_TOKEN = "accessToken"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"

  val THE_OPTIONS = {
   val options = new Options()
   options.addOption(CONSUMER_KEY, true, "Twitter OAuth Consumer Key")
   options.addOption(CONSUMER_SECRET, true, "Twitter OAuth Consumer Secret")
   options.addOption(ACCESS_TOKEN, true, "Twitter OAuth Access Token")
   options.addOption(ACCESS_TOKEN_SECRET, true, "Twitter OAuth Access Token Secret")
   options
  }

  def parseCommandLineWithTwitterCredentials(args: Array[String]) = {
   val parser = new PosixParser
   try {
     val cl = parser.parse(THE_OPTIONS, args)
     System.setProperty("twitter4j.oauth.consumerKey", cl.getOptionValue(CONSUMER_KEY))
     System.setProperty("twitter4j.oauth.consumerSecret", cl.getOptionValue(CONSUMER_SECRET))
     System.setProperty("twitter4j.oauth.accessToken", cl.getOptionValue(ACCESS_TOKEN))
     System.setProperty("twitter4j.oauth.accessTokenSecret", cl.getOptionValue(ACCESS_TOKEN_SECRET))
     cl.getArgList.toArray
   } catch {
     case e: ParseException =>
       System.err.println("Parsing failed.  Reason: " + e.getMessage)
       System.exit(1)
   }
  }

  def getAuth = {
   Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  }

  object IntParam {
   def unapply(str: String): Option[Int] = {
     try {
       Some(str.toInt)
     } catch {
       case e: NumberFormatException => None
     }
   }
  }

  def cartesianProduct[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] =
  xs.foldLeft(Seq(Seq.empty[A])) {
    (x, y) => for (a <- x.view; b <- y) yield a :+ b
  }

  def processTweet(tweetString: String) = {
    val ENGLISH_STOP_WORDS = Seq("a", "an", "and", "are", "as", "at", "be", "but", "by",
      "for", "if", "in", "into", "is", "it",
      "no", "not", "of", "on", "or", "such",
      "that", "the", "their", "then", "there", "these",
      "they", "this", "to", "was", "will", "with")

    val stopWordsRegexString = ("""\b(""" + ENGLISH_STOP_WORDS.mkString("|") +""")\b""").r.toString
    val hashTagRegexString = """(#\w+)"""
    val mentionRegexString = """(@\w+)"""
    val urlRegexString = """(https?|ftp)://(\w+).((\w+)/)+(\w+)"""

    val replacedString = tweetString.toLowerCase.replaceAll(stopWordsRegexString , "")
    replacedString.replaceAll(hashTagRegexString, "")
      .replaceAll(mentionRegexString, "")
      .replaceAll(urlRegexString, "")
      .replaceAll(" {2,}", " ").trim
  }

}
