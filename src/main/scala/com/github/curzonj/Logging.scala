package com.github.curzonj

import org.slf4j.LoggerFactory

trait Logging {
  protected lazy val logger = LoggerFactory.getLogger(getClass.getName)
}
