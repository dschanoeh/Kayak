package de.vwag.kayak.can;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusNameContainer {
	class BusNameTupel {
		 private Bus bus;
		 private String name;

		 public BusNameTupel(Bus bus, String name) {
			 this.bus = bus;
			 this.name = name;
		 }

		 public Bus getBus() {
			 return bus;
		 }

		 public String getName() {
		     return name;
		 }
	};
	
	private Logger logger = Logger.getLogger("de.vwag.kayak.can");
	private ArrayList<BusNameTupel> container;
	
	public BusNameContainer() {
		container = new ArrayList<BusNameTupel>();
	}
	
	public void addPair(Bus bus, String name) {
		for(BusNameTupel tupel : container) {
			if(tupel.getBus().equals(bus) || tupel.getName().equals(name)) {
				logger.log(Level.WARNING, "BusNameContainer already contains this bus. Not adding a new one for integrity.");
				return;
			}
		}
		
		container.add(new BusNameTupel(bus, name));
	}
	
	public boolean containsName(String name) {
		for(BusNameTupel tupel : container) {
			if(tupel.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsBus(Bus bus) {
		for(BusNameTupel tupel : container) {
			if(tupel.getBus().equals(bus)) {
				return true;
			}
		}
		return false;
	}
	
	public String getName(Bus bus) {
		for(BusNameTupel tupel : container) {
			if(tupel.getBus().equals(bus)) {
				return tupel.getName();
			}
		}
		return null;
	}
	
	public Bus getBus(String name) {
		for(BusNameTupel tupel : container) {
			if(tupel.getName().equals(name)) {
				return tupel.getBus();
			}
		}
		return null;
	}
}
