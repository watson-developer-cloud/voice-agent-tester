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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import ibm.testing.microservice.api.utils.CloudantUtils;
import ibm.testing.microservice.api.utils.Messages;
import ibm.testing.microservice.models.Command;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetJobResultsFailures;
import ibm.testing.microservice.models.GetTestCase;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.MessageRequest;
import ibm.testing.microservice.models.MessageResponse;
import ibm.testing.microservice.api.utils.OutboundCallUtils;
import ibm.testing.microservice.models.ReceiveType;
import ibm.testing.microservice.models.SendType;

@Path("/tester")
public class TestingLogic {

	private static Logger log = Logger.getLogger(TestingLogic.class.getName());

	@POST
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@RolesAllowed({"testerWebhook"})
	public Response testerSteps(MessageRequest request) {
			
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, ""+request);
		}
		
		//Check if a dependency for the test was missing/deleted
		if(request.isMissingDependency()) {
			//If already hangup send the ok
			if(request.isHangUp()) {
				log.log(Level.WARNING,Messages.getMessage("CWSAT0054W",new String[] {request.getCallSessionId(),request.getJobId()}));
				return Response.ok().build();
			}
			//Else hangup the call
			return hangUpCall(request,MessageResponse.constructEmptyResponse(request)); 
		}
		
		
		//Get the job from either the db or the cache
		GetJob job = null;
		try {
			job = CloudantUtils.getJob(request.getWorkerId(), request.getJobId());
		} catch (Exception e) {
			//Job not found so stop call
			log.log(Level.SEVERE,Messages.getMessage("CWSAT0053E",new String[] {request.getJobId(),request.getWorkerId(),request.getCallSessionId()}));
			//Set missing dependency
			request.setMissingDependency();
			//Hangup call
			return hangUpCall(request,MessageResponse.constructEmptyResponse(request));
		}
		
		// See if starting outbound call and send initial empty greeting and add data for call in job 
		if (request.isStartOutboundCall() && job.getCallMetadata()==null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+job.getId()+" is on start outbound call so updating call session to new session id "+request.getCallSessionId());
			}
			//Set current call metadata
			job.setCallMetadata(request.getCallSessionId());
			// Get current test case from database and add to the job
			GetTestCase testCase = null;
			String currentCaseId=job.getWorkerUsed().getCases().get(job.getCaseNumber()).getCaseId();
			try {
				testCase = CloudantUtils.getTestCase(currentCaseId);
				// Update the current test case and if it was modified during run. The first time we always get the test case from the database and add it to the job and then use it from the job
				job.updateCurrentCase(testCase);
			} catch (Exception e) {
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0055E",new String[] {request.getJobId(),request.getCallSessionId(),currentCaseId}));
				job.setJobInvalid();
				job.manageMissingTestCase(currentCaseId);
				updateJob(job,false);
				request.setMissingDependency();
				// Gateway doesn't hang up on the initial turn
				return Response.ok(MessageResponse.constructEmptyResponse(request)).build();
			}
			if (job.isJobCreated()) {
				job.setJobStarting();
				if(!updateJob(job,true)) {
					job.setJobInvalid();
					updateJob(job,false);
					request.setMissingDependency();
					return hangUpCall(request,MessageResponse.constructEmptyResponse(request));
				}
			} else if (!job.isJobValidForRunning()) {
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0056E",new String[] {request.getCallSessionId(),job.getId(),job.getStatus()}));
				job.setJobInvalid();
				if(!updateJob(job,false)) {
					request.setMissingDependency();
					return hangUpCall(request,MessageResponse.constructEmptyResponse(request));
				}
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			} else {
				updateJob(job,true);
			}
			if(CloudantUtils.getCache()!=null)
				CloudantUtils.getCache().updateJobOnCall(job, request.getCallSessionId());
			return Response.ok(MessageResponse.constructEmptyResponse(request)).build();
		} 
		
		if(job.getCallMetadata()!=null && !job.getCallMetadata().equals(request.getCallSessionId())) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+job.getId()+" was requested for call "+request.getCallSessionId()+" but did not match call sessions. Sending error to Voice Gateway");
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}else if(job.getCallMetadata()==null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+job.getId()+" was requested for call "+request.getCallSessionId()+" but job did not contain any session id. Job callmetadata was null");
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}
		
		// See if its hangup then update resources accordingly
		if (request.isHangUp()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" is hanging up");
			}
			// Job changed states
			if (job.isJobStopping()||job.isJobReStarting()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" hanging up and was manually interrupted. A request to STOP or START the job was made.",job);
				}
				manageJobStatus(request, job);
			} else if(job.isJobValidForRunning())
				updateResources(request, job);
			else {
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0056E",new String[] {request.getCallSessionId(),job.getId(),job.getStatus()}));
				job.setJobInvalid();
				updateJob(job,false);
			}
			return Response.ok().build();
		}
		// Verify job status
		else if (!job.isJobRunning()) {
			// Check if we want to stop the job
			if(job.isJobStarting()) {
				job.setJobRunning();
			}
			else if (job.isJobStopping()) {
				// Terminate currently running test and stop job
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" was running and was manually interrupted. A request to STOP the job was made.",job);
				}
				return hangUpCall(request);
			} else if (job.isJobReStarting()) {
				// Terminate currently running test and restart job
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" was running and was manually interrupted. A request to START the job was made.",job);
				}
				return hangUpCall(request);
			} else if (!job.isJobPausing()) {
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0056E",new String[] {request.getCallSessionId(),job.getId(),job.getStatus()}));
				job.setJobInvalid();
				if(!updateJob(job,false)) {
					request.setMissingDependency();
					return hangUpCall(request,MessageResponse.constructEmptyResponse(request));
				}
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}

		// Get current test case
		GetTestCase testCase = job.getCurrentCase();
		if(testCase==null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" had current test case null but was not starting outbound call. Sending not acceptable to VGW");
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}

		//Validate turn
		ValidationWrapper receivedValidation = validateMessageReceived(request, testCase, job);
		if (receivedValidation.isPassed()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Turn approved for job "+job.getId()+" on call "+request.getCallSessionId()+". Got "+request+" and expected "+testCase.getTurns().get(job.getTurnNumber()).getReceive(),job);
			}
			// Check if next turn will be bigger than the array which will
			// mean our test has ended and we need to hangup call
			if (job.getTurnNumber() >= testCase.getTurns().size() - 1) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+job.getId()+" running on test case "+testCase.getId()+" completed sucessfully",job);
				}
				// Job ended everything went accordingly so hang up
				request.setCompletedSucesfully();
				return hangUpCall(request, buildResponse(request, testCase, job));
			} else {
				// Build message response from send in test case
				MessageResponse response = buildResponse(request, testCase, job);
				// Move to other turn number
				job = job.turnNumber(job.getTurnNumber() + 1);
				if(!updateJob(job,true)) {
					request.setMissingDependency();
					return hangUpCall(request,MessageResponse.constructEmptyResponse(request));
				}
				return Response.ok(response).build();
			}
		} else {
			// Construct failure from GetJobResultsFailures
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Turn failed for job "+job.getId()+" on call "+request.getCallSessionId()+". Got "+request+" and expected "+testCase.getTurns().get(job.getTurnNumber()).getReceive()+" Error found validating: "+receivedValidation.getError(),job);
			}			
			
			GetJobResultsFailures failure = new GetJobResultsFailures().time(Instant.now().toEpochMilli())
					.error(receivedValidation.getError()).testCaseID(testCase.getId()).testCaseName(testCase.getName())
					.turnNumber(job.getTurnNumber()).iteration(job.getIterationNumber())
					.sipCallID(request.getCallSessionId());
			// Add failure to context to update job when hang up comes
			request.setTestFailed(failure);
			return hangUpCall(request);
		}
	}

	// Manage status if call was interrupted
	private void manageJobStatus(MessageRequest request, GetJob job) {
		if (job.isJobStopping()) {
			job.reset();
			job.setJobStopped();
			updateJob(job,false);
		} else if (job.isJobReStarting()) {
			job.reset();
			job.setJobStarting();
			if(updateJob(job,true))
				startOtherCall(job.getWorkerUsed(), job, request);
		} else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+job.getId()+" running on call "+request.getCallSessionId()+" was manually interrupted but was found with an unexpected state. Setting as invalid",job);
			}
			job.setJobInvalid();
			updateJob(job,false);
		}
	}

	// Hangup call with empty text
	private Response hangUpCall(MessageRequest request) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+request.getJobId()+" is hanging up");
		}
		MessageResponse response = new MessageResponse(request);
		response = response.setUpHangUp();
		return Response.ok(response).build();
	}

	// Hangup call with what the last turn had
	private Response hangUpCall(MessageRequest request, MessageResponse response) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Call "+request.getCallSessionId()+" regarding job "+request.getJobId()+" is hanging up");
		}
		response = response.setUpHangUp();
		return Response.ok(response).build();
	}

	// Update job depending of its current status
	private void updateResources(MessageRequest request, GetJob job) {
		// Get worker outline thats being used
		GetWorker worker = job.getWorkerUsed();
		// Get current test case
		GetTestCase testCase = job.getCurrentCase();
		
		// Verify if the test failed
		if (request.isTestFailed()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+job.getId()+" on call "+request.getCallSessionId()+" failed on test case "+testCase.getId()+" on turn "+job.getTurnNumber()+" so adding failure "+request.getFailureReason());
			}
			GetJobResultsFailures failure = request.getFailureReason();
			job.addResultFailure(failure);
			if (worker.getFailuresToIgnore() != null
					&& job.getResults().getNumberOfFailures() > worker.getFailuresToIgnore()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" exceeded failures to ignore. Marking job as failed");
				}
				job.setJobFailed();
				updateJob(job,false);
				// Job failed so remove callsession from cache
				if(CloudantUtils.getCache()!=null)
					CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
				return;
			}
		}
		//See if we are on the last turn and validate it
		else if(!request.isCompletedSucesfully()&&job.getTurnNumber() == testCase.getTurns().size()-1) {
			ValidationWrapper lastValidation = validateMessageReceived(request, testCase, job);
			request.setCompletedSucesfully();
			if(lastValidation.isPassed()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Turn approved for job "+job.getId()+" on call "+request.getCallSessionId()+". Got "+request+" and expected "+testCase.getTurns().get(job.getTurnNumber()).getReceive(),job);
				}
			} else {
				// Construct failure from GetJobResultsFailures
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Turn failed for job "+job.getId()+" on call "+request.getCallSessionId()+". Got "+request+" and expected "+testCase.getTurns().get(job.getTurnNumber()).getReceive()+" Error found validating: "+lastValidation.getError(),job);
				}
				GetJobResultsFailures failure = new GetJobResultsFailures().time(Instant.now().toEpochMilli())
						.error(lastValidation.getError()).testCaseID(testCase.getId()).testCaseName(testCase.getName())
						.turnNumber(job.getTurnNumber()).iteration(job.getIterationNumber())
						.sipCallID(request.getCallSessionId());
				job.addResultFailure(failure);
				if (worker.getFailuresToIgnore() != null
						&& job.getResults().getNumberOfFailures() > worker.getFailuresToIgnore()) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" exceeded failures to ignore. Marking job as failed");
					}
					job.setJobFailed();
					updateJob(job,false);
					// Job failed so remove callsession from cache
					if(CloudantUtils.getCache()!=null)
						CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
					return;
				}
			}
		}
		
		// See if we were expecting more turns in the test case and mark as failure
		if (!request.isTestFailed()&&!request.isCompletedSucesfully()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" hanged up  but did not finish conversation as stated by test case "+testCase.getId()+". Adding as failure");
			}
			GetJobResultsFailures failure = new GetJobResultsFailures().time(Instant.now().toEpochMilli()).error(
					"Call for job: "+job.getId()+" hanged up but did not finish conversation as stated by test case: "+testCase.getId()+". Got this as hangup reason: "+request.getHangUpReason())
					.testCaseID(testCase.getId()).testCaseName(testCase.getName()).turnNumber(job.getTurnNumber())
					.iteration(job.getIterationNumber()).sipCallID((String) request.getContext().get("vgwSessionID"));
			job.addResultFailure(failure);
			if (worker.getFailuresToIgnore() != null
					&& job.getResults().getNumberOfFailures() > worker.getFailuresToIgnore()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" exceeded failures to ignore. Marking job as failed");
				}
				job.setJobFailed();
				updateJob(job,false);
				// Job failed so remove callsession from cache
				if(CloudantUtils.getCache()!=null)
					CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
				return;
			}
		}
		
		// Get the index of current test case
		int indexOfCurrentTestCase = job.getCaseNumber();
		// Check if at the end of test cases array of worker
		if (indexOfCurrentTestCase >= worker.getCases().size() - 1) {
			// If end of last iteration stop everything and update to completed
			if (worker.getIterations() != 0 && worker.getIterations() <= job.getIterationNumber()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" completed all iterations. Marking job as completed");
				}
				job.setJobCompleted();
				if(!updateJob(job,false))
					return;
				// Job completed so remove call session from cache
				if(CloudantUtils.getCache()!=null)
					CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
			}
			// Else move to new iteration and start from first case
			else {
				if (job.getPercentComplete() == null)
					job.goToNewIteration();
				else
					job.goToNewIteration(request.getPercentPerCase());
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" moving to new iteration");
				}
				if (job.isJobPausing()) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" is being paused at test case "+testCase.getId()+" on iteration "+job.getIterationNumber());
					}
					job.setJobPaused();
					updateJob(job,false);
					// Job paused so remove call session from cache
					if(CloudantUtils.getCache()!=null)
						CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
				}else {
					job.setCallMetadata(null);
					updateJob(job,true);
					startOtherCall(worker, job, request);
				}
			}
		} else {
			if (job.getPercentComplete() == null)
				job.goToNewTestCase();
			else
				job.goToNewTestCase(request.getPercentPerCase());
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" moving to test case "+testCase.getId());
			}
			if (job.isJobPausing()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Job "+request.getJobId()+" on call "+request.getCallSessionId()+" is being paused at test case "+testCase.getId()+" on iteration "+job.getIterationNumber());
				}
				job.setJobPaused();
				updateJob(job,false);
				// Job paused so remove call session from cache
				if(CloudantUtils.getCache()!=null)
					CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
			}else {
				job.setCallMetadata(null);
				updateJob(job,true);
				startOtherCall(worker, job, request);
			}
		}
	}
	
	// Wrapping method for updating and managing job if failure
	private boolean updateJob(GetJob job,boolean onlyOnCache) {
		try {
			CloudantUtils.updateJob(job,onlyOnCache);
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e);
			return false;
		}
	}
	
	// Wrapping method for starting another call for another test case
	private void startOtherCall(GetWorker worker,GetJob job,MessageRequest request) {
		// New call is starting so removing previous call session from cache
		if(CloudantUtils.getCache()!=null)
			CloudantUtils.getCache().updateJobOnCall(null, request.getCallSessionId());
		OutboundCallUtils.startSingleCall(job, request.getServiceOrientedContext());
	}

	// Principal method for validating turns
	private ValidationWrapper validateMessageReceived(MessageRequest request, GetTestCase testCase, GetJob job) {
		// Get expected from test case
		ReceiveType turn = testCase.getTurns().get(job.getTurnNumber()).getReceive();
		String type = turn.getType().toString();
		// Validate according to type
		switch (type) {
		case "string":
			return validateString(request.getInputText(), (String) turn.getValue());
		case "substring":
			return validateSubstring(request.getInputText(), (String) turn.getValue());
		case "regex":
			return validateRegex(request.getInputText(), (String) turn.getValue());
		case "context":
			return validateContext(request.getContext(), (Map<String, Object>) turn.getValue());
		case "andList":
			List<ReceiveType> val=parseReceiveList(turn,testCase.getId());
			if(val==null)
				return new ValidationWrapper(false,"Problem parsing andList from test case: "+testCase.getId()+". Your definition could need to be changed. ");
			return validateAndList(request, val,testCase.getId());
		case "orList":
			val=parseReceiveList(turn,testCase.getId());
			if(val==null)
				return new ValidationWrapper(false,"Problem parsing orList from test case: "+testCase.getId()+". Your definition could need to be changed. ");
			return validateOrList(request, val,testCase.getId());
		}
		return new ValidationWrapper(false,
				"Something wrong occured validating. Type: " + type + " was not matched to an accepted type. ");
	}
	
	// To parse the receive list from the test case
	private List<ReceiveType> parseReceiveList(ReceiveType receive,String testCaseId){
		Jsonb mapper=JsonbBuilder.create();
		try {
			return JsonbBuilder.create().fromJson(mapper.toJson(receive.getValue()), new ArrayList<ReceiveType>() {}.getClass().getGenericSuperclass());
		}catch(Exception e) {
			log.log(Level.SEVERE,Messages.getMessage("CWSAT0058E",testCaseId),e);
			return null;
		}
	}

	private ValidationWrapper validateString(String received, String expected) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type string");
		}
		if (!received.equalsIgnoreCase(expected))
			return new ValidationWrapper(
					"Validation Error for type string: Expected: '" + expected + "' but Received: '" + received + "' ");
		return new ValidationWrapper(true);
	}

	private ValidationWrapper validateSubstring(String received, String substring) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type substring");
		}
		if (!received.contains(substring))
			return new ValidationWrapper("Validation Error for type substring: Expected string to contain: '"
					+ substring + "' but Received string: '" + received + "' ");
		return new ValidationWrapper(true);
	}

	private ValidationWrapper validateRegex(String received, String regex) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type regex");
		}
		if (!received.matches(regex))
			return new ValidationWrapper("Validation Error for type regex: Expected string to match: '" + regex
					+ "' but Received string: '" + received + "' ");
		return new ValidationWrapper(true);
	}

	private ValidationWrapper validateContext(Map<String, Object> contextReceived,
			Map<String, Object> contextExpected) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type context");
		}
		StringBuilder sb = new StringBuilder();
		for (String content : contextExpected.keySet()) {
			if (!contextReceived.containsKey(content))
				sb.append("Validation Error for type context: Expected context to contain key: " + content+" ");
			else if (!contextReceived.get(content).equals(contextExpected.get(content)))
				sb.append("Validation Error for type context: Expected context to contain value for key " + content
						+ ": " + contextExpected.get(content) + " but Received: " + contextReceived.get(content)+" ");
		}
		if (sb.toString().isEmpty())
			return new ValidationWrapper(true);
		return new ValidationWrapper(false, sb.toString());
	}

	private ValidationWrapper validateAndList(MessageRequest received, List<ReceiveType> expected, String testcaseId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type andList");
		}
		ValidationWrapper vw = null;
		for (ReceiveType receive : expected) {
			switch (receive.getType().toString()) {
			case "string":
				vw = validateString(received.getInputText(), (String) receive.getValue());
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			case "substring":
				vw = validateSubstring(received.getInputText(), (String) receive.getValue());
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			case "regex":
				vw = validateRegex(received.getInputText(), (String) receive.getValue());
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			case "context":
				vw = validateContext(received.getContext(), (Map<String, Object>) receive.getValue());
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			case "andList":
				List<ReceiveType> val=parseReceiveList(receive,testcaseId);
				if(val==null)
					return new ValidationWrapper(false,"Problem parsing andList. Your definition could need to be changed. ");
				vw = validateAndList(received, val,testcaseId);
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			case "orList":
				val=parseReceiveList(receive,testcaseId);
				if(val==null)
					return new ValidationWrapper(false,"Problem parsing andList. Your definition could need to be changed. ");
				vw = validateOrList(received, val,testcaseId);
				if (!vw.isPassed())
					return vw.prependToError("Validation Error for type andList:");
				break;
			}
		}
		return new ValidationWrapper(true);
	}

	private ValidationWrapper validateOrList(MessageRequest received, List<ReceiveType> expected, String testcaseId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating for type orList");
		}
		ValidationWrapper vw = null;
		for (ReceiveType receive : expected) {
			switch (receive.getType().toString()) {
			case "string":
				vw = validateString(received.getInputText(), (String) receive.getValue());
				if (vw.isPassed())
					return vw;
				break;
			case "substring":
				vw = validateSubstring(received.getInputText(), (String) receive.getValue());
				if (vw.isPassed())
					return vw;
				break;
			case "regex":
				vw = validateRegex(received.getInputText(), (String) receive.getValue());
				if (vw.isPassed())
					return vw;
				break;
			case "context":
				vw = validateContext(received.getContext(), (Map<String, Object>) receive.getValue());
				if (vw.isPassed())
					return vw;
				break;
			case "andList":
				List<ReceiveType> val=parseReceiveList(receive,testcaseId);
				if(val==null)
					return new ValidationWrapper(false,"Problem parsing andList. Your definition could need to be changed. ");
				vw = validateAndList(received, val,testcaseId);
				if (vw.isPassed())
					return vw;
				break;
			case "orList":
				val=parseReceiveList(receive,testcaseId);
				if(val==null)
					return new ValidationWrapper(false,"Problem parsing orList. Your definition could need to be changed. ");
				vw = validateOrList(received, val,testcaseId);
				if (vw.isPassed())
					return vw;
				break;
			}
		}
		return new ValidationWrapper(false,"Validation Error for type orList: None of the receive types expected matched what was received.");
	}

	private MessageResponse buildResponse(MessageRequest request, GetTestCase testCase, GetJob job) {
		MessageResponse response = new MessageResponse(request);
		// Get expected from test case
		SendType turn = testCase.getTurns().get(job.getTurnNumber()).getSend();
		String type = turn.getType().toString();
		// Validate according to type
		switch (type) {
		case "string":
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Building response for type string");
			}
			response.setText((String) turn.getValue());
			break;
		case "commands":
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Building response for type commands");
			}
			try {
				Jsonb mapper=JsonbBuilder.create();
				List<Command> l= JsonbBuilder.create().fromJson(mapper.toJson(turn.getValue()), new ArrayList<Command>() {}.getClass().getGenericSuperclass());
				response.setVgwActionSequence(l);
			}catch(Exception e) {
				log.log(Level.SEVERE,Messages.getMessage("CWSAT0058E",testCase.getId()),e);
				return null;
			}
			break;
		}
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Responding for job "+job.getId()+" on call "+request.getCallSessionId()+" with "+turn);
		}
		return response;
	}

	// Class for wrapping a turn validation. It encapsulates if the turned passed or not and the error of why it didn't pass
	private class ValidationWrapper {
		boolean passed = false;
		String error = null;

		public ValidationWrapper(boolean passed, String error) {
			super();
			this.passed = passed;
			this.error = error;
		}

		public ValidationWrapper(String error) {
			super();
			this.error = error;
		}

		public ValidationWrapper(boolean passed) {
			super();
			this.passed = passed;
		}

		public boolean isPassed() {
			return passed;
		}

		public void setPassed(boolean passed) {
			this.passed = passed;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
		
		public ValidationWrapper prependToError(String prepend) {
			if(this.error==null)
				this.error=prepend;
			else
				this.error=prepend+" "+this.error;
			return this;
		}
	}

}
