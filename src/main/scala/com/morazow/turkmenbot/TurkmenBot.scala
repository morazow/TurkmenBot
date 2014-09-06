package com.morazow.turkmenbot

import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.StatusUpdate

import scala.collection.JavaConversions._

object TurkmenBot {

  def main(args: Array[String]) = {

    val twitterFactory = new TwitterFactory()
    val twitter = twitterFactory.getInstance()

    println("Updating status!")
    //twitter.updateStatus("Test")
    val num = if (args.length == 1) args(0).toInt else 10
    val userName = twitter.getScreenName
    val statuses = twitter.getMentionsTimeline.take(num)
    println("Statuses: "+statuses)

    statuses.foreach { status => {
      val statusAuthor = status.getUser.getScreenName
      val mentionedEntities = status.getUserMentionEntities.map(_.getScreenName).toList
      val participants = (statusAuthor :: mentionedEntities).toSet - userName
      val text = participants.map(p=>"@"+p).mkString(" ") + parseStatus(status.getText)
      val reply = new StatusUpdate(text).inReplyToStatusId(status.getId)
      println("Replying: " + text)
      twitter.updateStatus(reply)
    }}

    println("Done!")

  }

  def parseStatus(str: String) = {
    val status = str.split("\\s+")
    if (status(1) != "!")
      " OK."
    else {
      status(2) match {
        case "Salam" | "salam" => " Waleykimsalam"
        case "Nakyl" | "nakyl" => getNextNakyl()
        case _ => " Düşünmedim?"
      }
    }
  }

  lazy val nakyllar =
    scala.io.Source.fromFile("nakyllar.txt").getLines.toList//.filter(_.length <= 130)

  var nextNakyl = -1

  def getNextNakyl() = {
    //nextNakyl = (nextNakyl + 1) % nakyllar.length
    " " + nakyllar(scala.util.Random.nextInt(nakyllar.length))
  }

}
