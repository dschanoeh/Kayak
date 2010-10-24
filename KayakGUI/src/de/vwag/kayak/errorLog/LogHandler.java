package de.vwag.kayak.errorLog;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class LogHandler extends Handler implements IStructuredContentProvider {
	LogView view;
	Logger kayakLogger = Logger.getLogger("de.vwag.kayak");
	
	public LogHandler(LogView view) {
		this.view = view;
		kayakLogger.addHandler(this);
	}

	@Override
	public void publish(LogRecord record) {
		view.addLogRecord(record);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}

}
