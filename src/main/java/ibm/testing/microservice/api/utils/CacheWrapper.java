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

import javax.naming.NamingException;

import com.ibm.websphere.cache.DistributedMap;
import ibm.testing.microservice.models.GetJob;

public interface CacheWrapper {

	public DistributedMap getCacheMapObj() throws NamingException;
	//Methods for adding resources to cache
	public void addJobToCache(GetJob job);
	//Methods for getting resources from cache
	public GetJob getJobFromCache(String workerId, String jobId);
	//Methods for updating resources in cache
	public void updateJobInCache(String jobId,GetJob job);
	//Method for removing things from cache
	public void removeJobFromCache(String workerId, String jobId);
	//Method for updating job given a callID
	public void updateJobOnCall(GetJob job,String callSessionId);
	//Method for getting job given a callID
	public GetJob getJobOnCall(String callSessionId);
	
}
