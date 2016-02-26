# configuration-magic
_Where the magic happens_

Configuration magic is a thin wrapper around Typesafe's configuration library. Its main goal is to load the configuration from a dynamodb table.
The librairy is currently split in two, configuration-magic-core and configuration-magic-play2-4.

## Concept
### Sources
The idea is to be able to compose an application configuration from one or more sources. The sources can be files, a classpath resource or a dynamodb table.
When requiring a property, the look-up order will be the same as the configuration sources.

The look-up sources depends on the current mode of the application. Three modes are accepted, and provide the following definition:

````scala
mode match {
  case Dev => List(userHome, devClassPath, classPath)
  case Test => List(testClassPath, classPath)
  case Prod => List(dynamo, classPath)
}
````

This list of sources can be overridden if the default behaviour doesn't match the needs of your application.

### Application Identity
The library needs to identify the application currently running.

Presently, this is done by requiring an object of type ````Identity````.
````Identity```` is a trait that describes the necessary information to locate the configuration.
It is defined as:

````scala
sealed trait Identity {
  def stack: String
  def app: String
  def stage: String
  def region: String
}
````

And is implemented by ````AwsApplication```` and ````LocalApplication````

## configuration-magic-core

### Defining the DynamoDB Table
This module will provide you with a way to load your configuration from a DynamoDB Table.

The default name of the table will be: ````config-${identity.stack}````

The partition key needs to be named "App" (type String), the sort key needs to be named "Stage" (type String) and the configuration "Config" needs a Map.

Once the table is created, insert at least one item matching your application identity to expect a configuration to be loaded.

### Loading the configuration

In your build.sbt

````scala
libraryDependencies += "com.gu" %% "configuration-magic-core" %  "1.1.0"
````

Then to load the configuration

````scala
import com.gu.cm.Configuration
val config = Configuration("myApp", Mode.Prod).load
````

## configuration-magic-play2-4

This module aim to simplify the usage of configuration magic for play 2.4.

build.sbt

````scala
libraryDependencies ++= Seq(
  "com.gu" %% "configuration-magic-core" %  "1.1.0",
  "com.gu" %% "configuration-magic-play2-4" %  "1.1.0"
)
````

application.conf

````
play.application.name="myApp"
play.application.loader="com.gu.cm.ConfigurationGuiceApplicationLoader"
````

You can also modify de default behaviour by creating your own loader that extends ````ConfigurationGuiceApplicationLoader```` or that manually calls the core configruation.
See ConfigurationGuiceApplicationLoader.scala for a reference implementation.

## Logging
By default the library will log to sysout, but if desired you can implement com.gu.Logger and pass that implementation when calling ````com.gu.cm.Configuration.apply````

Note that when using the play2.4 module, the default behaviour is to use Play's default logger.