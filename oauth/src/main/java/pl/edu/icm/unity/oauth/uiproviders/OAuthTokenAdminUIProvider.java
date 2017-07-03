/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.oauth.uiproviders;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.vaadin.ui.Component;

import pl.edu.icm.unity.engine.api.AttributesManagement;
import pl.edu.icm.unity.engine.api.attributes.AttributeSupport;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.token.SecuredTokensManagement;
import pl.edu.icm.unity.webui.providers.AdminUITabProvider;

/**
 * Provides OAuthToken UI component to home UI
 * @author P.Piernik
 *
 */
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OAuthTokenAdminUIProvider implements AdminUITabProvider
{	
	public static final String ID = "oauthAdminTokens";
	private SecuredTokensManagement tokenMan;
	private UnityMessageSource msg;
	private AttributeSupport attrProcessor;
	private AttributesManagement attrMan;
	
	
	
	public OAuthTokenAdminUIProvider(SecuredTokensManagement tokenMan, UnityMessageSource msg,
			AttributeSupport attrProcessor, AttributesManagement attrMan)
	{
		this.tokenMan = tokenMan;
		this.msg = msg;
		this.attrProcessor = attrProcessor;
		this.attrMan = attrMan;
	}



	@Override
	public Component getUI()
	{
		return new AdminTokensComponent(tokenMan, msg, attrProcessor, attrMan);
	}



	@Override
	public String getLabelKey()
	{
		return "OAuthTokenAdminUI.tokensLabel";
	}



	@Override
	public String getDescriptionKey()
	{
		return "OAuthTokenAdminUI.tokensDesc";
	}



	@Override
	public String getId()
	{
		return ID;
	}

}
