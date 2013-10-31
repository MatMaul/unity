/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.reg.reqman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.server.api.RegistrationsManagement;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.types.registration.RegistrationRequestState;
import pl.edu.icm.unity.webadmin.reg.reqman.RequestsTable.RequestSelectionListener;
import pl.edu.icm.unity.webui.WebSession;
import pl.edu.icm.unity.webui.bus.EventListener;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Component responsible for management of the submitted registration requests.
 * 
 * Shows their table, and the selected request in the right side, where it can be edited, 
 * accepted, rejected or dropped.
 *  
 * @author K. Benedyczak
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestsComponent extends CustomComponent
{
	private RegistrationsManagement registrationsManagement;
	private UnityMessageSource msg;
	
	private RequestsTable requestsTable;
	
	@Autowired
	public RequestsComponent(RegistrationsManagement registrationsManagement,
			UnityMessageSource msg)
	{
		this.registrationsManagement = registrationsManagement;
		this.msg = msg;
		initUI();
		WebSession.getCurrent().getEventBus().addListener(new EventListener<RegistrationRequestChangedEvent>()
		{
			@Override
			public void handleEvent(RegistrationRequestChangedEvent event)
			{
				requestsTable.refresh();
			}
		}, RegistrationRequestChangedEvent.class);
	}


	private void initUI()
	{
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		hl.setSizeFull();
		requestsTable = new RequestsTable(registrationsManagement, msg);
		
		final RequestProcessingPanel requestPanel = new RequestProcessingPanel(msg, registrationsManagement);
		requestsTable.addValueChangeListener(new RequestSelectionListener()
		{
			@Override
			public void requestChanged(RegistrationRequestState request)
			{
				requestPanel.setRequest(request);
			}
		});
		
		hl.addComponents(requestsTable, requestPanel);
		hl.setExpandRatio(requestsTable, 0.5f);
		hl.setExpandRatio(requestPanel, 0.5f);
		setCompositionRoot(hl);
		setCaption(msg.getMessage("RequestsComponent.caption"));
	}
}