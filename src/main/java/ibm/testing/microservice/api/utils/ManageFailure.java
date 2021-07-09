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

import java.util.logging.Level;
import java.util.logging.Logger;

import ibm.testing.microservice.models.GetJob;

public class ManageFailure implements Runnable{

	private String callSessionId;
	private String failure;
	private Integer waitTime=3000;
	
	private static Logger log = Logger.getLogger(ManageFailure.class.getName());
	
	public ManageFailure(String callSessionId, String failure) {
		this.callSessionId=callSessionId;
		this.failure=failure;
	}
	
	@Override
	public void run() {
		try {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Failure helper managing failure on call "+callSessionId+". Got failure "+failure);
				log.log(Level.FINEST, "Failure helper waiting "+waitTime+" ms to allow primary part to manage failure");
			}
			// Sleep three seconds before trying to update the job. This gives the primary SOE a chance to update the call
			Thread.sleep(waitTime);
			GetJob job = CloudantUtils.getJobForCall(callSessionId);
			if(job!=null) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Failure helper marking job "+job.getId()+" on call "+callSessionId+" as invalid");
				}
				job.setJobInvalid();
				CloudantUtils.updateJob(job,false);
			}else {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Failure helper the failure on job "+job.getId()+" on call "+callSessionId+" was managed by primary part");
				}
			}
		} catch (Exception e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Failure helper could not mark job as invalid for call "+callSessionId,e);
			}
		}
	}

}
