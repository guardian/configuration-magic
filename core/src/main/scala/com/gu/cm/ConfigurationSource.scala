package com.gu.cm

import java.io.File

import com.typesafe.config._

trait ConfigurationSource {
  def load: Config
}

object EmptyConfigurationSource extends ConfigurationSource {
  val load: Config = ConfigFactory.empty()
}

class FileConfigurationSource(file: File) extends ConfigurationSource {
  override def load: Config = ConfigFactory.parseFileAnySyntax(file)
}

object FileConfigurationSource {
  def apply(file: File): FileConfigurationSource = new FileConfigurationSource(file)
  def apply(file: String): FileConfigurationSource = new FileConfigurationSource(new File(file))
}

class ClassPathConfigurationSource(path: String) extends ConfigurationSource {
  override def load: Config = ConfigFactory.parseResourcesAnySyntax(path)
}

object ClassPathConfigurationSource {
  def apply(path: String): ClassPathConfigurationSource = new ClassPathConfigurationSource(path)
}
