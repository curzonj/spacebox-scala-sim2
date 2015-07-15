package com.github.curzonj

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object CommandReceiver extends Logging {

  lazy val redis = RedisFactory.get
  implicit val formats = DefaultFormats ++ JavaTypesSerializers.all

  var lastCommandFetch: Future[List[String]] = Future { List() }
  def fetchCommands(thisTick: Long): Future[List[String]] = {
    val name = "command_at_"+thisTick
    val commandFetch = lastCommandFetch

    logger.trace("at=commandFetch:start ts={}", thisTick)
    lastCommandFetch = redis.withTransaction { t =>
      t.rename("commands", name)
      t.lRange[String](name)
    }

    lastCommandFetch onComplete { _ =>
      logger.trace("at=commandFetch:complete ts={}", thisTick)
    }

    commandFetch
  }

  case class ApiVector3(x: Float, y: Float, z: Float)
  case class ApiVector4(x: Float, y: Float, z: Float, w: Float)
  case class ApiCommand(uuid: UUID, patch: ApiPatch) {
    var sourceJson: Option[JValue] = None
  }
  case class ApiPatch(
                     uuid: Option[UUID],
                     `type`: String,
                     solar_system: Option[UUID],
                     agent_id: Option[UUID],
                     blueprint: Option[UUID],
                     position: Option[ApiVector3],
                     velocity: Option[ApiVector3],
                     chunk: Option[ApiVector3],
                     facing: Option[ApiVector4],
                     health: Int,
                     model_name: String,
                     systems: Map[String, JValue]
                     )

  def commands(tick: Long): Future[List[ApiCommand]] = {
    CommandReceiver.fetchCommands(tick) map { list =>
      list map { value =>
        val parsed = Try(parse(value))
        val extracted = parsed flatMap {
          v => Try(v.extract[ApiCommand])
        } map { cmd =>
          val bytes = KryoFactory.serialize(cmd)
          logger.debug("data sizes kryoDeflate={} json={}", bytes.size, value.getBytes.size)
          var dser = KryoFactory.readObject(bytes, classOf[ApiCommand])
          println(dser)

          // parsed.get can't throw an exception here because
          // Try.map only looks at successes
          cmd.sourceJson = Some(parsed.get)
          cmd
        }

        if (extracted.isFailure) logger.error("Failed to parse `"+value.toString+"`", extracted.failed.get)

        extracted
      } collect {
        case Success(v) => v
      }
    }
  }
}
