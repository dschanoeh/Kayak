package de.vwag.kayak.canData;

public class Signal {
	private String name;
	private String unit;
	private int valueInt;
	private double valueDouble;
	
	public Signal(SignalInformation information) {
		this.name = information.getName();
		this.unit = information.getUnit();
	}

	public int getValueInt() {
		return valueInt;
	}

	public void setValueInt(int valueInt) {
		this.valueInt = valueInt;
	}

	public double getValueDouble() {
		return valueDouble;
	}

	public void setValueDouble(double valueDouble) {
		this.valueDouble = valueDouble;
	}

	public String getName() {
		return name;
	}

	public String getUnit() {
		return unit;
	}

}
