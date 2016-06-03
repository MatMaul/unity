/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.engine.api.authn;

import com.fasterxml.jackson.databind.node.ObjectNode;

import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.exceptions.IllegalIdentityValueException;
import pl.edu.icm.unity.exceptions.TooManyAttempts;
import pl.edu.icm.unity.exceptions.WrongArgumentException;
import pl.edu.icm.unity.types.basic.IdentityTaV;

/**
 * Implementations provide a credential reset functionality. Typically are bound
 * to a particular credential type. Additionally implementation instances must be stateful
 * and maintain an identity for which the reset is performed.
 * 
 * @author K. Benedyczak
 */
public interface CredentialReset
{
	/**
	 * @return settings of credential reset
	 */
	public CredentialResetSettings getSettings();

	/**
	 * Sets the subject for which the operations are made.
	 * @param subject
	 */
	public void setSubject(IdentityTaV subject);
	
	/**
	 * 
	 * @return the question for the subject, which must be set before. If the question is
	 * not defined, a random question is returned, from the configured ones.
	 */
	public String getSecurityQuestion();

	/**
	 * Verifies if the static data is about the subject is correct. Currently only the security question
	 * answer.  
	 * @param aswer
	 * @throws WrongArgumentException
	 * @throws IllegalIdentityValueException
	 */
	public void verifyStaticData(String aswer) throws WrongArgumentException,
			IllegalIdentityValueException, TooManyAttempts;

	/**
	 * Verifies if the dynamic data about the subject is correct. Currently only the email code.
	 * @param emailCode
	 * @throws WrongArgumentException
	 */
	public void verifyDynamicData(String emailCode) throws WrongArgumentException, TooManyAttempts;

	/**
	 * Sends a random confirmation code to the receiver.
	 * 
	 * @throws EngineException 
	 */
	public void sendCode() throws EngineException;

	/**
	 * @return JSON with the current credential configuration of the subject
	 */
	public ObjectNode getCredentialConfiguration();

	/**
	 * Changes the credential to a new one.
	 * 
	 * @param newCredential new credential, typically encoded in credential
	 *                specific way.
	 * @throws EngineException 
	 */
	public void updateCredential(String newCredential) throws EngineException;
}
