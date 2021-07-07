/* ***************************************************************** */
/*                                                                   */
/* Licensed Materials - Property of IBM                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2019. All Rights Reserved.                */
/*                                                                   */
/* US Government Users Restricted Rights - Use, duplication or       */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. */
/*                                                                   */
/* ***************************************************************** */
package ibm.testing.microservice.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Initializer implements ServletContextListener{
	
	private static Logger log = Logger.getLogger(Initializer.class.getName());
	

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Initializing context and version");
		}
		InputStream resourceAsStream = sce.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
		try {
			Manifest manifest = new Manifest(resourceAsStream);
			String version = manifest.getMainAttributes().getValue("Voice-Agent-Tester-Version");
			log.log(Level.INFO,Messages.getMessage("CWSAT0059I",version));
			
		} catch (IOException e) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Failed to read the version = " + e.toString());
			}			
		}
	}
	
}
