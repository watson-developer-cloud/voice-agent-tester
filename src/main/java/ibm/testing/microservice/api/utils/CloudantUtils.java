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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.DesignDocument;
import com.cloudant.client.api.model.DesignDocument.MapReduce;
import com.cloudant.client.api.model.Document;
import com.cloudant.client.api.views.Key;
import com.cloudant.client.org.lightcouch.NoDocumentException;

import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.CreateTestCase;
import ibm.testing.microservice.models.CreateWorker;
import ibm.testing.microservice.models.CreateWorkerCases;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetTestCase;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.GetWorkerJobs;
import ibm.testing.microservice.models.SubJobs;

public class CloudantUtils {

	// Credentials for database
	private static String cloudantUrl;
	private static String cloudantUsername;
	private static String cloudantPassword;
	private static String cloudantAccount;
	private static String cloudantApikey;
	private static String dbName;
	private static String cloudantResourcesPath;
	private static Boolean loginWithUserPass = null;
	private static Boolean loginWithUrl = null;
	private static String cacheImplementation;
	private static final String DYNACACHE_IMPLEMENTATION = "dynacache";
	private static final String REDIS_IMPLEMENTATION = "redis";

	// Persistence objects
	private static Database db;
	private static CacheWrapper cache;

	// View names for design docs
	private final static String DESIGN_DOC_NAME = "group_by";
	private final static String TEST_CASE_GROUP_VIEW = "test_namespace";
	private final static String WORKER_GROUP_VIEW = "worker_namespace";
	private final static String JOB_CALL_GROUP_VIEW = "job_callId";
	private final static String BATCH_JOB_SUB_JOB_GROUP_VIEW = "batchJob_subJob";
	private final static String JOB_WORKER_GROUP_VIEW = "job_worker";// TODO verify if this is actually necessary
	
	@Resource(lookup="cloudant/scheduledExecutor")
    private static ScheduledExecutorService executor;

