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
