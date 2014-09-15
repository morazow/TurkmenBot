package com.morazow.turkmenbot

import twitter4j.Twitter
import twitter4j.TwitterFactory

import twitter4j.Paging
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.ResponseList
import twitter4j.TwitterResponse

import java.util.Calendar
import java.text.SimpleDateFormat

import scala.collection.JavaConversions._

/**
 * A trait with checkAndWait function that checks whether the
 * rate limit has been hit and wait if it has.
 *
 * This ignores the fact that different request types have different
 * limits, but it keeps things simple.
 */
trait RateChecker {
  /**
   * See whether the rate limit has been hit, and wait until it
   * resets if so. Waits 10 seconds longer than the reset time to
   * ensure the time is sufficient.
   *
   * This is surely not an optimal solution, but it seems to do
   * the trick.
   */
  def checkAndWait(response: TwitterResponse, verbose: Boolean = false) {
    val rateLimitStatus = response.getRateLimitStatus
    if (verbose) println("RLS: " + rateLimitStatus)
    if (rateLimitStatus != null && rateLimitStatus.getRemaining == 0) {
      println("*** You hit your rate limit. ***")
      val waitTime = rateLimitStatus.getSecondsUntilReset + 10
      println("Waiting " + waitTime + " seconds ( "
        + waitTime/60.0 + " minutes) for rate limit reset.")
      Thread.sleep(waitTime*1000)
    }
  }
}

object TurkmenBot extends RateChecker {

  val db = "last_id.txt"
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")

  val twitterFactory = new TwitterFactory()
  val twitter = twitterFactory.getInstance()
  val userName = twitter.getScreenName

  def main(args: Array[String]) = {

    var last_id: Long = readLastID(db) // last id
    var page = new Paging()

    println("Starting infinite loop!")
    while (true) {
      Thread.sleep(1000 * 60 * 1) // wait 1 minute

      println("Getting mentions!")
      page.setSinceId(last_id)
      val mentions = twitter.getMentionsTimeline(page)
      checkAndWait(mentions, true)

      possibleReplies(mentions, userName).foreach { reply => {
        println("Replying: <" + reply._1 + "> to id: " + reply._2)

        val response = twitter.updateStatus(new StatusUpdate(reply._1).inReplyToStatusId(reply._2))
        last_id = scala.math.max(last_id, reply._2 + 1)

        checkAndWait(response, true)
      }}

      writeLastID(db, last_id)
    }

    println("Done!")
  }

  def possibleReplies(mentions: ResponseList[Status], userName: String) = {
    for {
      mention <- mentions
      statusAuthor = mention.getUser.getScreenName
      mentionedEntities = mention.getUserMentionEntities.map(_.getScreenName).toList
      participants = (statusAuthor :: mentionedEntities).toSet - userName
      text = participants.map(p=>"@"+p).mkString(" ") + parseStatus(mention.getText)
    } yield (text, mention.getId)
  }

  def parseStatus(str: String) = {
    val status = str.split("\\s+")
    if (status(1) != "!")
      getNextNakyl() // OK ýerine nakyl bilen ýürege düş
    else {
      status(2) match {
        case "Salam" | "salam" => " Waleýkimsalam!"
        case "Nakyl" | "nakyl" => getNextNakyl()
        case "Fuck"  | "fuck"  => " Samsyk, sögünme Twitter-da!"
        case _ => " Düşünmedim? " + dateFormatter.format(Calendar.getInstance().getTime())
      }
    }
  }

  import java.nio.charset.CodingErrorAction
  import scala.io.Codec

  implicit val codec = Codec("UTF-8")
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)

  lazy val nakyllar =
    scala.io.Source.fromFile("files/nakyllar.txt").getLines.toList//.filter(_.length <= 130)

  def getNextNakyl() = {
    " " + nakyllar(scala.util.Random.nextInt(nakyllar.length))
  }

  def readLastID(fileName: String): Long = {
    scala.io.Source.fromFile("last_id.txt").getLines.toList.head.toLong
  }

  def writeLastID(fileName: String, last_id: Long) = {
    import java.io.File
    import java.io.PrintWriter
    val writer = new PrintWriter(new File(fileName))
    writer.println(last_id)
    writer.close()
  }

}
