package org.javatari.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;

public abstract class KeyFilteredRepeatsAdapter implements KeyListener {
	
	private KeyEvent pendingRelease = null;
	
	private final Runnable trigger = new Runnable() {
		@Override
		public void run() {
			if (pendingRelease != null) SwingHelper.edtInvokeLater(sender);
		}
	};
	
	private final Runnable sender = new Runnable() {
		@Override
		public void run() {
			if (pendingRelease != null) {
				filteredKeyReleased(pendingRelease);
				pendingRelease = null;
			}
		}
	};

	public abstract void filteredKeyPressed(KeyEvent e);

	public abstract void filteredKeyReleased(KeyEvent e);

	@Override
	public final void keyTyped(KeyEvent e) {
		// Nothing
	}

	@Override
	public final void keyPressed(KeyEvent e) {
		if (pendingRelease != null && pendingRelease.getKeyCode() == e.getKeyCode() && pendingRelease.getModifiersEx() == e.getModifiersEx())
			pendingRelease = null;
		filteredKeyPressed(e);
	}

	@Override
	public final void keyReleased(KeyEvent e) {
		if (pendingRelease != null) filteredKeyReleased(pendingRelease);
		pendingRelease = e;
		SwingUtilities.invokeLater(trigger);
	}
	
}
