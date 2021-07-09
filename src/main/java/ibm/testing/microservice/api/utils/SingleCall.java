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
