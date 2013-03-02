// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.CartridgeFormat;

/**
 * Implements generic bank switching using unmasked address access via bus monitoring (outside Cart area)
 */
public abstract class CartridgeBankedByBusMonitoring extends CartridgeBanked {

	protected CartridgeBankedByBusMonitoring(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format);
	}

	@Override
	public boolean needsBusMonitoring() {
		return true;
	}

	@Override
	public void monitorByteRead(int address, byte data) {
		performBankSwitchOnMonitoredAccess(address);
	}

	@Override
	public void monitorByteWritten(int address, byte data) {
		performBankSwitchOnMonitoredAccess(address);
	}
	
	@Override
	protected void performBankSwitchOnMaskedAddress() {
		// Bank switching is not done within masked address range
		// Its done directly via bus monitoring
	}

	protected abstract void performBankSwitchOnMonitoredAccess(int address);

	
	public static final long serialVersionUID = 1L;

}