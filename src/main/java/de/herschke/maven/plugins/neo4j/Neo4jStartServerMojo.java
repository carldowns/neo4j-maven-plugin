/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.herschke.maven.plugins.neo4j;

import java.io.File;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which starts a neo4j server.
 *
 * @author rhk
 */
@Mojo(name = "start-server")
public class Neo4jStartServerMojo extends AbstractMojo {

    @Parameter(defaultValue = "7474")
    private int port;
    @Parameter
    private File databasePath;
    @Parameter
    private ServerExtension[] serverExtensions;
    @Parameter
    private Properties serverProperties;

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public File getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(File databasePath) {
        this.databasePath = databasePath;
    }

    public ServerExtension[] getServerExtensions() {
        return serverExtensions;
    }

    public void setServerExtensions(ServerExtension[] serverExtensions) {
        this.serverExtensions = serverExtensions;
    }

    public Properties getServerProperties() {
        return serverProperties;
    }

    public void setServerProperties(Properties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public void execute()
            throws MojoExecutionException {
        final Neo4jServerThread neo4jServerThread = new Neo4jServerThread(getLog(), "localhost", port);
        if (databasePath != null) {
            neo4jServerThread.useDatabaseDir(databasePath);
        }
        if (serverProperties != null) {
            for (String key : serverProperties.stringPropertyNames()) {
                neo4jServerThread.withProperty(key, serverProperties.getProperty(key));
            }
        }
        if (serverExtensions != null) {
            for (ServerExtension extension : serverExtensions) {
                neo4jServerThread.withExtension(extension);
            }
        }
        getPluginContext().put("neo4j-server-thread", neo4jServerThread);
        neo4jServerThread.start();
    }
}
