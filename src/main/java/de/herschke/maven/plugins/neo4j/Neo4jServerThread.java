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
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.helpers.ServerBuilder;

/**
 * represents a deamon thread that starts a neo4j server
 *
 * @author rhk
 */
public class Neo4jServerThread extends Thread {

    private CommunityNeoServer server;
    private final String host;
    private final int port;
    private final File databaseDir;
    private final Log log;
    private final Client client;
    private final long aliveCheckPeriod;
    private boolean running;

    public Neo4jServerThread(Log mojoLog, String host, int port) {
        this(mojoLog, host, port, 1000, null);
    }

    public Neo4jServerThread(Log mojoLog, String host, int port, long aliveCheckPeriod, File databaseDir) {
        super("neo4j-server-thread");
        super.setDaemon(true);
        this.log = mojoLog;
        this.host = host;
        this.port = port;
        this.databaseDir = databaseDir;
        this.aliveCheckPeriod = aliveCheckPeriod;
        client = Client.create();
        client.setFollowRedirects(false);
    }

    @Override
    public synchronized void start() {
        try {
            log.info(String.format("Building Neo4j CommunityServer at: http://%s:%s/", host, port));
            ServerBuilder sb = ServerBuilder.server().onHost(host).onPort(port);
            if (databaseDir != null) {
                log.info(String.format("Neo4j CommunityServer will use database at: %s", databaseDir.getAbsolutePath()));
                sb.usingDatabaseDir(databaseDir.getAbsolutePath());
            }
            server = sb.build();
            log.info("Starting Neo4j CommunityServer");
            server.start();
            log.info("Neo4j CommunityServer started.");
            this.running = true;
            super.start();
        } catch (IOException ex) {
            log.error("cannot build Neo4j CommunityServer", ex);
        }
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        do {
            try {
                Thread.sleep(aliveCheckPeriod);
            } catch (InterruptedException ex) {
                log.warn("Thread was interrupted: ", ex);
            }
        } while (checkRunning() && checkAlive());
    }

    private synchronized boolean checkAlive() {
        log.debug("check, if server is still alive...");
        return client.resource(String.format("http://%s:%s/", host, port)).get(ClientResponse.class).getStatus() >= 200;
    }

    private synchronized boolean checkRunning() {
        return running;
    }

    public synchronized void shutdown() {
        running = false;
        if (server == null) {
            log.warn("Neo4j CommunityServer is not available. Already shut down?");
        } else {
            log.info("Stopping Neo4j CommunityServer");
            server.stop();
            log.info("Neo4j CommunityServer stopped.");
        }
    }
}
