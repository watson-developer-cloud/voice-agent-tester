/*
 * (C) Copyright IBM Corp. 2019, 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
