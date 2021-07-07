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

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.json.JsonObject;
import javax.naming.InitialContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import ibm.testing.microservice.api.utils.ManageFailure;

@Path("/verifyCallStatus")
public class HookJobStatus {
	
	private static Logger log = Logger.getLogger(HookJobStatus.class.getName());
	
	@Resource(lookup="webhook/scheduledExecutor")
    private static ScheduledExecutorService executor;
	
	static {
		try {
			executor=(ScheduledExecutorService)new InitialContext().lookup("webhook/scheduledExecutor");
		} catch (Exception e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "An error occurred initializing the secondary service for managing failures",e);
			}
		}
	}

	/**
	 * This is a secondary endpoint in case something goes wrong in the primary SOE part of the microservice. It gets all the call backs and in case something goes wrong
	 * @param hook
	 */
	
	@POST
	@Path("")
	@RolesAllowed({"testerWebhook"})
	public Response verifyJobStatusWithCallHook(JsonObject hook) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, ""+hook);
		}
		if(!hook.isNull("failureReason")) {
			executor.submit(new ManageFailure(hook.getString("vgwSessionID"), hook.getString("failureReason")));
		}
		return Response.ok().build();
	}
	
}
