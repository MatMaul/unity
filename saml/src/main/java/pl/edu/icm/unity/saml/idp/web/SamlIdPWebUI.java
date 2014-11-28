/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.saml.idp.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.idpcommon.EopException;
import pl.edu.icm.unity.saml.SAMLEndpointDefinition;
import pl.edu.icm.unity.saml.SAMLSessionParticipant;
import pl.edu.icm.unity.saml.idp.FreemarkerHandler;
import pl.edu.icm.unity.saml.idp.SamlIdpProperties;
import pl.edu.icm.unity.saml.idp.ctx.SAMLAuthnContext;
import pl.edu.icm.unity.saml.idp.preferences.SamlPreferences;
import pl.edu.icm.unity.saml.idp.preferences.SamlPreferences.SPSettings;
import pl.edu.icm.unity.saml.idp.processor.AuthnResponseProcessor;
import pl.edu.icm.unity.server.api.PreferencesManagement;
import pl.edu.icm.unity.server.api.internal.IdPEngine;
import pl.edu.icm.unity.server.api.internal.LoginSession;
import pl.edu.icm.unity.server.authn.AuthenticationException;
import pl.edu.icm.unity.server.authn.InvocationContext;
import pl.edu.icm.unity.server.endpoint.BindingAuthn;
import pl.edu.icm.unity.server.registries.IdentityTypesRegistry;
import pl.edu.icm.unity.server.translation.out.TranslationResult;
import pl.edu.icm.unity.server.utils.Log;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.EntityParam;
import pl.edu.icm.unity.types.basic.IdentityParam;
import pl.edu.icm.unity.types.endpoint.EndpointDescription;
import pl.edu.icm.unity.webui.EndpointRegistrationConfiguration;
import pl.edu.icm.unity.webui.UnityUIBase;
import pl.edu.icm.unity.webui.UnityWebUI;
import pl.edu.icm.unity.webui.authn.AuthenticationProcessor;
import pl.edu.icm.unity.webui.common.HtmlTag;
import pl.edu.icm.unity.webui.common.SafePanel;
import pl.edu.icm.unity.webui.common.TopHeaderLight;
import pl.edu.icm.unity.webui.common.attributes.AttributeHandlerRegistry;
import pl.edu.icm.unity.webui.common.provider.ExposedAttributesComponent;
import pl.edu.icm.unity.webui.common.provider.IdPButtonsBar;
import pl.edu.icm.unity.webui.common.provider.IdPButtonsBar.Action;
import pl.edu.icm.unity.webui.common.provider.IdentitySelectorComponent;
import pl.edu.icm.unity.webui.common.provider.SPInfoComponent;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import eu.unicore.samly2.SAMLConstants;
import eu.unicore.samly2.exceptions.SAMLRequesterException;

/**
 * The main UI of the SAML web IdP. Fairly simple: shows who asks, what is going to be sent,
 * and optionally allows for some customization. This UI is shown always after the user was authenticated
 * and when the SAML request was properly pre-processed.
 *  
 * @author K. Benedyczak
 */
@Component("SamlIdPWebUI")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Theme("unityTheme")
public class SamlIdPWebUI extends UnityUIBase implements UnityWebUI
{
	private static Logger log = Log.getLogger(Log.U_SERVER_SAML, SamlIdPWebUI.class);
	protected UnityMessageSource msg;
	protected EndpointDescription endpointDescription;
	protected IdPEngine idpEngine;
	protected FreemarkerHandler freemarkerHandler;
	protected AttributeHandlerRegistry handlersRegistry;
	protected IdentityTypesRegistry identityTypesRegistry;
	protected PreferencesManagement preferencesMan;
	protected AuthenticationProcessor authnProcessor;
	protected IdentitySelectorComponent idSelector;
	protected ExposedAttributesComponent attrsPresenter;
	
	protected AuthnResponseProcessor samlProcessor;
	protected SamlResponseHandler samlResponseHandler;
	protected CheckBox rememberCB;

	@Autowired
	public SamlIdPWebUI(UnityMessageSource msg, FreemarkerHandler freemarkerHandler,
			AttributeHandlerRegistry handlersRegistry, PreferencesManagement preferencesMan,
			AuthenticationProcessor authnProcessor, IdPEngine idpEngine,
			IdentityTypesRegistry identityTypesRegistry)
	{
		super(msg);
		this.msg = msg;
		this.freemarkerHandler = freemarkerHandler;
		this.handlersRegistry = handlersRegistry;
		this.preferencesMan = preferencesMan;
		this.authnProcessor = authnProcessor;
		this.idpEngine = idpEngine;
		this.identityTypesRegistry = identityTypesRegistry;
	}

	@Override
	public void configure(EndpointDescription description,
			List<Map<String, BindingAuthn>> authenticators,
			EndpointRegistrationConfiguration regCfg)
	{
		this.endpointDescription = description;
	}
	
