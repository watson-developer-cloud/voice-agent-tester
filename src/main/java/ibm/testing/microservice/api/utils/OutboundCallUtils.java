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

import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.naming.InitialContext;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ibm.testing.microservice.api.utils.CloudantUtils;
import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.CreateWorkerCallDefinition;
import ibm.testing.microservice.models.CreateWorkerTenantConfig;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.MessageRequest;

public class OutboundCallUtils {
	
	private static Logger log = Logger.getLogger(OutboundCallUtils.class.getName());
	private static String callerRestURL;
	private static String testerUri;
	private static String callerVoiceGatewayCredentials;
	private static String testerWebhookUsername;
	private static String testerWebhookPassword;
	private static final String outboundCallRestPath="vgw/outboundCalls/<tenantID>/startOutboundCall";
	private static boolean authenticated=false;
	private static final String testerEndpoint="voice-agent-tester/tester";
	private static final String webhookEndpoint="voice-agent-tester/verifyCallStatus";
	
	private static final String defaultPostResponseTimeout="15000";
	private static final Double defaultFirmupSilenceTime=1.6;
	private static final String defaultModelForSTT="en-US_NarrowbandModel";
	
	@Resource(lookup="concurrent/scheduledExecutor")
    private static ScheduledExecutorService executor;
	
	static {
		InitialContext ic;
		try {
			ic = new InitialContext();
			executor=(ScheduledExecutorService)ic.lookup("concurrent/scheduledExecutor");
			testerUri=ic.lookup("TESTER_WEBHOOK_URI").toString();
			if(CloudantUtils.isLookupNull(testerUri)) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "TESTER_WEBHOOK_URI was not specified. Trying to build from the container.");
				}
				Scanner scanner =new Scanner(new File("/etc/hostname"));
			    String host = scanner.next();
			    scanner.close();
				if(CloudantUtils.isLookupNull(host))
					if(log.isLoggable(Level.FINE)) {
						log.log(Level.FINE, "Host of the container could not be found.");
					}
				testerUri="http://"+host+":"+System.getenv("HTTP_PORT")+"/";
			}
			if(testerUri.charAt(testerUri.length()-1)!='/')
				testerUri+='/';
			callerRestURL=ic.lookup("CALLER_VOICE_GATEWAY_URI").toString();
			testerWebhookUsername=ic.lookup("TESTER_WEBHOOK_USERNAME").toString();
			testerWebhookPassword=ic.lookup("TESTER_WEBHOOK_PASSWORD").toString();
			if(CloudantUtils.isLookupNull(callerRestURL)) 
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0049E"));
			else {
				String restUsername= ic.lookup("CALLER_VOICE_GATEWAY_USERNAME").toString();
				String restPassword= ic.lookup("CALLER_VOICE_GATEWAY_PASSWORD").toString();
				if(!CloudantUtils.isLookupNull(restUsername) && !CloudantUtils.isLookupNull(restPassword)) {
					authenticated=true;
					callerVoiceGatewayCredentials=restUsername+":"+restPassword;
					callerVoiceGatewayCredentials=Base64.getEncoder().encodeToString(callerVoiceGatewayCredentials.getBytes());
				}
				if(callerRestURL.charAt(callerRestURL.length()-1)!='/')
					callerRestURL+='/';
				callerRestURL+=outboundCallRestPath;
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "Using endpoint for outbound calls "+callerRestURL+" and tester uri "+testerUri);
				}
				try {
					new URL(callerRestURL);
				}catch(Exception e) {
					log.log(Level.SEVERE,Messages.getMessage("CWSAT0050E"),e);
					callerRestURL=null;
				}
				try {
					new URL(testerUri);
				}catch(Exception e) {
					log.log(Level.SEVERE,Messages.getMessage("CWSAT0051E"),e);
					testerUri=null;
				}
			}
		} catch (Exception e) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Error initializing variables for outbound calls REST API.", e);
			}
		}
	}
	
	public static void startSingleCall(GetJob job,Map<String,Object> context) {
		if(context==null)
			executor.submit(new SingleCall(job));
		else
			executor.submit(new SingleCall(job, context));
	}
	
	
	public static void startInitialBatchCall(String masterId,GetWorker worker,Integer numberOfJobs, Integer jobsPerSecond) {
		if(jobsPerSecond==null)
			executor.submit(new BatchCalls(numberOfJobs, masterId, worker));
		else
			executor.submit(new BatchCalls(numberOfJobs, jobsPerSecond, masterId, worker));
	}
	
	public static void restartBatchCalls(BatchJob job,GetWorker worker, Integer jobsPerSecond) {
		if(jobsPerSecond==null)
			executor.submit(new BatchCalls(job, worker, BatchCalls.RESTART));
		else
			executor.submit(new BatchCalls(job, jobsPerSecond, worker, BatchCalls.RESTART));
	}
	
	public static void unpauseBatchCalls(BatchJob job,String workerId, Integer jobsPerSecond) {
		if(jobsPerSecond==null)
			executor.submit(new BatchCalls(job, new GetWorker().id(workerId), BatchCalls.UNPAUSE));
		else
			executor.submit(new BatchCalls(job, jobsPerSecond, new GetWorker().id(workerId), BatchCalls.UNPAUSE));
	}
	
	public static void startOutboundCall(GetJob job,Map<String,Object> context) {
		//Intialize outbound call
		if(callerRestURL==null)
			throw new ServerErrorException("Could not start outbound call. CALLER_VOICE_GATEWAY_URI was not defined.", 500);
		if(testerUri==null)
			throw new ServerErrorException("Could not start outbound call. TESTER_WEBHOOK_URI was not defined and could not be built from container.", 500);
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Got request to start outbound call for job "+job.getId(),job);
		}
		Builder outboundCallRequest=fillOutboundCallRequest(job.getWorkerUsed());
		String data=getDataForOutboundCall(job,context).toString();
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Starting outbound call for job "+job.getId()+" with JSON body "+data);
		}
