// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.joy;

public class Joystick {

	private Joystick(Joy.Info info) {
		this.info = info;
		this.state = info.getState();
	}
	
	public boolean update() {
		return state.update();
	}
	
	public int getButtons() {
		return state.getButtons();
	}

	public boolean getButton(int button) {
		return (state.getButtons() & (1 << button)) != 0;
	}
	
	public float getAxis(int axis) {
		float val = ((float)getAxisRaw(axis) - info.axesMinValues[axis]) / (info.axesMaxValues[axis] - info.axesMinValues[axis]) * 2 - 1;
		return (val < 0 ? -val : val) < deadZoneThreshold ? 0 : val;
	}

	public float getAxisNoThreshold(int axis) {
		return ((float)getAxisRaw(axis) - info.axesMinValues[axis]) / (info.axesMaxValues[axis] - info.axesMinValues[axis]) * 2 - 1;
	}

	public int getAxisDigital(int axis) {
		float val = ((float)getAxisRaw(axis) - info.axesMinValues[axis]) / (info.axesMaxValues[axis] - info.axesMinValues[axis]) * 2 - 1;
		return (val == 0 || (val < 0 ? -val : val) < digitalThreshold) ? 0 : (val > 0 ? 1 : -1);
	}

	public float getAxisDirection(int xAxis, int yAxis) {
		return getAxisDirection(xAxis, 1, yAxis, 1);
	}

	public float getAxisDirection(int xAxis, int xSig, int yAxis, int ySig) {
		float x = getAxis(xAxis) * xSig;
		float y = getAxis(yAxis) * ySig;
		if (x == 0 && y == 0) return CENTER;
		return (float) (1 - Math.atan2(x, y) / Math.PI) / 2;
	}

	public int getAxisDirectionCardinal(int xAxis, int yAxis) {
		return getAxisDirectionCardinal(xAxis, 1, yAxis, 1); 
	}
	
	public int getAxisDirectionCardinal(int xAxis, int xSig, int yAxis, int ySig) {
		float x = getAxisNoThreshold(xAxis);
		float y = getAxisNoThreshold(yAxis);
		if ((x < 0 ? -x : x) < digitalThreshold) x = 0; else x *= xSig;
		if ((y < 0 ? -y : y) < digitalThreshold) y = 0; else y *= ySig;
		if (x == 0 && y == 0) return CENTER;
		float dir = (float) (1 - Math.atan2(x, y) / Math.PI) / 2;
		dir += 1f/16; if (dir >= 1) dir -= 1;
		return (int) (dir * 8);
	}

	public int getAxisRaw(int axis) {
		return state.getAxis(axis);
	}

	public float getPOVDirection() {
		int pov = getPOVRaw();
		return pov == POV_CENTER_RAW ? -1 : (float)pov / POV_MAX_RAW;
	}

	public int getPOVDirectionCardinal() {
		int pov = getPOVRaw();
		return pov == POV_CENTER_RAW ? -1 : pov / POV_INC_RAW;
	}

	public int getPOVRaw() {
		return state.getPOV();
	}

	public void setDeadZoneThreshold(float thre) {
		deadZoneThreshold = thre;
	}
	
	public void setDigitalThreshold(float thre) {
		digitalThreshold = thre;
	}

	public Joy.Info getInfo() {
		return info;
	}

	public String toString() {
		return "Joystick (" + info.id + ") " + state;
	}
	
	public static Joystick create(Joy.Info info) {
		return new Joystick(info);
	}
	

	private final Joy.Info info;
	private final Joy.State state;
	private float deadZoneThreshold = 0.20f;
	private float digitalThreshold = 0.30f;


	public static int NORTH = 0;
	public static int NORTHEAST = 1;
	public static int EAST = 2;
	public static int SOUTHEAST = 3;
	public static int SOUTH = 4;
	public static int SOUTHWEST = 5;
	public static int WEST = 6;
	public static int NORTHWEST = 7;
	public static int CENTER = -1;

	private static int POV_CENTER_RAW = 65535;
	private static int POV_INC_RAW = 4500;
	private static int POV_MAX_RAW = 36000;
	
}
