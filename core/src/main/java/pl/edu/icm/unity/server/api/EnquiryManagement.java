/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.server.api;

import java.util.List;

import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.types.registration.EnquiryForm;
import pl.edu.icm.unity.types.registration.EnquiryResponse;
import pl.edu.icm.unity.types.registration.RegistrationContext;
import pl.edu.icm.unity.types.registration.RegistrationRequestAction;

/**
 * Enquires support: forms, submissions of requests and their processing.
 * @author K. Benedyczak
 */
public interface EnquiryManagement
{
	/**
	 * Add a new enquiry form.
	 * @param form
	 * @throws EngineException
	 */
	void addEnquiry(EnquiryForm form) throws EngineException;
	
	/**
	 * Triggers a (re?)send of enquiry notification message.
	 * The message will be send only for those who has not yet filled the enquiry.
	 *
	 * @param enquiryId
	 * @throws EngineException
	 */
	void sendEnquiry(String enquiryId) throws EngineException;
	
	/**
	 * Remove an existing enquiry form.
	 * @param formId
	 * @throws EngineException
	 */
	void removeEnquiry(String formId) throws EngineException;
	
	/**
	 * Updates an existing enquiry form. Will be applicable only to those users who has not yet filled the 
	 * original enquiry.
	 * @param updatedForm
	 * @throws EngineException
	 */
	void updateEnquiry(EnquiryForm updatedForm) throws EngineException;
	
	/**
	 * Accepts, deletes or rejects a given enquiry response. The request can be freely modified at this time
	 * too, with one exception: the credentials originally submitted are always preserved.
	 * @param id request id to be processed
	 * @param finalRequest updated request with edits made by admin
	 * @param action what to do with the request.
	 * @param publicComment comment to be recorded and sent to the requester
	 * @param privateComment comment to be internally recored only.
	 * @throws EngineException
	 */
	void processEnquiryResponse(String id, EnquiryResponse finalResponse, 
			RegistrationRequestAction action, String publicComment, 
			String privateComment) throws EngineException;
	
	/**
	 * 
	 * @return all available enquires.
	 * @throws EngineException
	 */
	List<EnquiryForm> getEnquires() throws EngineException;
	
	/**
	 * Submits an enquiry response.
	 * @param response
	 * @param context submission context
	 * @return id of the recorder response
	 * @throws EngineException
	 */
	String submitEnquiryResponse(EnquiryResponse response, RegistrationContext context) throws EngineException;
}