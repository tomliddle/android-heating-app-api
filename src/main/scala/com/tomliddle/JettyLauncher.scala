package com.tomliddle

import org.eclipse.jetty.server._
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
	* Embedded jetty launcher
	*/
object JettyLauncher {
	def main(args: Array[String]) {
		val port = 8080

		val server = new Server(port)

		// Context handler
		val context = new WebAppContext()
		context setContextPath "/"
		context.setResourceBase("src/main/webapp")
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
		context.addEventListener(new ScalatraListener)
		context.addServlet(classOf[DefaultServlet], "/")
		server.setHandler(context)

		server.start
		server.join
	}
}
