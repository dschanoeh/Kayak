package de.vwag.kayak;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {


	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
		//layout.addView("de.vwag.kayak.errorLog.LogView", IPageLayout.BOTTOM, IPageLayout.RATIO_MAX, IPageLayout.ID_EDITOR_AREA);
		layout.addView("de.vwag.kayak.busManagement.BusView",IPageLayout.LEFT,  (float) 0.8, IPageLayout.ID_PROJECT_EXPLORER);
		
	}

}
