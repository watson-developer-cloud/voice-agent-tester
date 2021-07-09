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
package ibm.testing.microservice.api.rest.endpoints;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletConfig;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ibm.testing.microservice.api.CaseApiService;
import ibm.testing.microservice.api.factories.CaseApiServiceFactory;
import ibm.testing.microservice.models.CreateTestCase;
import ibm.testing.microservice.models.GetTestCase;
import ibm.testing.microservice.models.Model201;

@Path("/v1/case")
@Tag(ref = "Test Case")
public class CaseApi {
	private final CaseApiService delegate;
	protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	public CaseApi(@Context ServletConfig servletContext) {
		CaseApiService delegate = null;

		if (servletContext != null) {
			String implClass = servletContext.getInitParameter("CaseApi.implementation");
			if (implClass != null && !"".equals(implClass.trim())) {
				try {
					delegate = (CaseApiService) Class.forName(implClass).newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (delegate == null) {
			delegate = CaseApiServiceFactory.getCaseApi();
		}

		this.delegate = delegate;
	}

	@POST
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Create a test case", description = "Creates a new Test Case which defines the conversation that will run when testing")
	@APIResponses(value = {
			@APIResponse(responseCode = "201", description = "Sucessfully created new test case", content = @Content(schema = @Schema(implementation = Model201.class))),
			@APIResponse(responseCode = "400", description = "Invalid Test Case"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response casePost(
			@RequestBody(description = "", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateTestCase.class))) CreateTestCase body,
			@Context SecurityContext securityContext){
		if (body == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Body is required. Must not be null.")
					.type(MediaType.TEXT_PLAIN).build();
		StringBuilder sb = new StringBuilder("");
		for (ConstraintViolation<CreateTestCase> err : validator.validate(body))
			sb.append(err.getMessage() + "\n");
		if (!sb.toString().isEmpty())
			return Response.status(Response.Status.BAD_REQUEST).entity(sb.toString()).type(MediaType.TEXT_PLAIN)
					.build();
		return delegate.casePost(body, securityContext);
	}

	@GET
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get all test cases", description = "Returns an array of all test cases that belong to the namespace specified")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Sucessfully got all test cases", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = GetTestCase.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response caseGet(
			@Parameter(description = "Identifier for mapping user/role to resource", required = false) @DefaultValue("default") @QueryParam("namespace") String namespace,
			@Context SecurityContext securityContext){
		return delegate.caseGet(namespace, securityContext);
	}

	@DELETE
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete all test cases", description = "Deletes all the test cases that belong to the namespace specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted all test cases"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response caseDelete(
			@Parameter(description = "Identifier for mapping user/role to resource", required = false) @DefaultValue("default") @QueryParam("namespace") String namespace,
			@Context SecurityContext securityContext){
		return delegate.caseDelete(namespace, securityContext);
	}

	@GET
	@Path("/{case-id}")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get a test case", description = "Returns the test case that corresponds to the ID specified")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Sucesfully got test case", content = @Content(schema = @Schema(implementation = GetTestCase.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "404", description = "Test case not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response caseCaseIdGet(
			@Parameter(description = "The ID of an existing test case", required = true) @PathParam("case-id") String caseId,
			@Context SecurityContext securityContext){
		return delegate.caseCaseIdGet(caseId, securityContext);
	}

	@PUT
	@Path("/{case-id}")
	@Consumes({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Modify a test case", description = "Modify the test case that corresponds to the ID specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully updated test case"),
			@APIResponse(responseCode = "400", description = "Invalid test case"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Test case not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response caseCaseIdPut(
			@RequestBody(description = "", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateTestCase.class))) CreateTestCase body,
			@Parameter(description = "The ID of an existing test case", required = true) @PathParam("case-id") String caseId,
			@Context SecurityContext securityContext){
		if (body == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Body is required. Must not be null.")
					.type(MediaType.TEXT_PLAIN).build();
		StringBuilder sb = new StringBuilder("");
		if(body.getNamespace()!="default")
			return Response.status(Response.Status.BAD_REQUEST).entity("Namespace should not be provided when modifying test case").type(MediaType.TEXT_PLAIN).build();
		for (ConstraintViolation<CreateTestCase> err : validator.validate(body))
			sb.append(err.getMessage() + "\n");
		if (!sb.toString().isEmpty())
			return Response.status(Response.Status.BAD_REQUEST).entity(sb.toString()).type(MediaType.TEXT_PLAIN)
					.build();
		return delegate.caseCaseIdPut(body, caseId, securityContext);
	}

	@DELETE
	@Path("/{case-id}")
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete a test case", description = "Deletes the test case that corresponds to the ID specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted test case"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Test case not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response caseCaseIdDelete(
			@Parameter(description = "The ID of an existing test case", required = true) @PathParam("case-id") String caseId,
			@Context SecurityContext securityContext){
		return delegate.caseCaseIdDelete(caseId, securityContext);
	}

}