//		CloudantUtils.updateJob(job); // Pretty sure this causes conflict errors
		//Start outbound call
		Response call=null;
		if(authenticated) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Using credentials to start outbound call for job "+job.getId(),callerVoiceGatewayCredentials);
			}
			call=outboundCallRequest.header(HttpHeaders.AUTHORIZATION, "Basic "+callerVoiceGatewayCredentials).post(Entity.entity(data, MediaType.APPLICATION_JSON));
		}
		else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Not using credentials to start outbound call for job "+job.getId());
			}
			call=outboundCallRequest.post(Entity.entity(data, MediaType.APPLICATION_JSON));
		}
		//Validate outbound call started
		if(call.getStatus()!=Response.Status.OK.getStatusCode()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Could not start outbound call for job "+job.getId()+" because something went wrong connecting to outbound call REST API.",call.getStatus()+":"+call.readEntity(String.class));
			}
			throw new ServerErrorException("Could not start outbound call. Something went wrong connecting to agent", 500);
		}
		JsonObject response = call.readEntity(JsonObject.class);
		if(!response.containsKey("msg")||!response.containsKey("vgwSessionID")||response.containsKey("failureReason")) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Could not start outbound call for job "+job.getId()+" because something went wrong connecting to outbound call REST API.",call.getStatus()+":"+response);
			}
			throw new ServerErrorException("Could not start outbound call. Something went wrong connecting to agent", 500);
		}
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Call "+response.getString("vgwSessionID")+" for job "+job.getId()+" has started");
		}
	}
	
	private static Builder fillOutboundCallRequest(GetWorker outline) {
		// Initialize client
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Initializing REST client to connect to outbound calls REST API");
		}
		Client cl = ClientBuilder.newClient();
		String restUrl = callerRestURL.replace("<tenantID>", outline.getCallDefinition().getTenantId());
		// Set target to a voice gateway instance
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Outbound calls REST API to request outbound call is "+restUrl);
		}
		WebTarget target=cl.target(restUrl);
		// Start building request for outbound call
		Builder startOutboundCall = target.request(MediaType.APPLICATION_JSON);
		return startOutboundCall;
	}
	
	private static JsonObject getDataForOutboundCall(GetJob job,Map<String,Object> startingContext) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Constructing outbound call JSON body from workers call definition");
		}
		CreateWorkerCallDefinition callDefinition=job.getWorkerUsed().getCallDefinition();
		//Add the specified required variables "to" and "from" for outbound call
		JsonObjectBuilder json= Json.createObjectBuilder()
				.add("to", callDefinition.getTo())
				.add("from", callDefinition.getFrom());
		json.add("statusWebhook", testerUri+webhookEndpoint);
		json.add("statusWebhookUsername", testerWebhookUsername);
		json.add("statusWebhookPassword", testerWebhookPassword);
		//Add db id for the job to the initial context
		JsonObjectBuilder context;
		if(startingContext==null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Constructing initial context for outbound call");
			}
			context=constructStartingContext(job);
		}
		else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Reusing provided context for outbound call");
			}
			if(!startingContext.containsKey(MessageRequest.STARTING_OUTBOUND_CALL)) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Adding initial outbound call REST start");
				}
				startingContext.put(MessageRequest.STARTING_OUTBOUND_CALL, "Yes");
			}
			context=Json.createObjectBuilder(startingContext);
		}
		JsonObjectBuilder tenantConfig;
		if(job.getWorkerUsed().getCallDefinition().getTenantConfig()!=null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Parsing tenantConfig provided in worker");
			}
			tenantConfig=parseTenantConfig(job.getWorkerUsed());
		}
		else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "No tenant config provided in worker. Adding default tenant config");
			}
			tenantConfig=createDefaultTenantConfig();
		}
		json.add("tenantConfig", tenantConfig);
		json.add("context", context);
		if(callDefinition.getRoute()!=null && !callDefinition.getRoute().isEmpty()) {
			StringBuilder route=new StringBuilder();
			for(int i=0;i<callDefinition.getRoute().size();i++) {
				route.append(callDefinition.getRoute().get(i)+((i<callDefinition.getRoute().size()-1)?",":""));
			}
			json.add("route", route.toString());
		}
		//Return data
		return json.build();
	}
	
	private static JsonObjectBuilder constructStartingContext(GetJob job) {		
		JsonObjectBuilder context= Json.createObjectBuilder()
				.add(MessageRequest.ID,job.getWorkerUsed().getId()+"_"+job.getId())
				.add(MessageRequest.STARTING_OUTBOUND_CALL, "Yes");
		if(job.getWorkerUsed().getIterations()!=0)
			context.add(MessageRequest.PERCENT_PER_CASE,100.0/(job.getWorkerUsed().getCases().size()*job.getWorkerUsed().getIterations()));
		return context;
	}
	
	private static JsonObjectBuilder parseTenantConfig(GetWorker outline) {
		if(CloudantUtils.isLookupNull(testerUri)) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "TESTER_WEBHOOK_URI was not specified and could not be built from container.");
			}
			throw new ServerErrorException("TESTER_WEBHOOK_URI was not defined and could not be built from container.",500);
		}
		if(!(outline.getCallDefinition().getTenantConfig().getConfig() instanceof Map)) {
			log.log(Level.INFO,Messages.getMessage("CWSAT0052W",outline.getId()));
			outline.getCallDefinition().setTenantConfig(new CreateWorkerTenantConfig());
			JsonObjectBuilder data = createDefaultTenantConfig();
			return data;
		}
		else {
			// Create new map so that we don't affect what was in the worker used by job
			Map<String,Object> config=new LinkedHashMap<String, Object>((Map<String, Object>) outline.getCallDefinition().getTenantConfig().getConfig());
			parseConversation(config);
			if(!config.containsKey("postResponseTimeout")) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Adding default postResponseTimeout "+defaultPostResponseTimeout);
				}
				config.put("postResponseTimeout", defaultPostResponseTimeout);
			}
			parseSTT(config);
			Map<String,Object> result= new HashMap<String, Object>();
			result.put("config", config);
			result.put("updateMethod",outline.getCallDefinition().getTenantConfig().getUpdateMethod());
			return Json.createObjectBuilder(result);
		}
	}
	
	private static void parseConversation(Map<String,Object> config) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Parsing and managing conversation and setting URL, user and password");
		}
		if(config.containsKey("conversation")) {
			if(config.get("conversation") instanceof Map) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Found valid conversation object. Adding URL, user and pass to it");
				}
				Map<String,Object> conversation=(Map<String, Object>) config.get("conversation");
				conversation.put("url", testerUri+testerEndpoint);
				conversation.put("username", testerWebhookUsername);
				conversation.put("password", testerWebhookPassword);
				config.put("conversation", conversation);
			}else {
				Map<String,Object> conversation=new HashMap<String, Object>();
				conversation.put("url", testerUri+testerEndpoint);
				conversation.put("username", testerWebhookUsername);
				conversation.put("password", testerWebhookPassword);
				config.put("conversation", conversation);
			}
		}else {
			Map<String,Object> conversation=new HashMap<String, Object>();
			conversation.put("url", testerUri+testerEndpoint);
			conversation.put("username", testerWebhookUsername);
			conversation.put("password", testerWebhookPassword);
			config.put("conversation", conversation);
		}
	}
	
	private static void parseSTT(Map<String,Object> config) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Parsing and managing STT for adding defaults");
		}
		if(config.containsKey("stt")) {
			if(config.get("stt") instanceof Map) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Found valid STT object. Checking for config");
				}
				Map<String,Object> stt=(Map<String, Object>) config.get("stt");
				if(stt.containsKey("config") && (stt.get("config") instanceof Map)) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Found valid config object in STT. Checking for defaults");
					}
					Map<String,Object> sttConfig=(Map<String, Object>) stt.get("config");
					if(!sttConfig.containsKey("firmup_silence_time") && !sttConfig.containsKey("model")) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Adding default firmupTime "+defaultFirmupSilenceTime+" and default model "+defaultModelForSTT);
						}
						sttConfig.put("firmup_silence_time", defaultFirmupSilenceTime);
						sttConfig.put("model", defaultModelForSTT);
						config.put("stt", stt);
					}
				}else {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Adding default firmupTime "+defaultFirmupSilenceTime+" and default model "+defaultModelForSTT);
					}
					Map<String,Object> sttConfig=new HashMap<String, Object>();
					sttConfig.put("firmup_silence_time", defaultFirmupSilenceTime);
					sttConfig.put("model", defaultModelForSTT);
					stt.put("config", sttConfig);
					config.put("stt", stt);
				}
			}else {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Adding default firmupTime "+defaultFirmupSilenceTime+" and default model "+defaultModelForSTT);
				}
				Map<String,Object> stt=new HashMap<String, Object>();
				Map<String,Object> sttConfig=new HashMap<String, Object>();
				sttConfig.put("firmup_silence_time", defaultFirmupSilenceTime);
				sttConfig.put("model", defaultModelForSTT);
				stt.put("config", sttConfig);
				config.put("stt", stt);
			}
		}else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Adding default firmupTime "+defaultFirmupSilenceTime+" and default model "+defaultModelForSTT);
			}
			Map<String,Object> stt=new HashMap<String, Object>();
			Map<String,Object> sttConfig=new HashMap<String, Object>();
			sttConfig.put("firmup_silence_time", defaultFirmupSilenceTime);
			sttConfig.put("model", defaultModelForSTT);
			stt.put("config", sttConfig);
			config.put("stt", stt);
		}
	}
	
	private static JsonObjectBuilder createDefaultTenantConfig() {
		Map<String,Object> conversation=new HashMap<String, Object>();
		conversation.put("url", testerUri+testerEndpoint);
		conversation.put("username", testerWebhookUsername);
		conversation.put("password", testerWebhookPassword);
		Map<String,Object> stt=new HashMap<String, Object>();
		Map<String,Object> sttConfig=new HashMap<String, Object>();
		sttConfig.put("firmup_silence_time", defaultFirmupSilenceTime);
		sttConfig.put("model", defaultModelForSTT);
		stt.put("config", sttConfig);
		Map<String,Object> config=new HashMap<String, Object>();
		config.put("postResponseTimeout", defaultPostResponseTimeout);
		config.put("conversation", conversation);
		config.put("stt", stt);
		Map<String,Object>result= new HashMap<String, Object>();
		result.put("config", config);
		return Json.createObjectBuilder(result);
	}
	
}
