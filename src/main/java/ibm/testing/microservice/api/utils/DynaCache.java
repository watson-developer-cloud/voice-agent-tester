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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;

import ibm.testing.microservice.models.GetJob;

public class DynaCache implements CacheWrapper{

	
	private static DistributedMap cacheObject;
	private static Integer TIME_TO_LIVE;
	private static final Integer DEFAULT_PRIORITY=1;
	private static Logger log = Logger.getLogger(DynaCache.class.getName());
	 
	protected DynaCache() {
 
	}
 
	public DistributedMap getCacheMapObj() throws NamingException {
		if (cacheObject == null) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Initializing Dynacache");
			}
			InitialContext ctx = new InitialContext();
			cacheObject = (DistributedMap) ctx
					.lookup("services/cache/samplecache");
			TIME_TO_LIVE=(Integer) ctx.lookup("CACHE_TIME_TO_LIVE");
			TIME_TO_LIVE*=60;//Get in seconds
			cacheObject.setTimeToLive(TIME_TO_LIVE);
		}
		return cacheObject;
	}
	
	public void addJobToCache(GetJob job){
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Adding job "+job.getId()+" to cache");
		}
		addToCache(job.getWorkerUsed().getId()+"_"+job.getId(),job);
	}
	
	
	private void addToCache(String id,Object resource) {
		if(verifyCache())
			cacheObject.put(id, resource);
	}
	
	public GetJob getJobFromCache(String workerId, String jobId){
		Object resource=getResourceFromCache(workerId+"_"+jobId);
		if(resource !=null && resource instanceof GetJob) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Getting job "+jobId+" from worker "+workerId+" from cache and resetting ttl");
			}
			cacheObject.put(workerId+"_"+jobId, resource, DEFAULT_PRIORITY, TIME_TO_LIVE, EntryInfo.SHARED_PUSH_PULL, null);
			return (GetJob) resource;
		}
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Job "+jobId+" was not found in cache");
		}
		return null;
	}
	
	private Object getResourceFromCache(String resourceId) {
		if(verifyCache())
			return cacheObject.get(resourceId);
		return null;
	}
	
	public void updateJobInCache(String jobId,GetJob job){
		if(verifyCache()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Updating job "+jobId+" from worker"+job.getWorkerUsed().getId()+" in cache");
			}
			cacheObject.put(job.getWorkerUsed().getId()+"_"+jobId, job);
		}
	}
	
	private boolean verifyCache() {
		if (cacheObject == null) {
			try {
				getCacheMapObj();
			} catch (NamingException e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Caching service not found");
				}
				return false;
			}
		}
		return true;
	}
	
	public void removeJobFromCache(String workerId, String jobId){
		if (cacheObject == null) {
			return;
		}
		if(cacheObject.containsKey(workerId+"_"+jobId)) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Removing job "+jobId+" from worker "+workerId+" from cache");
			}
			cacheObject.remove(workerId+"_"+jobId);
		}
	}

	@Override
	public void updateJobOnCall(GetJob job, String callSessionId) {
		if(verifyCache()) {
			if(log.isLoggable(Level.FINEST)) {
				if(job==null) 
					log.log(Level.FINEST, "Job passed was null. Removing from cache the job on call "+callSessionId);
				else
					log.log(Level.FINEST, "Updating job "+job.getId()+" from worker"+job.getWorkerUsed().getId()+" in cache to call "+callSessionId);
			}
			if(job==null)
				cacheObject.remove(callSessionId);
			else
				cacheObject.put(callSessionId, job);
		}
	}

	@Override
	public GetJob getJobOnCall(String callSessionId) {
		Object resource=getResourceFromCache(callSessionId);
		if(resource !=null && resource instanceof GetJob) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Getting job on call "+callSessionId+" from cache and resetting ttl");
			}
			cacheObject.put(callSessionId, resource, DEFAULT_PRIORITY, TIME_TO_LIVE, EntryInfo.SHARED_PUSH_PULL, null);
			return (GetJob) resource;
		}
		return null;
	}
	
}
