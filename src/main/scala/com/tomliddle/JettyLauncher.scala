package com.tomliddle

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
 * Created by tom on 18/12/14.
 */
object JettyLauncher { // this is my entry object as specified in sbt project definition
def main(args: Array[String]) {
	val port = 8080

	val server = new Server(port)
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
