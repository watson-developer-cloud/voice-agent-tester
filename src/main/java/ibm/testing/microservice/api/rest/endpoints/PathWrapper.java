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
package ibm.testing.microservice.api.rest.endpoints;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

@ApplicationPath("/")
@OpenAPIDefinition(
		tags = {
				@Tag(name="Test Case", description="Paths to create, read, modify and delete test cases for testing voice agents. Test Cases contain the outline of what we desire to test."),
				@Tag(name="Worker", description="Paths to create, read, modify and delete workers for testing voice agents. Workers contain information about tests and are in charge of creating and managing jobs."),
				@Tag(name = "Job", description = "Paths to create, read, modify and delete jobs for testing voice agents. Jobs are the actual executions of the worker."),
				@Tag(name = "Batch Job", description = "Paths to create, read, modify and delete batch jobs for testing voice agents. Batch Jobs start and manage multiple jobs.")
		},
	    info = @Info(
	        title = "Voice Agent Tester", 
	        version = "", 
	        description = "An api for running tests against Voice Agents and Voice Gateway installations"
//	        termsOfService = "http://jsdjcd",
//	        contact = @Contact(email = "wecqqcq@sdc.com"),
//	        license = @License(
//	            name = "License",
//	            url = "http://qfq"
//	        )
	    )
	)
@SecuritySchemes(
		value = {				
				@SecurityScheme(
						securitySchemeName = "AgentTesterBasicHttp",
						type = SecuritySchemeType.HTTP,
						description = "Basic http authentication to access API calls",
						scheme = "basic"
						)
		}
		)
public class PathWrapper extends Application{
	
}
