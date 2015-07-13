import java.util.TimerTask
import java.util.concurrent.{TimeUnit, ScheduledFuture, ScheduledThreadPoolExecutor}

import org.slf4j.LoggerFactory

object SimRunner {
  def main(args: Array[String]): Unit = {
    currentTick = System.currentTimeMillis
    pool.schedule(task, interval, TimeUnit.MILLISECONDS)

    println("scheduled")
  }

  val pool = new ScheduledThreadPoolExecutor(1)
  val logger = LoggerFactory.getLogger(getClass)
  val task = new TimerTask {
    def run() =  worldTick
  }

  val interval = 80
  var currentTick: Long = 0


  def worldTick: Unit = {
    currentTick += interval
    val startedAt= System.currentTimeMillis
    var jitter = startedAt - currentTick

    printM("world tick", "tick" -> currentTick, "jitter" -> jitter, "ts" -> startedAt)

    val endedAt = System.currentTimeMillis
    val delay = currentTick + interval - endedAt
    pool.schedule(task, delay, TimeUnit.MILLISECONDS)
  }

  def printM(str: String, list: (String, Any)*): Unit = {
    println(str + " " + (list map {case (key, value) => "" + key + "=" + value} mkString " "))
  }

}