	private static Logger log = Logger.getLogger(CloudantUtils.class.getName());
	private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	static {
		try {
			cloudantResourcesPath = new InitialContext().lookup("cloudantResources").toString();
			executor=(ScheduledExecutorService)new InitialContext().lookup("cloudant/scheduledExecutor");
			fillCredentials();
		} catch (Exception e) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Initialization of necessary variables for database failed.",e);
			}
		}
		initializeDatabase();
		initializeCache();
	}

	private static void fillCredentials() throws ServerErrorException, NamingException {
		if(log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "Initializing variables for database");
		}
		InitialContext ic = new InitialContext();
		cacheImplementation = ic.lookup("CACHE_IMPLEMENTATION").toString();
		cloudantUrl = ic.lookup("CLOUDANT_URL").toString();
		if (isLookupNull(cloudantUrl)) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_URL was not found. Looking for CLOUDANT_ACCOUNT.");
			}
			cloudantAccount = ic.lookup("CLOUDANT_ACCOUNT").toString();
			if (isLookupNull(cloudantAccount)) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_URL nor CLOUDANT_ACCOUNT was found. Must have one of them for connecting to database.");
				}
				throw new ServerErrorException(
						"Must have CLOUDANT_ACCOUNT or CLOUDANT_URL defined for acessing database.", 500);
			}
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_ACCOUNT found. Account got was "+cloudantAccount);
			}
			loginWithUrl = false;
		} else {
			loginWithUrl = true;
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_URL found. Url got was "+cloudantUrl);
			}
		}
		dbName = ic.lookup("CLOUDANT_DATABASE_NAME").toString();
		if (isLookupNull(dbName)) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_DATABASE_NAME was not provided.");
			}
			dbName = null;
			throw new ServerErrorException("CLOUDANT_DATABASE_NAME must be defined for acessing database.", 500);
		}
		cloudantUsername = ic.lookup("CLOUDANT_USERNAME").toString();
		cloudantPassword = ic.lookup("CLOUDANT_PASSWORD").toString();
		if (!isLookupNull(cloudantUsername) && !isLookupNull(cloudantPassword)) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_USERNAME and CLOUDANT_PASSWORD were provided. Authenticating with "+cloudantUsername+":"+cloudantPassword);
			}
			loginWithUserPass = true;
		}
		if (loginWithUserPass == null) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "CLOUDANT_USERNAME and CLOUDANT_PASSWORD were not found. Looking for CLOUDANT_APIKEY.");
			}
			cloudantApikey = ic.lookup("CLOUDANT_APIKEY").toString();
			if (!isLookupNull(cloudantApikey)) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_APIKEY was provided. Authenticating with apikey "+cloudantApikey);
				}
				loginWithUserPass = false;
			}
			else {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_USERNAME and CLOUDANT_PASSWORD nor CLOUDANT_APIKEY were found. Must have one of them for connecting to database.");
				}
			}
		}
	}

	private static void initializeCache() {
		if (cacheImplementation.equals(DYNACACHE_IMPLEMENTATION)) {
			log.log(Level.INFO,Messages.getMessage("CWSAT0046I"));
			cache = new DynaCache();
		} else if (cacheImplementation.equals(REDIS_IMPLEMENTATION)) {
			log.log(Level.INFO,Messages.getMessage("CWSAT0047I"));
			throw new ServerErrorException("No redis implementation yet", 500);
		} else {
			log.log(Level.INFO,Messages.getMessage("CWSAT0048I"));
			cache = null;
		}
	}

	public static boolean isLookupNull(String lookup) {
		return lookup == null || lookup.equals("null") || lookup.isEmpty() || lookup.charAt(0) == '$';
	}

	private static void initializeDatabase() {
		try {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Initializing database.");
			}
			if (loginWithUrl == null) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_URL nor CLOUDANT_ACCOUNT was found. Must have one of them for connecting to database.");
				}
				throw new ServerErrorException("Can't access the database. URL or account must be specified", 500);
			}
			if (loginWithUserPass == null) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_USERNAME and CLOUDANT_PASSWORD nor CLOUDANT_APIKEY were found. Must have one of them for connecting to database.");
				}
				throw new ServerErrorException("Can't access the database. Credentials must be specified", 500);
			}
			if (isLookupNull(dbName)) {
				if(log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "CLOUDANT_DATABASE_NAME was not provided.");
				}
				throw new ServerErrorException("CLOUDANT_DATABASE_NAME must be defined for acessing database.", 500);
			}
			CloudantClient cl = null;
			if (loginWithUrl) {
				if (loginWithUserPass)
					cl = ClientBuilder.url(new URL(cloudantUrl)).username(cloudantUsername).password(cloudantPassword)
							.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
				else
					cl = ClientBuilder.url(new URL(cloudantUrl)).iamApiKey(cloudantApikey)
							.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
			} else {
				if (loginWithUserPass)
					cl = ClientBuilder.account(cloudantAccount).username(cloudantUsername).password(cloudantPassword)
							.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
				else
					cl = ClientBuilder.account(cloudantAccount).iamApiKey(cloudantApikey)
							.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
			}
			db = cl.database(dbName, true);
		} catch (MalformedURLException e) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Connecting to database failed. A bad URL was provided.");
			}
			db = null;
		} catch (Exception e) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Something went wrong initializing database.");
			}
			db = null;
		}
	}

	private static Database getDatabase() throws ServerErrorException {
		if (db == null) {
			initializeDatabase();
			if (db == null)
				throw new ServerErrorException("Database problems", 500);
		}
		return db;
	}

	public static CacheWrapper getCache() {
		return cache;
	}

	public static void verifyDesignDocExistence() {
		Database db = getDatabase();
		DesignDocument ddoc = null;
		try {
			ddoc = db.getDesignDocumentManager().get("_design/" + DESIGN_DOC_NAME);
		} catch (NoDocumentException e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "No design document found. Creating it.");
			}
			ddoc = new DesignDocument();
			ddoc.setId("_design/" + DESIGN_DOC_NAME);
			db.save(ddoc);
			ddoc = db.getDesignDocumentManager().get("_design/" + DESIGN_DOC_NAME);
		} catch (Exception e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Something went wrong validating design document "+DESIGN_DOC_NAME);
			}
			return;
		}
		// Update views in design
		try {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Checking views inside design document "+DESIGN_DOC_NAME);
			}
			updateViews(ddoc);
		} catch (JsonbException | FileNotFoundException e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Something went wrong validating design document "+DESIGN_DOC_NAME);
			}
			return;
		} catch (Exception e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Something went wrong validating design document "+DESIGN_DOC_NAME);
			}
			return;
		}
	}

	private static void updateViews(DesignDocument docInDb) throws JsonbException, FileNotFoundException {
		File f = new File(cloudantResourcesPath + DESIGN_DOC_NAME + "/views");
		Jsonb mapper = JsonbBuilder.create();
		boolean changed = false;
		if (docInDb.getViews() == null) {
			changed = true;
			docInDb.setViews(new HashMap<String, MapReduce>());
		}
		for (File fls : f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		})) {
			MapReduce mp = new MapReduce();
			String name = FilenameUtils.removeExtension(fls.getName());
			Map<String, String> map = mapper.fromJson(new FileReader(fls), new HashMap<String, String>() {
			}.getClass().getGenericSuperclass());
			mp.setMap((map.get("map") == null || map.get("map").equals("null")) ? null : map.get("map"));
			mp.setReduce((map.get("reduce") == null || map.get("reduce").equals("null")) ? null : map.get("reduce"));
			if (docInDb.getViews().containsKey(name)) {
				String ddocMap = docInDb.getViews().get(name).getMap();
				String ddocReduce = docInDb.getViews().get(name).getReduce();
				String localMap = mp.getMap();
				localMap = (localMap == null || localMap.equals("null")) ? null : localMap;
				String localReduce = mp.getReduce();
				localReduce = (localReduce == null || localReduce.equals("null")) ? null : localReduce;
				if (!(ddocMap == localMap || ddocMap.equals(localMap))
						|| !(ddocReduce == localReduce || ddocReduce.equals(localReduce))) {
					changed = true;
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Design document view does not match local view "+name);
					}
					docInDb.getViews().put(name, mp);
				}
			} else {
				changed = true;
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Design document does not contain view "+name);
				}
				docInDb.getViews().put(name, mp);
			}
		}
		if (changed) {
			getDatabase().update(docInDb);
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Updated views inside design document "+DESIGN_DOC_NAME);
			}
		}
	}

	/*
	 * 
	 * Utilities for getting things from database
	 * 
	 */

	public static GetTestCase getTestCase(String caseId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Getting test case "+caseId+" from database.");
		}
		GetTestCase testCase = getDatabase().find(GetTestCase.class, caseId);
		if (!validator.validate(testCase).isEmpty()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Object "+caseId+" exists in database but does not match a test case");
			}
			throw new NoDocumentException("Resource: " + caseId + " is not valid to be a test case");
		}
		return testCase;
	}

	public static GetWorker getWorker(String workerId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Getting worker "+workerId+" from database.");
		}
		GetWorker worker = getDatabase().find(GetWorker.class, workerId);
		if (!validator.validate(worker).isEmpty()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Object "+workerId+" exists in database but does not match a worker");
			}
			throw new NoDocumentException("Resource: " + workerId + " is not valid to be a worker");
		}
		return worker;
	}

	public static GetJob getJob(String workerId, String jobId) {

		GetJob job = null;
		if (cache != null)
			job = cache.getJobFromCache(workerId,jobId);
		if (job == null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Getting job "+jobId+" from worker "+workerId+" from database.");
			}
			job = getDatabase().find(GetJob.class, workerId + "_" + jobId);
			if (!validator.validate(job).isEmpty()) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Object "+workerId+"_"+jobId+" exists in database but does not match a job");
				}
				throw new NoDocumentException("Resource: " + jobId + " is not valid to be a job");
			}
			if (cache != null && job.isJobValidForRunning())
				cache.addJobToCache(job);
		}
		return job;
	}

	public static BatchJob getBatchJob(String workerId, String jobId) throws IOException {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Getting batch job "+jobId+" from worker "+workerId+" from database and building stats");
		}
		BatchJob job = getDatabase().find(BatchJob.class, workerId + "_" + jobId);
		if (!validator.validate(job).isEmpty()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Object "+workerId+"_"+jobId+" exists in database but does not match a batch job");
			}
			throw new NoDocumentException("Resource: " + jobId + " is not valid to be a batch job");
		}
		verifyDesignDocExistence();
		// Get all sub jobs
		job.setSubJobs(getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, BATCH_JOB_SUB_JOB_GROUP_VIEW)
				.newRequest(Key.Type.STRING, SubJobs.class).keys(jobId).includeDocs(true).reduce(false).build()
				.getResponse().getValues());
		JsonObject reduce =null;
		if(cache!=null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Building stats for batch job "+jobId+" using cache");
			}
			// Initialize everything that would be included in the reduce
			Map<String, Object> red =new HashMap<String, Object>();
			red.put("jobsWithPercentage", 0);
			red.put("percentComplete", 0.0);
			red.put("maxStopTime", null);
			red.put("failures", 0);
			red.put(GetJob.CREATED, 0);
			red.put(GetJob.COMPLETED, 0);
			red.put(GetJob.FAILED, 0);
			red.put(GetJob.INVALID, 0);
			red.put(GetJob.PAUSED, 0);
			red.put(GetJob.PAUSING, 0);
			red.put(GetJob.RESTARTING, 0);
			red.put(GetJob.RUNNING, 0);
			red.put(GetJob.STARTING, 0);
			red.put(GetJob.STOPPED, 0);
			red.put(GetJob.STOPPING, 0);
			for(int i=0;i<job.getSubJobs().size();i++) {
				SubJobs sbjb=job.getSubJobs().get(i);
				if(sbjb.getStatus().equals(GetJob.COMPLETED) || sbjb.getStatus().equals(GetJob.FAILED) || sbjb.getStatus().equals(GetJob.INVALID) || sbjb.getStatus().equals(GetJob.PAUSED) || sbjb.getStatus().equals(GetJob.STOPPED)) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Job "+sbjb.getId()+" supposed to be not running so using stats already found in db");
					}
					red.put(sbjb.getStatus(), (int)red.get(sbjb.getStatus())+1);
					red.put("failures", (int)red.get("failures")+sbjb.getFailures());
					if(sbjb.getPercentComplete()!=null && sbjb.getPercentComplete()>0) {
						red.put("jobsWithPercentage", (int)red.get("jobsWithPercentage")+1);
						red.put("percentComplete", (double)red.get("percentComplete")+sbjb.getPercentComplete());
					}
					if(sbjb.getMaxStopTime()!=null && (red.get("maxStopTime")==null || sbjb.getMaxStopTime()>(long)red.get("maxStopTime"))){
						red.put("maxStopTime", sbjb.getMaxStopTime());
					}
				}else {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Job "+sbjb.getId()+" supposed to be running so getting job from cache and updating stats");
					}
					GetJob jb = CloudantUtils.getJob(workerId, sbjb.getId());
					if(jb.getPercentComplete()!=null) {
						red.put("jobsWithPercentage", (int)red.get("jobsWithPercentage")+1);
						red.put("percentComplete", (double)red.get("percentComplete")+jb.getPercentComplete());
					}
					if(jb.getStopTime()!=null && (red.get("maxStopTime")==null || jb.getStopTime()>(long)red.get("maxStopTime"))){
						red.put("maxStopTime", jb.getStopTime());
					}
					red.put("failures", (int)red.get("failures")+jb.getResults().getNumberOfFailures());
					sbjb.setFailures(jb.getResults().getNumberOfFailures());
					red.put(jb.getStatus(), (int)red.get(jb.getStatus())+1);
					sbjb.setStatus(jb.getStatus());
				}
			}
			reduce = Json.createObjectBuilder(red).build();
		}
		else {
			Map<String, Object> red = getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, BATCH_JOB_SUB_JOB_GROUP_VIEW)
					.newRequest(Key.Type.STRING, Map.class).keys(jobId).build().getSingleValue();
			reduce = Json.createObjectBuilder(red).build();
		}
		int jobsWithPercentage = reduce.getInt("jobsWithPercentage");
		if (reduce.getInt(GetJob.COMPLETED) == job.getConcurrentJobs()) {
			job.setStatus(GetJob.COMPLETED);
			job.setStopTime(reduce.getJsonNumber("maxStopTime").longValueExact());
		} else if (reduce.getInt(GetJob.STOPPED) == job.getConcurrentJobs()) {
			job.setStatus(GetJob.STOPPED);
			if (jobsWithPercentage > 0)
				job.setPercentComplete(reduce.getJsonNumber("percentComplete").doubleValue() / jobsWithPercentage);
		} else if (reduce.getInt(GetJob.PAUSED) == job.getConcurrentJobs()) {
			job.setStatus(GetJob.PAUSED);
			if (jobsWithPercentage > 0)
				job.setPercentComplete(reduce.getJsonNumber("percentComplete").doubleValue() / jobsWithPercentage);
		} else {
			if (reduce.getInt(GetJob.PAUSING) > 0) {
				job.setStatus(GetJob.PAUSING);
				if (jobsWithPercentage > 0)
					job.setPercentComplete(reduce.getJsonNumber("percentComplete").doubleValue() / jobsWithPercentage);
			} else if (reduce.getInt(GetJob.STOPPING) > 0) {
				job.setStatus(GetJob.STOPPING);
				if (jobsWithPercentage > 0)
					job.setPercentComplete(reduce.getJsonNumber("percentComplete").doubleValue() / jobsWithPercentage);
			} else if (reduce.getInt(GetJob.RUNNING) > 0 || reduce.getInt(GetJob.STARTING) > 0
					|| reduce.getInt(GetJob.RESTARTING) > 0) {
				if (jobsWithPercentage > 0)
					job.setPercentComplete(reduce.getJsonNumber("percentComplete").doubleValue() / jobsWithPercentage);
				job.setStatus(GetJob.RUNNING);
			} else if (reduce.getInt(GetJob.INVALID) > 0)
				job.setStatus(GetJob.INVALID);
			else if (reduce.getInt(GetJob.FAILED) > 0) {
				job.setStatus(GetJob.FAILED);
				if (reduce.getInt(GetJob.FAILED) + reduce.getInt(GetJob.COMPLETED) == job.getConcurrentJobs())
					job.setStopTime(reduce.getJsonNumber("maxStopTime").longValueExact());
			}
		}
		return job;
	}

	public static GetJob getJobForCall(String callSessionId) throws IOException {
		if(cache!=null) {
			return cache.getJobOnCall(callSessionId);
		}else {
			verifyDesignDocExistence();
			GetJob job = getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, JOB_CALL_GROUP_VIEW)
					.newRequest(Key.Type.STRING, GetJob.class).keys(callSessionId).build().getSingleValue();
			return job;
		}
	}

	public static List<GetWorker> getWorkersInNamespace(String namespace) throws IOException {
		verifyDesignDocExistence();
		return getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, WORKER_GROUP_VIEW)
				.newRequest(Key.Type.STRING, Object.class).keys(namespace).includeDocs(true).build().getResponse()
				.getDocsAs(GetWorker.class);
	}

	public static List<GetTestCase> getTestCasesInNamespace(String namespace) throws IOException {
		verifyDesignDocExistence();
		return getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, TEST_CASE_GROUP_VIEW)
				.newRequest(Key.Type.STRING, Object.class).keys(namespace).includeDocs(true).build().getResponse()
				.getDocsAs(GetTestCase.class);
	}

	/*
	 * 
	 * Utilities for creating things in database
	 * 
	 */

	public static com.cloudant.client.api.model.Response createTestCase(CreateTestCase testCase) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Posting to database the new test case "+testCase);
		}
		return getDatabase().post(testCase);
	}

	public static com.cloudant.client.api.model.Response createWorker(CreateWorker worker)
			throws IllegalArgumentException {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating the cases in worker "+worker);
		}
		String validation = validateWorkerCases(worker.getCases(), worker.getNamespace());
		if (!validation.isEmpty())
			throw new IllegalArgumentException(validation);
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Posting to database the new worker "+worker);
		}
		return getDatabase().post(worker);
	}

	public static com.cloudant.client.api.model.Response createJob(GetJob job) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Posting to database the new job "+job);
		}
		com.cloudant.client.api.model.Response resp = getDatabase().post(job);
		job.setRev(resp.getRev());
		if (cache != null) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Adding job "+job.getId()+" to cache");
			}
			cache.addJobToCache(job);
		}
		return resp;
	}

	public static com.cloudant.client.api.model.Response createBatchJob(BatchJob job) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Posting to database the new batch job "+job);
		}
		com.cloudant.client.api.model.Response resp = getDatabase().post(job);
		job.setRev(resp.getRev());
		return resp;
	}

	/*
	 * 
	 * Utilities for updating things in database
	 * 
	 */

	public static void updateTestCase(GetTestCase testCase) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Updating test case "+testCase.getId()+" in database");
		}
		com.cloudant.client.api.model.Response resp = getDatabase().update(testCase);
		testCase.setRev(resp.getRev());
	}

	public static void updateJob(GetJob job) {
		updateJob(job,true);
	}
	
	public static void updateJob(GetJob job,boolean onlyOnCache) {
		if(!onlyOnCache) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Updating job "+job.getId()+" on both database and cache");
			}
			com.cloudant.client.api.model.Response resp = getDatabase().update(job);
			job.setRev(resp.getRev());
			if (cache != null) {
				cache.updateJobInCache(job.getId(), job);
			}else {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Tried to update job "+job.getId()+" on both database and cache but cache was null. Updated only in database");
				}
			}
		}else {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Updating job "+job.getId()+" only in cache");
			}
			if (cache != null) {
				cache.updateJobInCache(job.getId(), job);
			}else {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Tried to update job "+job.getId()+" only on cache but cache was null. Will try to update directly to database");
				}
				com.cloudant.client.api.model.Response resp = getDatabase().update(job);
				job.setRev(resp.getRev());
			}
		}
	}

	public static void updateBatchJob(BatchJob job,String workerId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Updating batch job "+job.getId()+" in database");
		}
		// When we update a batch job we only update the start time and stop time so we will only get the document without building the sub jobs and stats
		com.cloudant.client.api.model.Response resp = getDatabase().update(getDatabase().find(BatchJob.class, workerId + "_" + job.getId()));
		job.setRev(resp.getRev());
	}

	public static void validateAndUpdateWorker(GetWorker worker) throws IllegalArgumentException {
		String validation = validateWorkerCases(worker.getCases(), worker.getNamespace());
		if (!validation.isEmpty())
			throw new IllegalArgumentException(validation);
		updateWorker(worker);
	}

	public static void updateWorker(GetWorker worker) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Updating worker "+worker.getId()+" in database");
		}
		com.cloudant.client.api.model.Response resp = getDatabase().update(worker);
		worker.setRev(resp.getRev());
	}

	private static String validateWorkerCases(List<CreateWorkerCases> cases, String namespace) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Validating namespace "+namespace+" contains the cases "+cases);
		}
		List<Document> casesInNamespace = null;
		try {
			verifyDesignDocExistence();
			casesInNamespace = getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, TEST_CASE_GROUP_VIEW)
					.newRequest(Key.Type.STRING, Object.class).keys(namespace).includeDocs(true).build().getResponse()
					.getDocsAs(Document.class);
		} catch (IOException e) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Worker cases could not be validated.");
			}
		}
		if (casesInNamespace == null)
			return "Worker cases could not be validated";
		else {
			// Convert list to map for less time consuming operations
			Map<String, Boolean> quickIds = new HashMap<String, Boolean>();
			for (Document tc : casesInNamespace)
				quickIds.put(tc.getId(), tc.isDeleted());
			HashSet<CreateWorkerCases> set = new HashSet<CreateWorkerCases>();
			StringBuilder msg = new StringBuilder("");
			for (CreateWorkerCases tc : cases) {
				if (set.contains(tc))// Only validate unique items
					continue;
				set.add(tc);
				String id = tc.getCaseId();
				if (!quickIds.containsKey(id) || quickIds.get(id) == true) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Test case "+id+" does not exist or namespace does not match "+namespace);
					}
					msg.append("Test case: " + id + " does not exist or namespace does not match with worker\n");
				}
			}
			return msg.toString();
		}
	}

	public static void pauseAllSubJobs(String workerId, BatchJob job) {
		for (SubJobs jb : job.getSubJobs()) {
			GetJob pausingJob = getJob(workerId, jb.getId());
			if (pausingJob.isJobValidForRunning() && !pausingJob.isJobPausing()) {
				pausingJob.setJobPausing();
				updateJob(pausingJob);
			}
		}
	}

	public static void stopAllSubJobs(String workerId, BatchJob job) {
		for (SubJobs jb : job.getSubJobs()) {
			GetJob stopJob = getJob(workerId, jb.getId());
			if (stopJob.isJobValidForRunning() && !stopJob.isJobStopping()) {
				stopJob.setJobStopping();
				updateJob(stopJob);
			}
		}
	}

	/*
	 * 
	 * Utilities for removing things from database
	 * 
	 */

	public static void removeTestCase(String caseId) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting test case "+caseId+" from database");
		}
		getDatabase().remove(getTestCase(caseId));
	}

	public static void removeTestCase(GetTestCase testCase) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting test case "+testCase.getId()+" from database");
		}
		getDatabase().remove(testCase);
	}

	public static void removeWorker(String workerId) throws IOException {
		// Get worker from db to remove jobs from its jobs array
		GetWorker worker = getWorker(workerId);
		removeWorker(worker);
	}

	public static void removeWorker(GetWorker worker) throws IOException {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting worker "+worker.getId());
		}
		verifyDesignDocExistence();
		// Set all jobs to be deleted
		ListIterator<GetWorkerJobs> iter = worker.getJobs().listIterator();
		while(iter.hasNext()) {
			GetWorkerJobs job=iter.next();
			try {
				GetJob jb = getJob(worker.getId(), job.getJobID());
				CloudantUtils.removeJob(jb);
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "When deleting from worker "+worker.getId()+" the job "+job.getJobID()+" an error occurred. Leaving to deletion helper to delete it if not missing.",e);
				}
				if (!(e instanceof NoDocumentException)) {
					executor.submit(new DeletionHelper(worker.getId()+"_"+job.getJobID(), "job"));
				}
			}
		}
		iter = worker.getBatchJobs().listIterator();
		while(iter.hasNext()) {
			GetWorkerJobs job=iter.next();
			try {
				BatchJob jb = CloudantUtils.getBatchJob(worker.getId(), job.getJobID());
				if(jb.getSubJobs().size()<jb.getConcurrentJobs()) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "When deleting from worker "+worker.getId()+" the batch job "+job.getJobID()+" did not contain all sub jobs. Leaving to deletion helper to delete it.");
					}
					executor.submit(new DeletionHelper(worker.getId()+"_"+job.getJobID(), "batchJob"));
				}
				else {
					removeBatchJob(worker, jb);
				}
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "When deleting from worker "+worker.getId()+" the batch job "+job.getJobID()+" an error occurred.",e);
				}
			}
		}
		// Remove worker
		getDatabase().remove(worker);
	}

	public static void removeJob(GetJob job) {
		if (cache != null)
			cache.removeJobFromCache(job.getWorkerUsed().getId(),job.getId());
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting job "+job.getId()+" from database");
		}
		getDatabase().remove(job);
	}

	public static void removeBatchJob(String workerId, BatchJob job) {
		if(job.getSubJobs().size()<job.getConcurrentJobs()) {
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "When deleting from worker "+workerId+" the batch job "+job.getId()+" did not contain all sub jobs. Leaving to deletion helper to delete it.");
			}
			executor.submit(new DeletionHelper(workerId+"_"+job.getId(), "batchJob"));
		}
		else {
			for (SubJobs jb : job.getSubJobs()) {
				try {
					GetJob jobToRemove = getJob(workerId, jb.getId());
					removeJob(jobToRemove);
				} catch (Exception e) {
					if(log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "Batch job "+job.getId()+" sub job "+jb.getId()+" could not be deleted. Leaving to deletion helper to delete it if not missing.",e);
					}
					if (!(e instanceof NoDocumentException)) {
						executor.submit(new DeletionHelper(workerId+"_"+jb.getId(), "job"));
					}
				}
			}
			if(log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "Deleting batch job "+job.getId()+" from database");
			}
			getDatabase().remove(job);
		}
		GetWorker worker = getWorker(workerId);
		worker.getBatchJobs().remove(new GetWorkerJobs().jobID(job.getId()));
		updateWorker(worker);
	}

	public static void removeBatchJob(GetWorker worker, BatchJob job) {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting batch job "+job.getId()+" starting to remove all sub jobs in it");
		}
		for (SubJobs jb : job.getSubJobs()) {
			try {
				GetJob jobToRemove = getJob(worker.getId(), jb.getId());
				removeJob(jobToRemove);
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Batch job "+job.getId()+" sub job "+jb.getId()+" could not be deleted. Leaving to deletion helper to delete it if not missing.",e);
				}
				if (!(e instanceof NoDocumentException)) {
					executor.submit(new DeletionHelper(worker.getId()+"_"+jb.getId(), "job"));
				}
			}
		}
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting job "+job.getId()+" from database");
		}
		getDatabase().remove(job);
	}
	
