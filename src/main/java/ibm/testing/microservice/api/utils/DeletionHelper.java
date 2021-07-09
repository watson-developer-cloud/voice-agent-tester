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

import com.cloudant.client.org.lightcouch.NoDocumentException;

import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetTestCase;
import ibm.testing.microservice.models.GetWorker;

public class DeletionHelper implements Runnable{
	
	private enum Resource{
		worker,testCase,job,batchJob;
		String type;
		String id;
		public String getId() {
			return id;
		}
		public String getType() {
			return type;
		}
		public Resource init(String resourceId,String resourceType) {
			this.id=resourceId;
			for (Resource type : Resource.values()) {
				if(resourceType.equals(type.toString())) {
					this.type=type.toString();
					break;
				}
			}
			return this;
		}
	}

	private Resource resource=Resource.worker;
	private Integer tries=5; 
	private Integer ran=0;
	private Integer waitTimeMili=1000;// Time to retry
	private Integer waitTimeMiliBatchJob=30000; // Time to wait if jobs on batchjob are not yet all created
	
	private Logger log = Logger.getLogger(DeletionHelper.class.getName());
	
	public DeletionHelper(String resourceId,String resourceType) {
		if(resourceId==null || resourceType==null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Enum for deletion could not be initialized");
			}
			this.ran=this.tries;
		}
		else
			resource.init(resourceId, resourceType);
	}
	
	@Override
	public void run() {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "The deletion helper is attempting to delete resource "+resource.getId());
		}
		boolean deleted=false;
		boolean allJobsFound=false;
		while(ran<tries && !deleted) {
			try {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Waiting "+waitTimeMili+" ms before trying to remove resource "+resource.getId());
				}
				Thread.sleep(waitTimeMili);
			} catch (InterruptedException e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Sleep was not done error occurred",e);
				}
			}
			ran++;
			switch(resource.getType()) {
			case "testCase":
				try {
					GetTestCase test=CloudantUtils.getTestCase(resource.getId());
					CloudantUtils.removeTestCase(test);
					deleted=true;
				} catch (Exception e) {
					if (e instanceof NoDocumentException) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Test case "+resource.getId()+" was attempted to delete by deletion helper but got a no document execption at attempt "+ran+". No further attempts will be made",e);
						}
						deleted=true;
						break;
					}
					else {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Test case "+resource.getId()+" still not deleted by deletion helper an error occurred. Times ran: "+ran,e);
						}
					}
				}
				break;
			case "worker":
				try {
					GetWorker worker=CloudantUtils.getWorker(resource.getId());
					CloudantUtils.removeWorker(worker);
					deleted=true;
				} catch (Exception e) {
					if (e instanceof NoDocumentException) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Worker "+resource.getId()+" was attempted to delete by deletion helper but got a no document execption at attempt "+ran+". No further attempts will be made",e);
						}
						deleted=true;
						break;
					}
					else {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Worker "+resource.getId()+" still not deleted by deletion helper an error occurred. Times ran: "+ran,e);
						}
					}
				}
				break;
			case "job":
				String[] ids=resource.getId().split("_");
				String workerId=ids[0];
				String jobId=ids[1];
				try {
					GetJob job=CloudantUtils.getJob(workerId, jobId);
					CloudantUtils.removeJob(job);
					deleted=true;
				} catch (Exception e) {
					if (e instanceof NoDocumentException) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Job "+resource.getId()+" was attempted to delete by deletion helper but got a no document execption at attempt "+ran+". No further attempts will be made",e);
						}
						deleted=true;
						break;
					}
					else {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Job "+resource.getId()+" still not deleted by deletion helper an error occurred. Times ran: "+ran,e);
						}
					}
				}
				break;
			case "batchJob":
				String[] batchIds=resource.getId().split("_");
				String workerBatchId=batchIds[0];
				String jobBatchId=batchIds[1];
				try {
					BatchJob job=CloudantUtils.getBatchJob(workerBatchId, jobBatchId);
					if(job.getSubJobs().size()<job.getConcurrentJobs() && !allJobsFound) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Batch job "+resource.getId()+" still doesn't contain all jobs.");
						}
						try {
							if(log.isLoggable(Level.FINEST)) {
								log.log(Level.FINEST, "Waiting "+waitTimeMiliBatchJob+" ms before trying again");
							}
							Thread.sleep(waitTimeMiliBatchJob);
						} catch (InterruptedException e) {
							if(log.isLoggable(Level.FINEST)) {
								log.log(Level.FINEST, "Sleep was not done error occurred",e);
							}
						}
						break;
					}
					else {
						allJobsFound=true;
						CloudantUtils.removeBatchJob(workerBatchId, job);
						deleted=true;
					}
				} catch (Exception e) {
					if (e instanceof NoDocumentException) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Batch job "+resource.getId()+" was attempted to delete by deletion helper but got a no document execption at attempt "+ran+". No further attempts will be made",e);
						}
						deleted=true;
						break;
					}
					else {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Batch job "+resource.getId()+" still not deleted by deletion helper an error occurred. Times ran: "+ran,e);
						}
					}
				}
				break;
			}
		}
		if(!deleted) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Resource "+resource.getId()+" was attempted to delete by the deletion helper a number of "+ran+" times but was not succesful");
			}
		}
		else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Resource "+resource.getId()+" was found succesfully deleted by the deletion helper on attempt "+ran);
			}
		}
	}

}
