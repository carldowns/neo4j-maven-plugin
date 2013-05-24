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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which stops a neo4j server.
 *
 * @author rhk
 */
@Mojo(name = "stop-server")
public class Neo4jStopServerMojo extends AbstractMojo {

    public void execute()
            throws MojoExecutionException {
        if (getPluginContext().containsKey("neo4j-server-thread")) {
            final Neo4jServerThread neo4jServerThread = (Neo4jServerThread) getPluginContext().get("neo4j-server-thread");
            neo4jServerThread.shutdown();
        } else {
            getLog().error("cannot find the Neo4j CommunityServer to shutdown");
        }
    }
}
