/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.server.translation.out.action;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.exceptions.IllegalAttributeValueException;
import pl.edu.icm.unity.server.api.AttributesManagement;
import pl.edu.icm.unity.server.translation.ActionParameterDesc;
import pl.edu.icm.unity.server.translation.ActionParameterDesc.Type;
import pl.edu.icm.unity.server.translation.TranslationActionDescription;
import pl.edu.icm.unity.server.translation.in.action.MapAttributeActionFactory;
import pl.edu.icm.unity.server.translation.out.AbstractOutputTranslationAction;
import pl.edu.icm.unity.server.translation.out.TranslationInput;
import pl.edu.icm.unity.server.translation.out.TranslationResult;
import pl.edu.icm.unity.server.utils.Log;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.basic.AttributeVisibility;
import pl.edu.icm.unity.types.confirmation.ConfirmationInfo;
import pl.edu.icm.unity.types.confirmation.VerifiableElement;

/**
 * Creates new outgoing attributes.
 *   
 * @author K. Benedyczak
 */
@Component
public class CreatePersistentAttributeActionFactory extends AbstractOutputTranslationActionFactory
{
	public static final String NAME = "createPersistentAttribute";
	private AttributesManagement attrsMan;
	
	@Autowired
	public CreatePersistentAttributeActionFactory(@Qualifier("insecure") AttributesManagement attrsMan)
	{
		super(NAME, new ActionParameterDesc[] {
				new ActionParameterDesc(
						"attributeName",
						"TranslationAction.createPersistentAttribute.paramDesc.attributeName",
						Type.UNITY_ATTRIBUTE),
				new ActionParameterDesc(
						"expression",
						"TranslationAction.createPersistentAttribute.paramDesc.expression",
						Type.EXPRESSION),
				new ActionParameterDesc(
						"group",
						"TranslationAction.createPersistentAttribute.paramDesc.group",
						Type.UNITY_GROUP)
		});
		this.attrsMan = attrsMan;
	}

	@Override
	public CreatePersistentAttributeAction getInstance(String... parameters) throws EngineException
	{
		return new CreatePersistentAttributeAction(parameters, this, attrsMan);
	}
	
	public static class CreatePersistentAttributeAction extends AbstractOutputTranslationAction
	{
		private static final Logger log = Log.getLogger(Log.U_SERVER_TRANSLATION, CreatePersistentAttributeAction.class);
		private String attrNameString;
		private AttributeType attributeType;
		private Serializable valuesExpression;
		private String group;

		public CreatePersistentAttributeAction(String[] params, TranslationActionDescription desc, 
				AttributesManagement attrsMan) throws EngineException
		{
			super(desc, params);
			setParameters(params, attrsMan);
		}

		@Override
		protected void invokeWrapped(TranslationInput input, Object mvelCtx, String currentProfile,
				TranslationResult result) throws EngineException
		{
			Object value = MVEL.executeExpression(valuesExpression, mvelCtx);
			if (value == null)
			{
				log.debug("Attribute value evaluated to null, skipping");
				return;
			}
			for (Attribute<?> existing: result.getAttributes())
			{
				if (existing.getName().equals(attrNameString))
				{
					log.trace("Attribute already exists, skipping");
					return;
				}
			}

			List<Object> typedValues;
			try
			{
				typedValues = MapAttributeActionFactory.MapAttributeAction.convertValues(
						value, attributeType.getValueType());
			} catch (IllegalAttributeValueException e)
			{
				log.debug("Can't convert attribute values returned by the action's expression "
						+ "to the type of attribute " + attrNameString + " , skipping it", e);
				return;
			}
			//for output profile we can't confirm - not yet implemented and rather not needed.
			for (Object val: typedValues)
			{
				if (val instanceof VerifiableElement)
				{
					((VerifiableElement) val).setConfirmationInfo(new ConfirmationInfo(true));
				}
			}

			@SuppressWarnings({ "unchecked", "rawtypes"})
			Attribute<?> newAttr = new Attribute(attrNameString, attributeType.getValueType(), group, 
					AttributeVisibility.full, typedValues, null, currentProfile);
			result.getAttributes().add(newAttr);
			result.getAttributesToPersist().add(newAttr);
			log.debug("Created a new persisted attribute: " + newAttr);
		}

		private void setParameters(String[] parameters, AttributesManagement attrsMan)
		{
			if (parameters.length != 3)
				throw new IllegalArgumentException("Action requires exactly 3 parameters");
			attrNameString = parameters[0];
			valuesExpression = MVEL.compileExpression(parameters[1]);
			group = parameters[2];

			try
			{
				attributeType = attrsMan.getAttributeTypesAsMap().get(attrNameString);
				if (attributeType == null)
					throw new IllegalArgumentException("The attribute type " + parameters[0] + 
							" is not a valid Unity attribute type and therefore can not be persisted");
				if (attributeType.isInstanceImmutable())
					throw new IllegalArgumentException("The attribute type " + parameters[0] + 
							" is managed internally only so it can not be persisted");
			} catch (EngineException e)
			{
				throw new IllegalStateException("Can not verify attribute type", e);
			}
		}

	}
}