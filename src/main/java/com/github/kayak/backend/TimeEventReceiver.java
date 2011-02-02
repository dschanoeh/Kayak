package com.github.kayak.backend;

public interface TimeEventReceiver {
	public void paused();
	public void played();
	public void stopped();
}
