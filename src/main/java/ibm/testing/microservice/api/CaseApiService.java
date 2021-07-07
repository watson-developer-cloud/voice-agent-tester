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
package ibm.testing.microservice.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import ibm.testing.microservice.models.CreateTestCase;

public abstract class CaseApiService {
    public abstract Response caseCaseIdDelete(String caseId,SecurityContext securityContext);
    public abstract Response caseCaseIdGet(String caseId,SecurityContext securityContext);
    public abstract Response caseCaseIdPut(CreateTestCase body,String caseId,SecurityContext securityContext);
    public abstract Response caseDelete(String namespace,SecurityContext securityContext);
    public abstract Response caseGet(String namespace,SecurityContext securityContext);
    public abstract Response casePost(CreateTestCase body,SecurityContext securityContext);
}
