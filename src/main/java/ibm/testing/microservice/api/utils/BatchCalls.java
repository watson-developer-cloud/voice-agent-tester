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

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cloudant.client.org.lightcouch.NoDocumentException;

import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.SubJobs;

public class BatchCalls implements Runnable{
	
	private static Logger log = Logger.getLogger(BatchCalls.class.getName());
	
	private Integer numberOfJobs;
	private Integer jobsPerSecond=-1;
	private String masterJobId;
	private GetWorker worker;
	private String startingOption=null;
	private BatchJob job;
	public static final String RESTART="restart";
	public static final String UNPAUSE="unpause";
	public static final String CREATE="create";
	
	
	public BatchCalls(int numberOfJobs,int jobsPerSecond,String masterId,GetWorker worker) {
		this.masterJobId=masterId;
		this.jobsPerSecond=jobsPerSecond;
		this.worker=worker;
		this.numberOfJobs=numberOfJobs;
		this.startingOption=CREATE;
	}
	
	public BatchCalls(int numberOfJobs,String masterId,GetWorker worker) {
		this.masterJobId=masterId;
		this.worker=worker;
		this.numberOfJobs=numberOfJobs;
		this.startingOption=CREATE;
	}
	
	public BatchCalls(BatchJob job,int jobsPerSecond,GetWorker worker,String startingOption) {
		this.job=job;
		this.worker=worker;
		this.startingOption=startingOption;
		this.jobsPerSecond=jobsPerSecond;
	}
	
	public BatchCalls(BatchJob job,GetWorker worker,String startingOption) {
		this.job=job;
		this.worker=worker;
		this.startingOption=startingOption;
	}

	@Override
	public void run() {
		if(startingOption.equals(CREATE)) {
			int currentStartedJobs=0;
			for(int i=1;i<=numberOfJobs;i++) {
				GetJob job=GetJob.newJobToStartCall(worker,masterJobId);
				CloudantUtils.createJob(job);
				try{
					OutboundCallUtils.startOutboundCall(job,null);
					if(this.jobsPerSecond!=-1) 
						currentStartedJobs++;
				}catch(Exception e) {
					log.log(Level.SEVERE, Messages.getMessage("CWSAT0029E",job.getId()), e);
					job.setJobStopped();
					try {
						CloudantUtils.updateJob(job,false);
					}catch(Exception e2) {
						log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e2);
					}
				}
				if(this.jobsPerSecond!=-1&&currentStartedJobs>=this.jobsPerSecond) {
					try {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Waiting 1 second before creating more jobs and starting calls for batch job "+masterJobId);
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						if(log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "Something went wrong sleeping a second between the jobs",e);
						}
					}
					currentStartedJobs=0;
				}
			}
		}else if(startingOption.equals(RESTART)) {
			int currentStartedJobs=0;
			for(SubJobs sub:this.job.getSubJobs()) {
				try {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Starting sub job"+sub.getId()+" from batch job "+job.getId()+" from worker "+worker.getId());
					}
					GetJob job= CloudantUtils.getJob(worker.getId(), sub.getId());
					//The job can only be put on pause if it is running
					if(job.isJobValidForReStarting()) {
						job.setWorkerUsed(worker);
						if(job.isJobRunning() || job.isJobStarting()) {
							job.setJobReStarting();
							job.setStartTime(Instant.now().toEpochMilli());
							CloudantUtils.updateJob(job);
						}else {
							job.reset();
							job.setJobStarting();
							//Post job to db
							CloudantUtils.updateJob(job,false);
							//Initialize call
							try{
								OutboundCallUtils.startOutboundCall(job,null);
								if(this.jobsPerSecond!=-1) 
									currentStartedJobs++;
							}catch(Exception e) {
								log.log(Level.SEVERE, Messages.getMessage("CWSAT0029E",job.getId()), e);
								job.setJobStopped();
								try {
									CloudantUtils.updateJob(job,false);
								}catch(Exception e2) {
									log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e2);
								}
							}
					}
						if(this.jobsPerSecond!=-1&&currentStartedJobs>=this.jobsPerSecond) {
							try {
								if(log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Waiting 1 second before restarting more sub jobs for batch job "+masterJobId);
								}
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								if(log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Something went wrong sleeping a second between the jobs",e);
								}
							}
							currentStartedJobs=0;
						}
					}else {
						log.log(Level.INFO, Messages.getMessage("CWSAT0028I",new String[] {sub.getId(),job.getStatus()}));
					}
				}catch(NoDocumentException e) {
					//Worker or job could not exist
					log.log(Level.INFO, e.getMessage(),e);
				}catch (Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
		    }
		}else if(startingOption.equals(UNPAUSE)) {
			int currentStartedJobs=0;
			for(SubJobs sub:job.getSubJobs()) {
				try {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Unpausing sub job"+sub.getId()+" from batch job "+job.getId()+" from worker "+worker.getId());
					}
					GetJob job= CloudantUtils.getJob(worker.getId(), sub.getId());
					//The job can only be put on pause if it is running
					if(job.isJobPaused()||job.isJobStopped()) {
						job.setJobStarting();
						CloudantUtils.updateJob(job,false);
						//Initialize call
						try{
							OutboundCallUtils.startOutboundCall(job,null);
							if(this.jobsPerSecond!=-1) 
								currentStartedJobs++;
						}catch(Exception e) {
							log.log(Level.SEVERE, Messages.getMessage("CWSAT0029E",job.getId()), e);
							job.setJobPaused();
							try {
								CloudantUtils.updateJob(job,false);
							}catch(Exception e2) {
								log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e2);
							}
						}
						if(this.jobsPerSecond!=-1&&currentStartedJobs>=this.jobsPerSecond) {
							try {
								if(log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Waiting 1 second before unpausing more sub jobs for batch job "+masterJobId);
								}
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								if(log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Something went wrong sleeping a second between the jobs",e);
								}
							}
							currentStartedJobs=0;
						}
					}
					else {
						log.log(Level.INFO, Messages.getMessage("CWSAT0033I",sub.getId()));
					}
				}catch(NoDocumentException e) {
					//Worker or job could not exist
					log.log(Level.INFO, e.getMessage(),e);
				}catch (Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
		    }
		}
	}

}
