package com.morazow.turkmenbot

import twitter4j.Twitter
import twitter4j.TwitterFactory

object TurkmenBot {

  def main(args: Array[String]) = {

    val twitterFactory = new TwitterFactory()
    val twitter = twitterFactory.getInstance()

    println("Updating status!")
    twitter.updateStatus("Test")

    println("Done!")

  }
}
