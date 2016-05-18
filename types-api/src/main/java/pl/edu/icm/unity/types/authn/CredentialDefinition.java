/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.types.authn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pl.edu.icm.unity.Constants;
import pl.edu.icm.unity.MessageSource;
import pl.edu.icm.unity.types.I18nDescribedObject;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.I18nStringJsonUtil;
import pl.edu.icm.unity.types.NamedObject;


/**
 * Configured instance of {@link CredentialType}.
 * 
 * @author K. Benedyczak
 */
public class CredentialDefinition extends I18nDescribedObject implements NamedObject
{
	private String name;
	private String typeId;
	private String jsonConfiguration;

	public CredentialDefinition()
	{
		super();
	}

	/**
	 * For tests: the displayed name is set to name (via default value) and description is set empty.
	 * @param typeId
	 * @param name
	 */
	public CredentialDefinition(String typeId, String name)
	{
		this(typeId, name, new I18nString(name), new I18nString(""));
	}
	
	/**
	 * Creates a standard CredentialDefinition with displayed name loaded from message bundle
	 * (using the typeId as key).
	 * @param typeId
	 * @param name
	 * @param msg
	 */
	public CredentialDefinition(String typeId, String name, I18nString description, MessageSource msg)
	{
		this(typeId, name, loadNames(typeId, msg), description);
	}
	
	public CredentialDefinition(String typeId, String name, I18nString displayedName, I18nString description)
	{
		super(displayedName, description);
		this.typeId = typeId;
		this.name = name;
	}
	
	@JsonCreator
	public CredentialDefinition(ObjectNode root)
	{
		fromJson(root);
	}
	
	private static I18nString loadNames(String name, MessageSource msg)
	{
		return new I18nString("CredDef." + name + ".displayedName", msg);
	}
	
	public String getTypeId()
	{
		return typeId;
	}
	public void setTypeId(String typeId)
	{
		this.typeId = typeId;
	}
	public String getJsonConfiguration()
	{
		return jsonConfiguration;
	}
	public void setJsonConfiguration(String jsonConfiguration)
	{
		this.jsonConfiguration = jsonConfiguration;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@JsonValue
	public ObjectNode toJson()
	{
		ObjectNode root = Constants.MAPPER.createObjectNode();
		root.put("typeId", getTypeId());
		root.put("name", getName());
		root.put("jsonConfiguration", getJsonConfiguration());
		root.set("displayedName", I18nStringJsonUtil.toJson(getDisplayedName()));
		root.set("i18nDescription", I18nStringJsonUtil.toJson(getDescription()));
		return root;
	}
	
	private void fromJson(ObjectNode root)
	{
		JsonNode n;
		n = root.get("name");
		setName(n.asText());

		n = root.get("typeId");
		setTypeId(n.asText());
		
		n = root.get("jsonConfiguration");
		setJsonConfiguration(n.asText());

		if (root.has("displayedName"))
			setDisplayedName(I18nStringJsonUtil.fromJson(root.get("displayedName")));
		else
			setDisplayedName(new I18nString(getName()));

		setDescription(I18nStringJsonUtil.fromJson(root.get("i18nDescription"), 
				root.get("description")));
	}
	
	@Override
	public String toString()
	{
		return "CredentialDefinition [name=" + name + ", typeId=" + typeId + "]";
	}

	@Override
	public CredentialDefinition clone()
	{
		CredentialDefinition ret = new CredentialDefinition(typeId, name, 
				displayedName.clone(), description.clone());
		ret.setJsonConfiguration(jsonConfiguration);
		return ret;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((jsonConfiguration == null) ? 0 : jsonConfiguration.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CredentialDefinition other = (CredentialDefinition) obj;
		if (jsonConfiguration == null)
		{
			if (other.jsonConfiguration != null)
				return false;
		} else if (!jsonConfiguration.equals(other.jsonConfiguration))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (typeId == null)
		{
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;
		return true;
	}
}
