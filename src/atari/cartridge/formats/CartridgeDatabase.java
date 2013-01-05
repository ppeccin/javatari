// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

public class CartridgeDatabase {

	public static ArrayList<CartridgeFormatOption> getFormatOptions(Cartridge cartridge) {
		return getFormatOptions(cartridge.content(), cartridge.contentName());
	}

	public static ArrayList<CartridgeFormatOption> getFormatOptionsUnhinted(Cartridge cartridge) {
		return getFormatOptions(cartridge.content(), "");
	}

	public static ArrayList<CartridgeFormatOption> getFormatOptions(byte[] content, String contentName) {
		ArrayList<CartridgeFormatOption> options = new ArrayList<CartridgeFormatOption>();
		for (CartridgeFormat format : allFormats) {
			CartridgeFormatOption option = format.getOption(content, contentName.toUpperCase());
			if (option != null) options.add(option);
		}
	
		// Verify if more than one option with same priority exists
		int prev = -1;
		for (CartridgeFormatOption op : options) {
			if (op.priority == prev) throw new IllegalStateException("More than one CartridgeFormatOption with priority: " + prev);
			prev = op.priority;
		}
		
		// Sort according to priority
		Collections.sort(options);
		return options;
	}

	static int priorityBoosted(int priority, CartridgeFormat format, String contentName) {
		if (formatHinted(format, contentName)) 
			return priority - FORMAT_HINT_BOOST;
		if (nameHinted(format, contentName)) 
			return priority - NAME_HINT_BOOST;
		return priority;
	}

	private static boolean formatHinted(CartridgeFormat format, String contentName) {
		return contentName.matches(HINTS_PREFIX_REGEX + format.id + HINTS_SUFFIX_REGEX);
	}

	private static boolean nameHinted(CartridgeFormat format, String contentName) {
		for (NameHint hint : nameHints)
			if (hint.format == format && hint.matches(contentName))
				return true;
		return false;
	}

	
	private static List<CartridgeFormat> allFormats = Arrays.asList(new CartridgeFormat[] {
			Cartridge4K.FORMAT,
			Cartridge2K_CV.FORMAT,
			Cartridge8K_F8.FORMAT,
			Cartridge12K_FA.FORMAT,
			Cartridge16K_F6.FORMAT,
			Cartridge24K_28K_FA2.FORMAT,
			Cartridge32K_F4.FORMAT,
			Cartridge64K_F0.FORMAT,
			Cartridge64K_X07.FORMAT,
			Cartridge8K_E0.FORMAT,
			Cartridge8K_FE.FORMAT,
			Cartridge16K_E7.FORMAT,
			Cartridge8K_512K_3E.FORMAT,
			Cartridge8K_512K_3F.FORMAT,
			Cartridge8K_64K_EF.FORMAT,
			Cartridge8K_512K_SB.FORMAT,
			Cartridge8K_UA.FORMAT,
			Cartridge8K_0840.FORMAT,
			Cartridge10K_DPC.FORMAT,
		});

	
	private static NameHint[] nameHints = new NameHint[] {
		new NameHint(Cartridge8K_E0.FORMAT, new String[] {
				".*MONTEZUMA.*",			"MONTZREV.*",
				".*GYRUS.*",
				".*TOOTH.*PROTECTORS.*",	"TOOTHPRO.*",
				".*DEATH.*STAR.*BATTLE.*",	"DETHSTAR.*",
				".*JAMES.*BOND.*",			"JAMEBOND.*",
				".*SUPER.*COBRA.*",			"SPRCOBRA.*",
				".*TUTANKHAM.*",			"TUTANK.*",
				".*POPEYE.*",
				".*SW.*ARCADE.*GAME.*",		"SWARCADE.*",
				".*Q.*BERT.*QUBES.*",		"QBRTQUBE.*",
				".*FROGGER.*2.*",			"FROGGER2.*",
				".*DO.*CASTLE.*",			"DOCASTLE.*"
		}),
		new NameHint(Cartridge8K_FE.FORMAT, new String[] {
				".*ROBOT?.*TANK.*", 		"ROBOTANK.*",
				".*DECATHLON.*"	, 			"DECATHLN.*"		// There is also a 16K F6 version
		}),
		new NameHint(Cartridge16K_E7.FORMAT, new String[] {
				".*BUMP.*JUMP.*",			"BNJ.*",
				".*BURGER.?TIME.*",			"BURGTIME.*",
				".*POWER.*HE.?MAN.*",		"HE_MAN.*"
		}),
		new NameHint(Cartridge8K_512K_3F.FORMAT, new String[] {
				".*POLARIS.*",
				".*RIVER.*PATROL.*",		"RIVERP.*",
				".*SPRINGER.*",
				".*MINER.*2049.*",			"MNR2049R.*",
				".*MINER.*2049.*VOLUME.*",	"MINRVOL2.*",
				".*ESPIAL.*",
				".*ANDREW.*DAVIE.*"								// Various 32K Image demos
		}),
		new NameHint(Cartridge8K_512K_3E.FORMAT, new String[] {
				".*BOULDER.*DASH.*", 		"BLDRDASH.*"
		})
	};


	private static final String HINTS_PREFIX_REGEX = "(|.*?(\\W|_|%20))";
	private static final String HINTS_SUFFIX_REGEX = "(|(\\W|_|%20).*)";
	
	private static final int NAME_HINT_BOOST = 50;
	private static final int FORMAT_HINT_BOOST = 100;

}


class NameHint {
	public NameHint(CartridgeFormat format, String[] namePatterns) {
		super();
		this.format = format;
		this.namePatterns = Arrays.asList(namePatterns);
	}
	public boolean matches(String contentName) {
		for (String pattern : namePatterns)
			if (contentName.matches(pattern)) return true;
		return false;
	}
	final CartridgeFormat format;
	final List<String> namePatterns;
}
