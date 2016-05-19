/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.objstore.tprofile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.edu.icm.unity.JsonUtil;
import pl.edu.icm.unity.store.impl.objstore.GenericObjectBean;
import pl.edu.icm.unity.store.objstore.DefaultEntityHandler;
import pl.edu.icm.unity.types.translation.TranslationProfile;

/**
 * Handler for {@link AbstractTranslationProfileInstance}.
 * 
 * @author K. Benedyczak
 */
@Component
public class TranslationProfileHandler extends DefaultEntityHandler<TranslationProfile>
{
	public static final String TRANSLATION_PROFILE_OBJECT_TYPE = "translationProfile";
	
	@Autowired
	public TranslationProfileHandler(ObjectMapper jsonMapper)
	{
		super(jsonMapper, TRANSLATION_PROFILE_OBJECT_TYPE, TranslationProfile.class);
	}

	@Override
	public GenericObjectBean toBlob(TranslationProfile value)
	{
		byte[] contents = JsonUtil.serialize2Bytes(value.toJsonObject());
		return new GenericObjectBean(value.getName(), contents, supportedType, 
				value.getProfileType().toString());
	}

	@Override
	public TranslationProfile fromBlob(GenericObjectBean blob)
	{
		return new TranslationProfile(JsonUtil.parse(blob.getContents()));
	}
}
