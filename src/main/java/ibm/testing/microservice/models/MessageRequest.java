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
package ibm.testing.microservice.models;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;

public class MessageRequest {

	private static final String TEXT = "text";

	private static Jsonb mapper = JsonbBuilder.create();

	private boolean alternateIntents;
	private Map<String, Object> input = new HashMap<String, Object>();
	private Map<String, Object> context = new HashMap<String, Object>();

	public static final String IS_FAILURE="test_failed";
	public static final String FAILURE="failure_reason";
	public static final String ID="conversation_id";
	public static final String TEST_INTERRUPTED="test_interrupted";
	//public static final String CURRENT_WORKER_CASE_INDEX="worker_case_index";
	public static final String STARTING_OUTBOUND_CALL="outbound_call_start";
	public static final String CASE_ID="current_case_id";
	public static final String IS_PARALLEL="job_parallel";
	public static final String PERCENT_PER_CASE="percent_per_test_case";
	public static final String COMPLETED_SUCESSFULLY="completed_sucessfully";
	public static final String MISSING_RESOURCES="missing_dependency";
	public static final String[] TESTING_CONTEXT_KEYS = { ID,PERCENT_PER_CASE };

	public MessageRequest() {

	}

	public boolean getAlternateIntents() {
		return alternateIntents;
	}

	public void setAlternateIntents(boolean alternateIntents) {
		this.alternateIntents = alternateIntents;
	}

	public Map<String, Object> getInput() {
		return input;
	}

	public void setInput(Map<String, Object> inputData) {
		this.input = inputData != null ? new HashMap<String, Object>(inputData) : inputData;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> contextData) {
		this.context = contextData != null ? new HashMap<String, Object>(contextData) : contextData;
	}

	public void setInputText(String text) {
		input.put(TEXT, text);
	}

	public String getInputText() {
		return (String) input.get(TEXT);
	}

	public Map<String, Object> getServiceOrientedContext() {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String s : TESTING_CONTEXT_KEYS)
			result.put(s, context.get(s));
		return result;
	}
	
	public String getWorkerId() {
		String id=(String)context.get(ID);
		return id==null?null:id.split("_")[0];
	}
	
	public String getJobId() {
		String id=(String)context.get(ID);
		return id==null?null:id.split("_")[1];
	}
	
	
	public void setTestFailed(GetJobResultsFailures failure) {
		this.context.put(IS_FAILURE, "Yes");
		this.context.put(FAILURE,failure);
	}
	
	public boolean isTestFailed() {
		if(this.context.containsKey(IS_FAILURE))
			return this.context.get(IS_FAILURE).equals("Yes");
		return false;
	}
	
	
	public void setMissingDependency() {
		this.context.put(MISSING_RESOURCES, "Yes");
	}
	
	public boolean isMissingDependency() {
		if(this.context.containsKey(MISSING_RESOURCES))
			return this.context.get(MISSING_RESOURCES).equals("Yes");
		return false;
	}
	
	
	public GetJobResultsFailures getFailureReason() {
		return mapper.fromJson(mapper.toJson(this.getContext().get(FAILURE)), GetJobResultsFailures.class);
	}
	
	public boolean isHangUp() {
		if(this.context.containsKey("vgwHangUp") && this.context.get("vgwHangUp").equals("Yes")) {
			return this.getInputText().equals("vgwHangUp");
		}
		return false;
	}
	
	public String getHangUpReason() {
		return this.context.get("vgwHangupReason").toString();
	}
	
	public void setTestInterrupted() {
		this.context.put(TEST_INTERRUPTED, "Yes");
	}
	
	public boolean isTestInterrupted() {
		if(this.context.containsKey(TEST_INTERRUPTED)&&this.context.get(TEST_INTERRUPTED).equals("Yes"))
			return true;
		return false;
	}
	
	public boolean isStartOutboundCall() {
		if(this.context.containsKey(STARTING_OUTBOUND_CALL)&&this.context.get(STARTING_OUTBOUND_CALL).equals("Yes"))
			return true;
		return false;
	}
	
	public void setCompletedSucesfully() {
		this.context.put(COMPLETED_SUCESSFULLY, "Yes");
	}
	
	public boolean isCompletedSucesfully() {
		if(this.context.containsKey(COMPLETED_SUCESSFULLY)&&this.context.get(COMPLETED_SUCESSFULLY).equals("Yes"))
			return true;
		return false;
	}
	
	public double getPercentPerCase() {
		return Double.parseDouble((String.valueOf(context.get(PERCENT_PER_CASE))));
	}
	
	public String getCallSessionId() {
		return (String) this.context.get("vgwSessionID");
	}

	@Override
	public String toString() {
		return toJsonString();
	}

	public String toJsonString() {
		StringBuilder jsonStr = new StringBuilder();
		try {
			jsonStr.append("{");
			jsonStr.append("\"input\":"+mapper.toJson(this.input)+",");
			jsonStr.append("\"context\":"+mapper.toJson(this.context)+",");
			jsonStr.append("\"alternateIntents\":"+mapper.toJson(this.alternateIntents));
			jsonStr.append("}");
		} catch (NullPointerException | JsonbException e) {
		}
		return jsonStr.toString();
	}
}
