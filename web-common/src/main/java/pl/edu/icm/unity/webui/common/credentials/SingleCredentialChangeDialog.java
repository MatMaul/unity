/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.common.credentials;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;

import pl.edu.icm.unity.engine.api.CredentialManagement;
import pl.edu.icm.unity.engine.api.CredentialRequirementManagement;
import pl.edu.icm.unity.engine.api.EntityCredentialManagement;
import pl.edu.icm.unity.engine.api.EntityManagement;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.exceptions.InternalException;
import pl.edu.icm.unity.types.authn.CredentialDefinition;
import pl.edu.icm.unity.webui.authn.additional.AdditionalAuthnHandler;
import pl.edu.icm.unity.webui.common.AbstractDialog;
import pl.edu.icm.unity.webui.common.NotificationPopup;


/**
 * Allows to change a single credential.
 * @author K. Benedyczak
 */
@PrototypeComponent
public class SingleCredentialChangeDialog extends AbstractDialog
{
	private CredentialManagement credMan;
	private EntityCredentialManagement ecredMan;
	private EntityManagement entityMan;
	private CredentialEditorRegistry credEditorReg;
	private final AdditionalAuthnHandler additionalAuthnHandler;
	
	private Callback callback;
	private long entityId;
	private boolean simpleMode;
	private String credentialId;
	private SingleCredentialPanel ui;
	
	@Autowired
	public SingleCredentialChangeDialog(AdditionalAuthnHandler additionalAuthnHandler,
			UnityMessageSource msg, CredentialManagement credMan,
			EntityCredentialManagement ecredMan, EntityManagement entityMan,
			CredentialRequirementManagement credReqMan,
			CredentialEditorRegistry credEditorReg)
	{
		super(msg, msg.getMessage("CredentialChangeDialog.caption"),
				msg.getMessage("update"), msg.getMessage("cancel"));
		this.additionalAuthnHandler = additionalAuthnHandler;
		this.credMan = credMan;
		this.ecredMan = ecredMan;
		this.entityMan = entityMan;
		this.credEditorReg = credEditorReg;
		setSizeEm(45, 36);
	}

	public SingleCredentialChangeDialog init(long entityId, boolean simpleMode,
			Callback callback, String credentialId)
	{
		this.entityId = entityId;
		this.callback = callback;
		this.simpleMode = simpleMode;
		this.credentialId = credentialId;
		return this;
	}

	@Override
	protected Component getContents() throws Exception
	{
		try
		{
			Collection<CredentialDefinition> allCreds = credMan
					.getCredentialDefinitions();
			CredentialDefinition credDef = null;
			for (CredentialDefinition cd : allCreds)
			{
				if (cd.getName().equals(credentialId))
					credDef = cd;
			}
			if (credDef == null)
				throw new InternalException(msg.getMessage(
						"CredentialChangeDialog.cantGetCredDefs")
						+ credentialId);

			ui = new SingleCredentialPanel(additionalAuthnHandler, msg, entityId, ecredMan, credMan, entityMan,
					credEditorReg, credDef, simpleMode, false, null);
		} catch (EngineException e)
		{
			NotificationPopup.showError(msg, msg.getMessage("error"), e);
			throw e;
		}

		return ui;
	}
	
	@Override
	protected AbstractField<?> getFocussedComponent()
	{
		return ui.getFocussedComponent();
	}
	
	@Override
	protected void onConfirm()
	{
		boolean changed = ui.updateCredential(false);
		if (!changed)
			return;
		close();
		callback.onClose(true);
	}

	@Override
	protected void onCancel()
	{
		close();
		callback.onClose(false);
	}

	public interface Callback
	{
		public void onClose(boolean changed);
	}
}
