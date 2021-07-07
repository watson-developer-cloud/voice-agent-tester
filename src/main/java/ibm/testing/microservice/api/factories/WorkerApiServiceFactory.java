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
package ibm.testing.microservice.api.factories;

import ibm.testing.microservice.api.WorkerApiService;
import ibm.testing.microservice.api.impl.WorkerApiServiceImpl;

public class WorkerApiServiceFactory {
    private final static WorkerApiService service = new WorkerApiServiceImpl();

    public static WorkerApiService getWorkerApi() {
        return service;
    }
}
