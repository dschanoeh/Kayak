package de.vwag.kayak.busManagement;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.vwag.kayak.can.*;

public class BusManagement  {
	private static ArrayList<Bus> busses;
	
	public static void addBus(Bus b) {
		busses.add(b);
	}
	
	public static void initialize() {
		busses = new ArrayList<Bus>();
	}
	
	public static Bus[] getBusses() {
		return busses.toArray(new Bus[1]);
	}

	

}
