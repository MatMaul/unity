/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.stdext.attr;

import org.springframework.stereotype.Component;

import pl.edu.icm.unity.base.attributes.AttributeValueSyntax;
import pl.edu.icm.unity.base.attributes.AttributeValueSyntaxFactory;

@Component
public class IntegerAttributeSyntaxFactory implements AttributeValueSyntaxFactory<Long>
{
	@Override
	public AttributeValueSyntax<Long> createInstance()
	{
		return new IntegerAttributeSyntax();
	}

	@Override
	public String getId()
	{
		return IntegerAttributeSyntax.ID;
	}
	
}
