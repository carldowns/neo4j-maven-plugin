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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which starts a neo4j server.
 *
 * @author rhk
 */
@Mojo(name = "start-server")
public class Neo4jStartServerMojo extends AbstractMojo {

    @Component
    private MavenSession session;
    @Component
    private MavenProject project;
    @Component
    private MojoExecution mojo;
    @Parameter(defaultValue = "7474")
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public MavenSession getSession() {
        return session;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MojoExecution getMojo() {
        return mojo;
    }

    public void setMojo(MojoExecution mojo) {
        this.mojo = mojo;
    }

    public void execute()
            throws MojoExecutionException {
        final Neo4jServerThread neo4jServerThread = new Neo4jServerThread(getLog(), "localhost", port);
        getPluginContext().put("neo4j-server-thread", neo4jServerThread);
        neo4jServerThread.start();
    }
}
