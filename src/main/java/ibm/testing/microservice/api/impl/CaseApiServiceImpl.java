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
package ibm.testing.microservice.api.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.cloudant.client.org.lightcouch.NoDocumentException;

import ibm.testing.microservice.api.CaseApiService;
import ibm.testing.microservice.api.utils.CloudantUtils;
import ibm.testing.microservice.api.utils.Messages;
import ibm.testing.microservice.models.CreateTestCase;
import ibm.testing.microservice.models.GetTestCase;
import ibm.testing.microservice.models.Model201;


public class CaseApiServiceImpl extends CaseApiService {
	
	private static Logger log = Logger.getLogger(CaseApiServiceImpl.class.getName());
	
	@Override
	public Response caseCaseIdDelete(String caseId, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0004I",caseId));
			CloudantUtils.removeTestCase(caseId);
			return Response.status(Response.Status.OK).build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",caseId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response caseCaseIdGet(String caseId, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0005I",caseId));
			GetTestCase tc= CloudantUtils.getTestCase(caseId);
			String JsonRepresentation = JsonbBuilder.create().toJson(tc); 
			return Response.ok(JsonRepresentation,MediaType.APPLICATION_JSON).build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",caseId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response caseCaseIdPut(CreateTestCase body, String caseId, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0006I",caseId));
			GetTestCase test = CloudantUtils.getTestCase(caseId);
			test.name(body.getName()).description(body.getDescription()).turns(body.getTurns());
			CloudantUtils.updateTestCase(test);
			return Response.ok().build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",caseId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response caseDelete(String namespace, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0007I",namespace));
			String errors=CloudantUtils.removeAllTestCasesInNamespace(namespace);
			if(errors.isEmpty())
				return Response.ok().build();
			return Response.status(Response.Status.CONFLICT).entity(errors).type(MediaType.TEXT_PLAIN).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response caseGet(String namespace, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0008I",namespace));
			List<GetTestCase>l=CloudantUtils.getTestCasesInNamespace(namespace);
			String JsonRepresentation = JsonbBuilder.create().toJson(l); 
			return Response.ok(JsonRepresentation).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response casePost(CreateTestCase body, SecurityContext securityContext){
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0009I",body.getNamespace()));
			com.cloudant.client.api.model.Response creation = CloudantUtils.createTestCase(body);
			return Response.status(Response.Status.CREATED).entity(new Model201().id(creation.getId())).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
