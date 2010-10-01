package de.vwag.kayak.canData;

public class Signal {
	private String name;
	private String unit;
	private long valueLong;
	private double valueDouble;
	
	public Signal(SignalInformation information) {
		this.name = information.getName();
		this.unit = information.getUnit();
	}

	public long getValueLong() {
		return valueLong;
	}

	public void setValueLong(long valueLong) {
		this.valueLong = valueLong;
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
