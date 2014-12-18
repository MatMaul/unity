/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile.dryrun;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.sandbox.SandboxAuthnEvent;
import pl.edu.icm.unity.server.api.TranslationProfileManagement;
import pl.edu.icm.unity.server.authn.remote.RemotelyAuthenticatedContext;
import pl.edu.icm.unity.server.authn.remote.SandboxAuthnContext;
import pl.edu.icm.unity.server.registries.TranslationActionsRegistry;
import pl.edu.icm.unity.server.translation.TranslationProfile;
import pl.edu.icm.unity.server.utils.Log;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.webadmin.tprofile.MappingResultComponent;
import pl.edu.icm.unity.webadmin.tprofile.TranslationProfileViewer;
import pl.edu.icm.unity.webui.common.HtmlLabel;
import pl.edu.icm.unity.webui.common.HtmlTag;
import pl.edu.icm.unity.webui.common.Images;
import pl.edu.icm.unity.webui.common.Styles;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * UI Component used by {@link DryRunStep}.
 * 
 * @author Roman Krysinski
 */
public class DryRunStepComponent extends CustomComponent 
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_WEB, DryRunStepComponent.class);
	
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout resultWrapper;
	@AutoGenerated
	private Label capturedLogs;
	@AutoGenerated
	private HtmlLabel logsLabel;
	@AutoGenerated
	private Label hr_2;
	@AutoGenerated
	private VerticalLayout mappingResultWrap;
	@AutoGenerated
	private Label hr_1;
	@AutoGenerated
	private VerticalLayout remoteIdpWrap;
	@AutoGenerated
	private Label hr_3;
	@AutoGenerated
	private Label authnResultLabel;
	@AutoGenerated
	private VerticalLayout progressWrapper;
	private TranslationActionsRegistry taRegistry;
	private TranslationProfileManagement tpMan;
	private UnityMessageSource msg;
	private MappingResultComponent mappingResult;
	private RemotelyAuthenticatedInputComponent remoteIdpInput;
	private TranslationProfileViewer profileViewer;
	
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param msg 
	 * @param sandboxURL 
	 */
	public DryRunStepComponent(UnityMessageSource msg, String sandboxURL, TranslationActionsRegistry registry,
			TranslationProfileManagement tpMan) 
	{
		this.msg = msg;
		this.taRegistry = registry;
		this.tpMan = tpMan;
		
		setCompositionRoot(buildMainLayout());
		
		capturedLogs.setContentMode(ContentMode.PREFORMATTED);
		capturedLogs.setValue("");
		
		logsLabel.resetValue();
		
		mappingResult = new MappingResultComponent(msg);
		mappingResultWrap.addComponent(mappingResult);
		
		remoteIdpInput = new RemotelyAuthenticatedInputComponent(msg);
		remoteIdpWrap.addComponent(remoteIdpInput);
		
		progressWrapper.addComponent(new Image("", Images.loader.getResource()));
		indicateProgress();
	}

	public void handle(SandboxAuthnEvent event) 
	{
		SandboxAuthnContext ctx = event.getCtx();
		if (ctx.getAuthnException() == null)
		{
			authnResultLabel.setValue(msg.getMessage("DryRun.DryRunStepComponent.authnResultLabel.success"));
			authnResultLabel.setStyleName(Styles.success.toString());
		} else
		{
			authnResultLabel.setValue(msg.getMessage("DryRun.DryRunStepComponent.authnResultLabel.error"));
			authnResultLabel.setStyleName(Styles.error.toString());
		}
		logsLabel.setHtmlValue("DryRun.DryRunStepComponent.logsLabel");
		RemotelyAuthenticatedContext remoteAuthnContext = ctx.getAuthnContext();
		if (remoteAuthnContext != null)
		{
			remoteIdpInput.displayAuthnInput(remoteAuthnContext.getAuthnInput());
			mappingResult.displayMappingResult(remoteAuthnContext.getMappingResult(), 
				remoteAuthnContext.getInputTranslationProfile());
			showProfile(remoteAuthnContext.getInputTranslationProfile());
		} else
		{
			profileViewer.setVisible(false);
		}
		Exception e = ctx.getAuthnException();
		StringBuilder logs = new StringBuilder(ctx.getLogs());
		if (e != null)
		{
			CharArrayWriter writer = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(writer));
			logs.append("\n\n").append(writer.toString());
		}
		capturedLogs.setValue(logs.toString());
				
		hideProgressShowResult();
	}

	public void indicateProgress()
	{
		resultWrapper.setVisible(false);
		progressWrapper.setVisible(true);
	}
	
	private void hideProgressShowResult()
	{
		progressWrapper.setVisible(false);
		resultWrapper.setVisible(true);
	}
	
	private void showProfile(String profile)
	{
		try
		{
			TranslationProfile tp = tpMan.listInputProfiles().get(profile);
			profileViewer.setInput(tp);
			profileViewer.setVisible(true);
		} catch (EngineException e)
		{
			log.error(e);
		}
	}
	
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeUndefined();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		// progressWrapper
		progressWrapper = new VerticalLayout();
		progressWrapper.setSizeUndefined();
		progressWrapper.setMargin(false);
		mainLayout.addComponent(progressWrapper);
		
		// resultWrapper
		resultWrapper = buildResultWrapper();
		mainLayout.addComponent(resultWrapper);
		
		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildResultWrapper() {
		// common part: create layout
		resultWrapper = new VerticalLayout();
		resultWrapper.setImmediate(false);
		resultWrapper.setWidth("100.0%");
		resultWrapper.setHeightUndefined();
		resultWrapper.setMargin(false);
		resultWrapper.setSpacing(true);
		
		// authnResultLabel
		authnResultLabel = new Label();
		authnResultLabel.setImmediate(false);
		authnResultLabel.setWidth("-1px");
		authnResultLabel.setHeightUndefined();
		authnResultLabel.setValue("Label");
		resultWrapper.addComponent(authnResultLabel);
		
		// hr_3
		hr_3 = HtmlTag.hr();
		hr_3.setImmediate(false);
		hr_3.setWidth("100.0%");
		hr_3.setHeight("-1px");
		resultWrapper.addComponent(hr_3);
		
		// remoteIdpWrap
		remoteIdpWrap = new VerticalLayout();
		remoteIdpWrap.setImmediate(false);
		remoteIdpWrap.setSizeUndefined();
		remoteIdpWrap.setMargin(false);

		// mappingResultWrap
		mappingResultWrap = new VerticalLayout();
		mappingResultWrap.setImmediate(false);
		mappingResultWrap.setWidth("-1px");
		mappingResultWrap.setHeight("-1px");
		mappingResultWrap.setMargin(false);
		
		HorizontalLayout hr = new HorizontalLayout(remoteIdpWrap, mappingResultWrap);
		hr.setSpacing(true);
		
		resultWrapper.addComponent(hr);
		
		// hr_1
		hr_1 = HtmlTag.hr();
		resultWrapper.addComponent(hr_1);
		
		profileViewer = new TranslationProfileViewer(msg, taRegistry);
		
		resultWrapper.addComponent(profileViewer);
		
		// hr_2
		hr_2 = HtmlTag.hr();
		resultWrapper.addComponent(hr_2);
		
		// logsLabel
		logsLabel = new HtmlLabel(msg);
		logsLabel.setImmediate(false);
		logsLabel.setWidth("-1px");
		logsLabel.setHeight("-1px");
		resultWrapper.addComponent(logsLabel);
		
		// capturedLogs
		capturedLogs = new Label();
		capturedLogs.setImmediate(false);
		capturedLogs.setWidth("-1px");
		capturedLogs.setHeight("-1px");
		capturedLogs.setValue("Label");
		resultWrapper.addComponent(capturedLogs);
		
		return resultWrapper;
	}

}
