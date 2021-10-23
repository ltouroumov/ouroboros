package ch.ltouroumov.ouroboros.utils

import org.apache.logging.log4j.{LogManager, Logger}

trait StrictLogging { self =>
  protected val logger: Logger = LogManager.getLogger(self.getClass)
}