//	public static void executorRemoveBatchJob(String workerId, BatchJob job) {
//		if(log.isLoggable(Level.FINEST)) {
//			log.log(Level.FINEST, "Deleting batch job "+job.getId()+" starting to remove all sub jobs in it");
//		}
//		boolean sucesful=true;
//		for (SubJobs jb : job.getSubJobs()) {
//			try {
//				GetJob jobToRemove = getJob(workerId, jb.getId());
//				removeJob(jobToRemove);
//			} catch (Exception e) {
//				sucesful=false;
//				if(log.isLoggable(Level.FINEST)) {
//					log.log(Level.FINEST, "Batch job "+job.getId()+" sub job "+jb.getId()+" could not be deleted. Leaving to deletion helper to delete it if not missing.",e);
//				}
//				if (!(e instanceof NoDocumentException)) {
//					executor.submit(new DeletionHelper(workerId+"_"+jb.getId(), "job"));
//				}
//			}
//		}
//		if(sucesful) {
//			if(log.isLoggable(Level.FINEST)) {
//				log.log(Level.FINEST, "Sucesfully deleted all subjobs from batch job "+job.getId()+" from database. Removing batch job");
//			}
//			getDatabase().remove(job);
//		}
//	}

	
	public static Response removeJob(GetWorker worker, GetJob job) {
		// Get job from db to remove
		worker.getJobs().remove(new GetWorkerJobs().jobID(job.getId()));
		removeJob(job);
		updateWorker(worker);
		return Response.ok().build();
	}

	public static String removeAllTestCasesInNamespace(String namespace) throws IOException {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting all test cases in namespace "+namespace);
		}
		verifyDesignDocExistence();
		StringBuilder sb = new StringBuilder();
		for (GetTestCase test : getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, TEST_CASE_GROUP_VIEW)
				.newRequest(Key.Type.STRING, Object.class).keys(namespace).includeDocs(true).build().getResponse()
				.getDocsAs(GetTestCase.class)) {
			try {
				removeTestCase(test);
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Test case "+test.getId()+" from namespace "+namespace+" could not be deleted. An error occurred. Leaving to deletion helper to delete it if not missing.",e);
				}
				if (!(e instanceof NoDocumentException)) {
					executor.submit(new DeletionHelper(test.getId(), "testCase"));
				}
				sb.append(e.getMessage());
			}
		}
		return sb.toString();
	}

	public static String removeAllWorkersInNamespace(String namespace) throws IOException {
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Deleting all workers in namespace "+namespace);
		}
		verifyDesignDocExistence();
		StringBuilder sb = new StringBuilder();
		for (GetWorker worker : getDatabase().getViewRequestBuilder(DESIGN_DOC_NAME, WORKER_GROUP_VIEW)
				.newRequest(Key.Type.STRING, Object.class).keys(namespace).includeDocs(true).build().getResponse()
				.getDocsAs(GetWorker.class)) {
			try {
				removeWorker(worker);
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Worker "+worker.getId()+" from namespace "+namespace+" could not be deleted. An error occurred. Leaving to deletion helper to delete it if not missing.",e);
					if (!(e instanceof NoDocumentException)) {
						executor.submit(new DeletionHelper(worker.getId(), "worker"));
					}
				}
				sb.append(e.getMessage());
			}
		}
		return sb.toString();
	}

	public static void removeAllJobsInWorker(String workerId) throws IOException {
		GetWorker worker = getWorker(workerId);
		if (worker.getJobs().size() == 0)
			return;
		// Delete everything and let the SOE fail if job not found
		ListIterator<GetWorkerJobs> iter = worker.getJobs().listIterator();
		while(iter.hasNext()) {
			GetWorkerJobs job=iter.next();
			try {
				GetJob jb = getJob(workerId, job.getJobID());
				CloudantUtils.removeJob(jb);
				iter.remove();
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "When deleting from worker "+worker.getId()+" the job "+job.getJobID()+" an error occurred. Leaving to deletion helper to delete it.",e);
				}
				if (!(e instanceof NoDocumentException)) {
					executor.submit(new DeletionHelper(worker.getId()+"_"+job.getJobID(), "job"));
				}
			}
		}
		updateWorker(worker);
	}

	public static void removeAllBatchJobsInWorker(String workerId) throws IOException {
		GetWorker worker = getWorker(workerId);
		if (worker.getBatchJobs().size() == 0)
			return;
		if(log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Go request to delete all batch jobs in worker "+worker.getId()+". Leaving deletion to deletion helper");
		}
		// Delete everything and let the SOE fail if job not found
		ListIterator<GetWorkerJobs> iter = worker.getBatchJobs().listIterator();
		while(iter.hasNext()) {
			GetWorkerJobs job=iter.next();
			try {
				executor.submit(new DeletionHelper(worker.getId()+"_"+job.getJobID(), "batchJob"));
				iter.remove();
			} catch (Exception e) {
				if(log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "When deleting from worker "+worker.getId()+" the batch job "+job.getJobID()+" an error occurred.",e);
				}
			}
		}
		updateWorker(worker);
	}

}
