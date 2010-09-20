package de.vwag.kayak.canData;

import java.nio.ByteOrder;

/**
 * This is the information that is necessary to extract one single data element out
 * of a CAN frame.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class SignalInformation {
	public enum DataType {
		INT,
		FLOAT
	};
	
	private String unit;
	private float factor;
	private float offset;
	private int startPosition;
	private int size;
	private ByteOrder byteOrder;
	private String name;
	private boolean signed;
	private float minimum;
	private float maximum;
	
	
	public float getMinimum() {
		return minimum;
	}
	public void setMinimum(float minimum) {
		this.minimum = minimum;
	}
	public float getMaximum() {
		return maximum;
	}
	public void setMaximum(float maximum) {
		this.maximum = maximum;
	}
	public boolean isSigned() {
		return signed;
	}
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ByteOrder getByteOrder() {
		return byteOrder;
	}
	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public float getFactor() {
		return factor;
	}
	public void setFactor(float factor) {
		this.factor = factor;
	}
	public float getOffset() {
		return offset;
	}
	public void setOffset(float offset) {
		this.offset = offset;
	}
	public int getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	
}
