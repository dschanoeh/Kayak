package de.vwag.kayak.canData;

import java.nio.ByteOrder;

/**
 * This is the information that is necessary to extract one single data element out
 * of a CAN frame.
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 *
 */
public class ParsingInformation {
	public enum DataType {
		INT,
		FLOAT
	};
	
	private String unit;
	private float factor;
	private float offset;
	private int startPosition;
	private int endPosition;
	private ByteOrder byteOrder;
	
	
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
	public int getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
	
	
}
