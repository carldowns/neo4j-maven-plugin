# Maven Plugin for Neo4j Community Server

## Introduction

This is a (yet very simple) maven plugin to start and stop an embedded Neo4j Community Server on a given port. With this plugin, it is possible, to run integration tests, that depends on an existing Neo4j Server.

## Usage

### Maven Coordinates

The plugin is available at the following maven coordinates:

| coordinate | value              |
| :--------- | :----------------- |
| groupId    | de.herschke        |
| artifactId | neo4j-maven-plugin |
| version    | 2.0.1-SNAPSHOT     |

To use the plugin, you have to:
- clone this repo, 
- build this maven project and install the plugin in the local repository
- add the plugin to your own project.

### Sample Configuration

Here is a sample `pom.xml` that uses this plugin:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    ...

    <profiles>
        <profile>
            <id>neo4j-server</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>de.herschke</groupId>
                        <artifactId>neo4j-maven-plugin</artifactId>
                        <version>2.0.1-SNAPSHOT</version>
                        <executions>
                            <execution>
                                <id>start-neo4j-server</id>
                                <phase>pre-integration-test</phase>
                                <goals>
                                    <goal>start-server</goal>
                                </goals>
                            </execution>
                            <execution>
                                <id>stop-neo4j-server</id>
                                <phase>post-integration-test</phase>
                                <goals>
                                    <goal>stop-server</goal>
                                </goals>
                            </execution>
                        </executions>
                        <configuration>
                            <port>7474</port>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    ...
</project>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As you can see, the port is configured for two executions. These executions let maven start the Neo4j CommunityServer at the `pre-integration-test` phase and stop it at the `post-integration-test` phase.

Within the `integration-test` lifecycle you can now use the server as you use a commonly installed server.

### Using Server-Extensions

One of the famous benefits of Neo4j's Server is the use of your home-brew extensions. And finally you'd like to test the usage of these extensions in your app.

To do so, you have to define the extensions in two ways inside your `pom.xml`:

- declare them as a dependency for the neo4j-maven-plugin
- declare them as server-extensions in the configuration section of the neo4j-maven-plugin

Here's an example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<project>
    ...
    <plugin>
        <groupId>de.herschke</groupId>
        <artifactId>neo4j-maven-plugin</artifactId>
        <version>2.0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <id>start-neo4j-server</id>
                <phase>pre-integration-test</phase>
                <goals>
                    <goal>start-server</goal>
                </goals>
                <configuration>
                    <port>7474</port>
                    <serverExtensions>
                        <serverExtension>
                            <packageName>org.neo4j.server.extension.helloworld</packageName>
                            <mountPoint>/helloworld</mountPoint>
                        </serverExtension>
                    </serverExtensions>
                </configuration>
            </execution>
            ...
        </executions>
        <dependencies>
            <dependency>
                <groupId>org.neo4j</groupId>
                <artifactId>helloworld-extension</artifactId>
                <version>1.0</version>    
            </dependency>
        </dependencies>
    </plugin>
    ...
</project>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Using Server Plugins

To use a server plugin (e.g. [Neo4j Spatial](https://github.com/neo4j/spatial)), 
just add the dependency to the plugin configuration. The plugin will 
automatically announce itself.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<project>
    ...
           <build>
                <plugins>
                    <plugin>
                        <groupId>de.herschke</groupId>
                        <artifactId>neo4j-maven-plugin</artifactId>
                        <version>2.0.1-SNAPSHOT</version>
                        <executions>
                            <execution>
                                <id>start-neo4j-server</id>
                                <phase>pre-integration-test</phase>
                                <goals>
                                    <goal>start-server</goal>
                                </goals>
                                <configuration>
                                    <port>7777</port>
                                </configuration>
                            </execution>
                            <execution>
                                <id>stop-neo4j-server</id>
                                <phase>post-integration-test</phase>
                                <goals>
                                    <goal>stop-server</goal>
                                </goals>
                            </execution>
                        </executions>
                        <dependencies>
                            <dependency>
                                <artifactId>neo4j-spatial</artifactId>
                                <groupId>org.neo4j</groupId>
                                <version>0.12-neo4j-2.0.0-SNAPSHOT</version>
                            </dependency>
                        </dependencies>
                    </plugin>
                </plugins>
            </build>
    ...
</project>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Roadmap

Some plan's for further releases:

- deploy the plugin to the maven repository
- let you specify more configuration values (e.g. databaseDirectory, neo4j-server.properties location)
- better error handling and previously "port in use" check
