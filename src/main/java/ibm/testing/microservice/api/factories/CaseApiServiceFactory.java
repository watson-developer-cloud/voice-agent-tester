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

import ibm.testing.microservice.api.CaseApiService;
import ibm.testing.microservice.api.impl.CaseApiServiceImpl;

public class CaseApiServiceFactory {
    private final static CaseApiService service = new CaseApiServiceImpl();

    public static CaseApiService getCaseApi() {
        return service;
    }
}
