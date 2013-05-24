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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.cypher.SyntaxException;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.helpers.ServerBuilder;

/**
 * represents a deamon thread that starts a neo4j server
 *
 * @author rhk
 */
public class Neo4jServerThread extends Thread {

    public static enum State {

        INITIALIZE,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }
    private State state = State.INITIALIZE;
    private CommunityNeoServer server;
    private final String host;
    private final int port;
    private final Log log;
    private final Client client;
    private final long aliveCheckPeriod;
    private final ServerBuilder serverBuilder;

    public Neo4jServerThread(Log mojoLog, String host, int port) {
        this(mojoLog, host, port, 1000);
    }

    public Neo4jServerThread(Log mojoLog, String host, int port, long aliveCheckPeriod) {
        super("neo4j-server-thread");
        super.setDaemon(true);
        this.log = mojoLog;
        this.host = host;
        this.port = port;
        this.aliveCheckPeriod = aliveCheckPeriod;
        client = Client.create();
        client.setFollowRedirects(false);
        log.info(String.format("Building Neo4j CommunityServer at: http://%s:%s/", host, port));
        serverBuilder = ServerBuilder.server().onHost(host).onPort(port);
    }

    public Neo4jServerThread useDatabaseDir(File databaseDir) {
        checkState(State.INITIALIZE);
        log.info(String.format("Neo4j CommunityServer will use database at: %s", databaseDir.getAbsolutePath()));
        serverBuilder.usingDatabaseDir(databaseDir.getAbsolutePath());
        return this;
    }

    public Neo4jServerThread withExtension(ServerExtension extension) {
        checkState(State.INITIALIZE);
        log.info(String.format("Neo4j CommunityServer will use extension with package: %s at mount point: %s", extension.getPackageName(), extension.getMountPoint()));
        serverBuilder.withThirdPartyJaxRsPackage(extension.getPackageName(), extension.getMountPoint());
        return this;
    }

    public Neo4jServerThread withProperty(String key, String value) {
        checkState(State.INITIALIZE);
        log.info(String.format("Neo4j CommunityServer will use property: %s = %s", key, value));
        serverBuilder.withProperty(key, value);
        return this;
    }

    public synchronized ExecutionResult populateDatabase(String cypherQuery) throws SyntaxException {
        checkState(State.RUNNING);
        log.info(String.format("Bootstrap of Neo4j CommunityServer with Cypher query:%n%s%n", cypherQuery));
        server.getDatabase().getIndexManager().getNodeAutoIndexer().setEnabled(true);
        server.getDatabase().getIndexManager().getNodeAutoIndexer().startAutoIndexingProperty("name");
        ExecutionEngine engine = new ExecutionEngine(server.getDatabase().getGraph(), StringLogger.SYSTEM);
        return engine.execute(cypherQuery);
    }

    @Override
    public synchronized void start() {
        checkState(State.INITIALIZE);
        this.state = State.STARTING;
        try {
            server = serverBuilder.build();
            log.info("Starting Neo4j CommunityServer");
            server.start();
            log.info("Neo4j CommunityServer started.");
            super.start();
            this.state = State.RUNNING;
        } catch (IOException ex) {
            log.error("cannot build Neo4j CommunityServer", ex);
        }
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        do {
            if (aliveCheckPeriod > 0) {
                try {
                    Thread.sleep(aliveCheckPeriod);
                } catch (InterruptedException ex) {
                    log.warn("Thread was interrupted: ", ex);
                }
            }
        } while (checkRunning() && checkAlive());
        synchronized (this) {
            this.state = State.STOPPED;
        }
        log.info("Neo4j CommunityServer stopped.");
    }

    private synchronized boolean checkAlive() {
        log.debug("check, if server is still alive...");
        return client.resource(String.format("http://%s:%s/", host, port)).get(ClientResponse.class).getStatus() >= 200;
    }

    private synchronized void checkState(State expected) {
        if (this.state != expected) {
            throw new IllegalStateException(String.format("Not in expected %s state. Current State is: %s", expected.name(), this.state));
        }
    }

    private synchronized boolean checkRunning() {
        return this.state == State.RUNNING;
    }

    public synchronized void shutdown() {
        checkState(State.RUNNING);
        if (server == null) {
            log.warn("Neo4j CommunityServer is not available. Already shut down?");
        } else {
            this.state = State.STOPPING;
            log.info("Stopping Neo4j CommunityServer");
            server.stop();
        }
    }
}
