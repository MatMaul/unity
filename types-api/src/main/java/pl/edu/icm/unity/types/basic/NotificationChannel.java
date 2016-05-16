/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.types.basic;

import pl.edu.icm.unity.types.DescribedObjectImpl;

/**
 * Notification channel.
 * @author K. Benedyczak
 */
public class NotificationChannel extends DescribedObjectImpl
{
	private String configuration;
	private String facilityId;
	
	public NotificationChannel(String name, String description, String configuration, String facilityId)
	{
		super(name, description);
		this.configuration = configuration;
		this.facilityId = facilityId;
	}

	public NotificationChannel()
	{
	}

	public String getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}

	public String getFacilityId()
	{
		return facilityId;
	}

	public void setFacilityId(String facilityId)
	{
		this.facilityId = facilityId;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
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
		NotificationChannel other = (NotificationChannel) obj;
		if (configuration == null)
		{
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (facilityId == null)
		{
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		return true;
	}
}
