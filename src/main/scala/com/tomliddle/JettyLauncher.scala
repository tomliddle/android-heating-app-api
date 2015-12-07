package com.tomliddle

import org.eclipse.jetty.server.handler._
import org.eclipse.jetty.server._
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher {
	def main(args: Array[String]) {
		val port = 8080

		val threadPool = new QueuedThreadPool
		threadPool.setMaxThreads(20)
		val server = new Server(threadPool)

		// Extra options
		server.setDumpAfterStart(false)
		server.setDumpBeforeStop(false)
		server.setStopAtShutdown(true)

		// Context handler
		val context = new WebAppContext()
		context setContextPath "/"
		context.setResourceBase("src/main/webapp")
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
		context.addEventListener(new ScalatraListener)
		context.addServlet(classOf[DefaultServlet], "/")
		server.setHandler(context)

		// Server connector
		val http = new ServerConnector(server, new HttpConnectionFactory(new HttpConfiguration))
		http.setPort(port)
		http.setIdleTimeout(10000)
		server.addConnector(http)

		server.start
		server.join
	}
}
