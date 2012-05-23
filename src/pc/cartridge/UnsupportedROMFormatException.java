package pc.cartridge;

public class UnsupportedROMFormatException extends Exception {
	public UnsupportedROMFormatException(String message) {
		super(message);
	}
	private static final long serialVersionUID = 1L;
}