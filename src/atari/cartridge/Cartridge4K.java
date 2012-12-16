// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the 4K unbanked format. Smaller ROMs will be copied several times to fill the entire 4K
 */
public final class Cartridge4K extends Cartridge {

	public Cartridge4K(byte[] content) {
		super();
		int len = content.length;
		byte[] newContent = new byte[SIZE];
		for (int pos = 0; pos < SIZE; pos += len)
			System.arraycopy(content, 0, newContent, pos, len);
		setContent(newContent);
	}


	public static boolean accepts(byte[] content, Boolean superChip, boolean sliced) {
		return content.length >= 4 && SIZE % content.length == 0 && (superChip == null || !superChip) && !sliced; 
	}

	public static final int SIZE = 4096;

	public static final long serialVersionUID = 1L;

}

