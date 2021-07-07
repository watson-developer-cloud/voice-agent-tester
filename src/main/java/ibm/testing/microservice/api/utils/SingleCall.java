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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ibm.testing.microservice.models.GetJob;

public class SingleCall implements Runnable {

	private static Logger log = Logger.getLogger(SingleCall.class.getName());

	private GetJob job;
	private Map<String, Object> context;

	public SingleCall(GetJob job) {
		this.job = job;
	}

	public SingleCall(GetJob job, Map<String, Object> context) {
		this(job);
		this.context = context;
	}

	@Override
	public void run() {
		try {
			OutboundCallUtils.startOutboundCall(job, context);
		}catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0057E",job.getId()),e);
			job.setJobPaused();
			try {
				CloudantUtils.updateJob(job,false);
			}catch(Exception e2) {
				log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e2);
			}
		}
	}

}