	protected TranslationResult getUserInfo(SAMLAuthnContext samlCtx, AuthnResponseProcessor processor) 
			throws EngineException
	{
		String profile = samlCtx.getSamlConfiguration().getValue(SamlIdpProperties.TRANSLATION_PROFILE);
		LoginSession ae = InvocationContext.getCurrent().getLoginSession();
		return idpEngine.obtainUserInformation(new EntityParam(ae.getEntityId()), 
				processor.getChosenGroup(), profile, 
				samlProcessor.getIdentityTarget(), "SAML2", SAMLConstants.BINDING_HTTP_REDIRECT,
				processor.isIdentityCreationAllowed());
	}

	@Override
	protected void appInit(VaadinRequest request)
	{
		SAMLAuthnContext samlCtx = SamlResponseHandler.getContext();
		samlProcessor = new AuthnResponseProcessor(samlCtx, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		samlResponseHandler = new SamlResponseHandler(freemarkerHandler, samlProcessor);
		
		VerticalLayout vmain = new VerticalLayout();
		TopHeaderLight header = new TopHeaderLight(endpointDescription.getId(), msg);
		vmain.addComponent(header);

		
		VerticalLayout contents = new VerticalLayout();
		contents.setSizeUndefined();
		contents.setMargin(true);
		contents.setSpacing(true);
		vmain.addComponent(contents);
		vmain.setComponentAlignment(contents, Alignment.TOP_CENTER);
		
		try
		{
			createInfoPart(samlCtx, contents);

			createExposedDataPart(samlCtx, contents);

			createButtonsPart(samlCtx, contents);

			setContent(vmain);

			loadPreferences(samlCtx);
		} catch (EopException e)
		{
			//OK
		}
	}

	protected void createInfoPart(SAMLAuthnContext samlCtx, VerticalLayout contents)
	{
		String samlRequester = samlCtx.getRequest().getIssuer().getStringValue();
		String returnAddress = samlCtx.getRequest().getAssertionConsumerServiceURL();
		if (returnAddress == null)
			returnAddress = samlCtx.getSamlConfiguration().getReturnAddressForRequester(
					samlCtx.getRequest().getIssuer());

		Label info1 = new Label(msg.getMessage("SamlIdPWebUI.info1"));
		info1.setStyleName(Reindeer.LABEL_H1);
		SPInfoComponent spInfo = new SPInfoComponent(msg, null, samlRequester, returnAddress);
		Label spc1 = HtmlTag.br();
		Label info2 = new Label(msg.getMessage("SamlIdPWebUI.info2"));
		
		contents.addComponents(info1, spInfo, spc1, info2);
	}

	protected void createExposedDataPart(SAMLAuthnContext samlCtx, VerticalLayout contents) throws EopException
	{
		SafePanel exposedInfoPanel = new SafePanel();
		contents.addComponent(exposedInfoPanel);
		VerticalLayout eiLayout = new VerticalLayout();
		eiLayout.setMargin(true);
		eiLayout.setSpacing(true);
		exposedInfoPanel.setContent(eiLayout);
		try
		{
			TranslationResult translationResult = getUserInfo(samlCtx, samlProcessor);
			createIdentityPart(translationResult, eiLayout);
			eiLayout.addComponent(HtmlTag.br());
			createAttributesPart(translationResult, eiLayout);
		} catch (SAMLRequesterException e)
		{
			//we kill the session as the user may want to log as different user if has access to several entities.
			log.debug("SAML problem when handling client request", e);
			samlResponseHandler.handleException(e, true);
			return;
		} catch (Exception e)
		{
			log.error("Engine problem when handling client request", e);
			//we kill the session as the user may want to log as different user if has access to several entities.
			samlResponseHandler.handleException(e, true);
			return;
		}
		
		rememberCB = new CheckBox(msg.getMessage("SamlIdPWebUI.rememberSettings"));
		contents.addComponent(rememberCB);
	}
	
	protected void createIdentityPart(TranslationResult translationResult, VerticalLayout contents) 
			throws EngineException, SAMLRequesterException
	{
		List<IdentityParam> validIdentities = samlProcessor.getCompatibleIdentities(
				translationResult.getIdentities());
		idSelector = new IdentitySelectorComponent(msg, identityTypesRegistry, validIdentities);
		contents.addComponent(idSelector);
	}
	
	protected void createAttributesPart(TranslationResult translationResult, VerticalLayout contents) throws EngineException
	{
		attrsPresenter = new ExposedAttributesComponent(msg, handlersRegistry, 
				translationResult.getAttributes(), true);
		contents.addComponent(attrsPresenter);
	}
	
	protected void createButtonsPart(final SAMLAuthnContext samlCtx, VerticalLayout contents)
	{
		IdPButtonsBar buttons = new IdPButtonsBar(msg, authnProcessor, new IdPButtonsBar.ActionListener()
		{
			@Override
			public void buttonClicked(Action action)
			{
				try
				{
					if (Action.ACCEPT == action)
						confirm(samlCtx);
					else if (Action.DENY == action)
						decline();
				} catch (EopException e) 
				{
					//OK
				}
			}
		});
		
		contents.addComponent(buttons);
		contents.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
	}
	
	
	protected void loadPreferences(SAMLAuthnContext samlCtx) throws EopException
	{
		try
		{
			SamlPreferences preferences = SamlPreferences.getPreferences(preferencesMan);
			SPSettings settings = preferences.getSPSettings(samlCtx.getRequest().getIssuer());
			updateUIFromPreferences(settings, samlCtx);
		} catch (EopException e)
		{
			throw e;
		} catch (Exception e)
		{
			log.error("Engine problem when processing stored preferences", e);
			//we kill the session as the user may want to log as different user if has access to several entities.
			samlResponseHandler.handleException(e, true);
			return;
		}
	}
	
	protected void updateUIFromPreferences(SPSettings settings, SAMLAuthnContext samlCtx) throws EngineException, EopException
	{
		if (settings == null)
			return;
		Set<String> hidden = settings.getHiddenAttribtues();
		attrsPresenter.setHidden(hidden);
		String selId = settings.getSelectedIdentity();
		idSelector.setSelected(selId);

		if (settings.isDoNotAsk())
		{
			if (settings.isDefaultAccept())
				confirm(samlCtx);
			else
				decline();
		}
	}
	
	/**
	 * Applies UI selected values to the given preferences object
	 * @param preferences
	 * @param samlCtx
	 * @param defaultAccept
	 * @throws EngineException
	 */
	protected void updatePreferencesFromUI(SamlPreferences preferences, SAMLAuthnContext samlCtx, boolean defaultAccept) 
			throws EngineException
	{
		if (!rememberCB.getValue())
			return;
		NameIDType reqIssuer = samlCtx.getRequest().getIssuer();
		SPSettings settings = preferences.getSPSettings(reqIssuer);
		settings.setDefaultAccept(defaultAccept);
		settings.setDoNotAsk(true);
		
		settings.setHiddenAttribtues(attrsPresenter.getHidden());

		String identityValue = idSelector.getSelectedIdentityForPreferences();
		if (identityValue != null)
			settings.setSelectedIdentity(identityValue);
		preferences.setSPSettings(reqIssuer, settings);
	}
	
	protected void storePreferences(boolean defaultAccept)
	{
		try
		{
			SAMLAuthnContext samlCtx = SamlResponseHandler.getContext();
			SamlPreferences preferences = SamlPreferences.getPreferences(preferencesMan);
			updatePreferencesFromUI(preferences, samlCtx, defaultAccept);
			SamlPreferences.savePreferences(preferencesMan, preferences);
		} catch (EngineException e)
		{
			log.error("Unable to store user's preferences", e);
		}
	}

	protected void decline() throws EopException
	{
		storePreferences(false);
		AuthenticationException ea = new AuthenticationException("Authentication was declined");
		samlResponseHandler.handleException(ea, false);
	}
	
	protected void confirm(SAMLAuthnContext samlCtx) throws EopException
	{
		storePreferences(true);
		ResponseDocument respDoc;
		try
		{
			Collection<Attribute<?>> attributes = attrsPresenter.getUserFilteredAttributes();
			respDoc = samlProcessor.processAuthnRequest(idSelector.getSelectedIdentity(), attributes);
		} catch (Exception e)
		{
			samlResponseHandler.handleException(e, false);
			return;
		}
		addSessionParticipant(samlCtx, samlProcessor.getAuthenticatedSubject().getNameID(), 
				samlProcessor.getSessionId());
		samlResponseHandler.returnSamlResponse(respDoc);
	}
	
	protected void addSessionParticipant(SAMLAuthnContext samlCtx, NameIDType returnedSubject,
			String sessionId)
	{
		String participantId = samlCtx.getRequest().getIssuer().getStringValue();
		SamlIdpProperties samlIdpProperties = samlCtx.getSamlConfiguration();
		String configKey = samlIdpProperties.getSPConfigKey(samlCtx.getRequest().getIssuer());
		String localIdpSamlId = samlIdpProperties.getValue(SamlIdpProperties.ISSUER_URI);
		List<SAMLEndpointDefinition> logoutEndpoints = configKey == null ? 
				new ArrayList<SAMLEndpointDefinition>(0) :
				samlCtx.getSamlConfiguration().getLogoutEndpointsFromStructuredList(configKey);
		authnProcessor.addSessionParticipant(new SAMLSessionParticipant(participantId, 
				returnedSubject, sessionId, logoutEndpoints, localIdpSamlId));
	}
}
