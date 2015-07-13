import java.util.TimerTask
import java.util.concurrent.{TimeUnit, ScheduledFuture, ScheduledThreadPoolExecutor}
import org.json4s.jackson.JsonMethods
import scredis.exceptions.RedisTransactionAbortedException
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}
import com.github.curzonj.RedisFactory
import org.slf4j.LoggerFactory
import ExecutionContext.Implicits.global
import org.json4s._
import org.json4s.jackson.JsonMethods._

object SimRunner {
  def main(args: Array[String]): Unit = {
    currentTick = System.currentTimeMillis
    pool.schedule(task, interval, TimeUnit.MILLISECONDS)

    println("scheduled")
  }

  val logger = LoggerFactory.getLogger(getClass)
  val task = new TimerTask {
    def run() =  worldTick
  }

  val interval = 80
  var currentTick: Long = 0

  lazy val pool = new ScheduledThreadPoolExecutor(1)
  lazy val redis = RedisFactory.get

  def worldTick: Unit = {
    currentTick += interval
    val startedAt= System.currentTimeMillis
    val jitter = startedAt - currentTick

    printM("world tick", "tick" -> currentTick, "jitter" -> jitter, "ts" -> startedAt)

    val name = "command_at_"+currentTick
    redis.withTransaction { t =>
      t.rename("commands", name)
      t.lRange[String](name)
    } map { value =>
      def optionalJson(s: String): Any = Try(parse(s)) getOrElse s
      val obj = value map { v => optionalJson(v) }

      println(obj)
    } onComplete { _ =>
      val endedAt = System.currentTimeMillis
      printM("end tick", "duration" -> (endedAt - startedAt))

      val delay = currentTick + interval - endedAt
      pool.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
  }

  def printM(str: String, list: (String, Any)*): Unit = {
    println(str + " " + (list map {case (key, value) => "" + key + "=" + value} mkString " "))
  }

}
