/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.h2.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.server.utils.Log;
import pl.edu.icm.unity.server.utils.UnityServerConfiguration;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.db.DBPropertiesHelper;

/**
 * Database configuration
 * @author K. Benedyczak
 */
@Component
public class DBConfiguration extends PropertiesHelper
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_CFG, DBConfiguration.class);
	public enum Dialect {h2, mysql, psql};

	@DocumentationReferencePrefix
	public static final String PREFIX = UnityServerConfiguration.BASE_PREFIX+DBPropertiesHelper.PREFIX;
	
	public static final String DBCONFIG_FILE = "mapconfigFile";
	public static final String LOCAL_DB_URL = "localDBUrl";
	public static final String ALTERNATIVE_DB_CONFIG = "alternativeUnityDb";
	
	@DocumentationReferenceMeta
	public static final Map<String, PropertyMD> META;
	static 
	{
		META = DBPropertiesHelper.getMetadata(Driver.class, "jdbc:h2:file:data/unitydb.bin", 
				Dialect.h2, "");
		META.put(DBCONFIG_FILE, new PropertyMD().setPath().setHidden().
				setDescription("Path of the low level database file with mappings configuration."));
		META.put(LOCAL_DB_URL, new PropertyMD("jdbc:h2:file:data/unity-localdb.bin").
				setDescription("Location of the local H2 database can be " +
				"controlled with this connection URL."));
	}
	
	@Autowired
	public DBConfiguration(UnityServerConfiguration main) throws ConfigurationException
	{
		super(PREFIX, main.getProperties(), META, log);
		String alternativeDB;
		alternativeDB = System.getProperty(ALTERNATIVE_DB_CONFIG);
		if(alternativeDB != null)
		{
			loadAlternativeDbConfig(alternativeDB);	
		}
		
	}
	
	private void loadAlternativeDbConfig(String db)
	{
		String path = "/dbConfigs/" + db + ".conf";
		log.debug("Loading alternative DB config from " + path);
		Properties p = new Properties();
		try
		{
			p.load(getClass().getResourceAsStream(path));
			setProperties(p);
		} catch (Exception e)
		{
			log.error("Cannot load alternative DB config from " + path, e);
		}	
	}
	
	public Properties getProperties()
	{
		return properties;
	}
}
