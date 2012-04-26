// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the 4K unbanked format
 */
public final class Cartridge4K extends Cartridge {

	public Cartridge4K(byte[] content) {
		super();
		if (content.length != SIZE && content.length != 2048 )		// Also accepts 2K ROMs, but duplicates the content to form 4K
			throw new IllegalStateException("Invalid size for " + this.getClass().getName() + ": " + content.length);
		if (content.length == 2048) {
			byte[] newContent = new byte[4096];
			System.arraycopy(content, 0, newContent, 0, 2048);
			System.arraycopy(content, 0, newContent, 2048, 2048);
			setContent(newContent);
			return;
		}
		setContent(content);
	}

	public static final int SIZE = 4096;
	public static final int HALF_SIZE = 2048;

	private static final long serialVersionUID = 1L;

}

