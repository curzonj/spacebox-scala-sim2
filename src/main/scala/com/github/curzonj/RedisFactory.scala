package com.github.curzonj

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.slf4j.LoggerFactory
import scredis._
import scala.util.{ Success, Failure }
import java.net.URI

object RedisFactory extends Logging {

  val typesafeConfig = ConfigFactory.load()
  val mergedConfig = sys.env.get("REDIS_URL").map { value =>
    val uri = new URI(value)
    logger.info("REDIS_URL={}", value)

    typesafeConfig.withValue("scredis.redis.host",
      ConfigValueFactory.fromAnyRef(uri.getHost))
  }.getOrElse(typesafeConfig)

  val redis = Redis(mergedConfig.getConfig("scredis"))

  // Import internal ActorSystem's dispatcher (execution context) to register callbacks
  import redis.dispatcher

  def get: Redis = redis
  def destroy = redis.quit()

}
