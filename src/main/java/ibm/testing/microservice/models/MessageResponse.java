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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;

public class MessageResponse {

	private static Jsonb mapper = JsonbBuilder.create();

	private Map<String, Object> input = new HashMap<String, Object>();
	private Map<String, Object> output = new HashMap<String, Object>();
	private Map<String, Object> context = new HashMap<String, Object>();
	
	public MessageResponse(MessageRequest request) {
		this.context=request.getContext();
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

	public Map<String, Object> getOutput() {
		return output;
	}

	public void setOutput(Map<String, Object> outputData) {
		this.output = outputData != null ? new HashMap<String, Object>(outputData) : outputData;
	}

	public void setVgwActionSequence(List<Command> vgwActionSequence) {
		output.put("vgwActionSequence", vgwActionSequence);
	}

	public MessageResponse setUpHangUp() {
		Map<String, String> hangup = new HashMap<String, String>();
		hangup.put("command", "vgwActHangup");
		this.output.put("vgwAction", hangup);
		return this;
	}
	
	public void setText(String text) {
		ArrayList<String> temp=new ArrayList<String>();
		temp.add(text);
		this.output.put("text", temp);
	}
	
	public static MessageResponse constructEmptyResponse(MessageRequest request) {
		MessageResponse response=new MessageResponse(request);
		if(response.getContext().containsKey(MessageRequest.STARTING_OUTBOUND_CALL))
			response.getContext().remove(MessageRequest.STARTING_OUTBOUND_CALL);
		response.setText("");
		return response;
	}

	@Override
	public String toString() {
		return toJsonString();
	}

	public String toJsonString() {
		String jsonStr = null;
		try {
			jsonStr = mapper.toJson(this);
		} catch (NullPointerException | JsonbException e) {
		}
		return jsonStr;
	}
}
