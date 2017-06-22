/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.embeddedserver.jetty

import io.javalin.Javalin
import io.javalin.embeddedserver.EmbeddedServer
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.session.SessionHandler
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ServerSocket

class EmbeddedJettyServer(private var server: Server, private val javalinHandler: SessionHandler) : EmbeddedServer {

    private val log = LoggerFactory.getLogger(EmbeddedServer::class.java)

    @Throws(Exception::class)
    override fun start(host: String, port: Int): Int {

        if (server.connectors.isEmpty()) {
            server.connectors = arrayOf<Connector>(ServerConnector(server).apply {
                this.host = host
                this.port = port
            })
        }

        server.handler = javalinHandler
        server.start()

        log.info("Javalin has started \\o/")

        for (connector in server.connectors) {
            log.info("Localhost: " + getProtocol(connector) + "://localhost:" + (connector as ServerConnector).localPort)
        }

        return (server.connectors[0] as ServerConnector).localPort
    }

    @Throws(InterruptedException::class)
    override fun join() = server.join()

    override fun stop() {
        log.info("Stopping Javalin ...")
        try {
            server.stop()
        } catch (e: Exception) {
            log.error("Javalin failed to stop gracefully", e)
        }
        log.info("Javalin has stopped")
    }

    override fun activeThreadCount(): Int = server.threadPool.threads - server.threadPool.idleThreads

    override fun attribute(key: String): Any = server.getAttribute(key)

    private fun getProtocol(connector: Connector): String = if (connector.protocols.contains("ssl")) "https" else "http"

}