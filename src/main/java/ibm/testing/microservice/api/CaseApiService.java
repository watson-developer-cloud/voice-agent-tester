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
