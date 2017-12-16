package com.gu.cm

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.amazonaws.services.simplesystemsmanagement.{AWSSimpleSystemsManagement, AWSSimpleSystemsManagementClientBuilder}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}


class SystemsManagerParametersConfigurationSource(systemsManager: AWSSimpleSystemsManagement, identity: Identity) extends ConfigurationSource {

  val path = s"/${identity.stack}/${identity.stage}/${identity.app}"
  val pathWithTrailingSlash = s"$path/"

  final case class ParametersPage(parameters: Seq[Parameter], nextToken: Option[String])
  final object ParametersPage {
    val first = ParametersPage(Seq(), None)
  }

  def getAllParametersByPath(path: String): Map[String, _] = {
    def getAllParametersByPathPaginated(path: String, currentParametersPage: ParametersPage): ParametersPage = {
      val result = systemsManager.getParametersByPath(new GetParametersByPathRequest()
        .withPath(path)
        .withMaxResults(10)
        .withWithDecryption(true)
        .withNextToken(currentParametersPage.nextToken.orNull)
      )
      val parametersPage = ParametersPage(
        parameters = result.getParameters.asScala ++ currentParametersPage.parameters,
        nextToken = Option(result.getNextToken)
      )
      if (parametersPage.nextToken.isDefined) {
        getAllParametersByPathPaginated(path, parametersPage)
      } else {
        parametersPage
      }
    }

    val parametersPage = getAllParametersByPathPaginated(path, ParametersPage.first)

    parametersPage.parameters.map(p => (p.getName.replace(pathWithTrailingSlash, ""), p.getValue)).toMap
  }

  override def load: Config = {
    val config = for {
      parameters <- Try(getAllParametersByPath(path))
    } yield {
      ConfigFactory.parseMap(parameters.asJava, s"Systems Manager Parameters $path [App=${identity.app}, Stage=${identity.stage}]")
    }

    config match {
      case Success(theConfig) => theConfig
      case Failure(theFailure) => ConfigFactory.empty(s"no Systems Manager Parameters (or failed to load) for $path [App=${identity.app}, Stage=${identity.stage}], exception=[$theFailure]")
    }
  }
}

object SystemsManagerParametersConfigurationSource {
  def apply(identity: Identity): SystemsManagerParametersConfigurationSource = {
    val systemsManager: AWSSimpleSystemsManagement = AWSSimpleSystemsManagementClientBuilder.standard()
      .withCredentials(new DefaultAWSCredentialsProviderChain())
      .withRegion(Regions.fromName(identity.region))
      .build()

    new SystemsManagerParametersConfigurationSource(systemsManager, identity)
  }
}