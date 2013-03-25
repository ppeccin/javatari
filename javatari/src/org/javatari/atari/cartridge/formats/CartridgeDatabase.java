// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.CartridgeInfo;


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

	public static CartridgeInfo getInfo(Cartridge cartridge) {
		CartridgeInfo info = new CartridgeInfo();
		String contentName = cartridge.contentName().toUpperCase();
		if (contentName.matches(".*JOYSTICK.*")) return info;
		for (String romName : paddlesRomNames) {
			if (contentName.matches(romName)) {
				info.usePaddles = true;
				break;
			}
		}
		return info;
	}
	
	static int priorityBoosted(int priority, CartridgeFormat format, String contentName) {
		if (formatMatchesByHint(format, contentName)) 
			return priority - FORMAT_HINT_BOOST;
		if (formatMatchesByName(format, contentName)) 
			return priority - FORMAT_NAME_BOOST;
		return priority;
	}

	private static boolean formatMatchesByHint(CartridgeFormat format, String contentName) {
		return contentName.matches(HINTS_PREFIX_REGEX + format.id + HINTS_SUFFIX_REGEX);
	}

	private static boolean formatMatchesByName(CartridgeFormat format, String contentName) {
		for (FormatRomNameMatcher matcher : formatRomNames)
			if (matcher.format == format && matcher.matches(contentName))
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
			CartridgePitfall2EnhancedDPCAudio.FORMAT
		});

	
	private static FormatRomNameMatcher[] formatRomNames = new FormatRomNameMatcher[] {
		new FormatRomNameMatcher(Cartridge8K_E0.FORMAT, new String[] {
				".*MONTEZUMA.*",			".*MONTZREV.*",
				".*GYRUS.*",
				".*TOOTH.*PROTECTORS.*",	".*TOOTHPRO.*",
				".*DEATH.*STAR.*BATTLE.*",	".*DETHSTAR.*",
				".*JAMES.*BOND.*",			".*JAMEBOND.*",
				".*SUPER.*COBRA.*",			".*SPRCOBRA.*",
				".*TUTANKHAM.*",			".*TUTANK.*",
				".*POPEYE.*",
				".*SW.*ARCADE.*GAME.*",		".*SWARCADE.*",
				".*Q.*BERT.*QUBES.*",		".*QBRTQUBE.*",
				".*FROGGER.*2.*",
				".*DO.*CASTLE.*"
		}),
		new FormatRomNameMatcher(Cartridge8K_FE.FORMAT, new String[] {
				".*ROBOT.*TANK.*",			".*ROBOTANK.*",
				".*DECATHLON.*"	, 			".*DECATHLN.*"		// There is also a 16K F6 version
		}),
		new FormatRomNameMatcher(Cartridge16K_E7.FORMAT, new String[] {
				".*BUMP.*JUMP.*",			".*BNJ.*",
				".*BURGER.*TIME.*",			".*BURGTIME.*",
				".*POWER.*HE.?MAN.*",		".*HE_MAN.*"
		}),
		new FormatRomNameMatcher(Cartridge8K_512K_3F.FORMAT, new String[] {
				".*POLARIS.*",
				".*RIVER.*PATROL.*",		".*RIVERP.*",
				".*SPRINGER.*",
				".*MINER.*2049.*",			".*MNR2049R.*",
				".*MINER.*2049.*VOLUME.*",	".*MINRVOL2.*",
				".*ESPIAL.*",
				".*ANDREW.*DAVIE.*"								// Various 32K Image demos
		}),
		new FormatRomNameMatcher(Cartridge8K_512K_3E.FORMAT, new String[] {
				".*BOULDER.*DASH.*", 		".*BLDRDASH.*"
		}),
		new FormatRomNameMatcher(CartridgePitfall2EnhancedDPCAudio.FORMAT, new String[] {
				".*PITFALL.*(2|II).*"
		})
	};


	private static final String[] paddlesRomNames = new String[] {
		".*PADDLES.*",										// Generic hint
		".*BREAKOUT.*",
		".*SUPER.*BREAKOUT.*",			".*SUPERB.*",
		".*WARLORDS.*",
		".*STEEPLE.*CHASE.*",			".*STEPLCHS.*",
		".*VIDEO.*OLYMPICS.*",			".*VID(|_)OLYM(|P).*",
		".*CIRCUS.*ATARI.*", 			".*CIRCATRI.*",
		".*KABOOM.*",               	
		".*BUGS((?!BUNNY).)*",								// Bugs, but not Bugs Bunny!
		".*BACHELOR.*PARTY.*", 			".*BACHELOR.*",
		".*BACHELORETTE.*PARTY.*", 		".*BACHLRTT.*",
		".*BEAT.*EM.*EAT.*EM.*", 		".*BEATEM.*",
		".*PHILLY.*FLASHER.*",			".*PHILLY.*",
		".*JEDI.*ARENA.*",				".*JEDIAREN.*",
		".*EGGOMANIA.*",				".*EGGOMANA.*",
		".*PICNIC.*",               	
		".*PIECE.*O.*CAKE.*",			".*PIECECKE.*",
		".*BACKGAMMON.*", 				".*BACKGAM.*",
		".*BLACKJACK.*",				".*BLACK(|_)J.*",
		".*CANYON.*BOMBER.*", 			".*CANYONB.*",
		".*CASINO.*",               	
		".*DEMONS.*DIAMONDS.*",			".*DEMONDIM.*",
		".*DUKES.*HAZZARD.*2.*",    	".*STUNT.?2.*",
		".*ENCOUNTER.*L.?5.*", 			".*ENCONTL5.*",
		".*FIREBALL.*",
		".*GI.*JOE.*COBRA.*STRIKE.*", 	".*GIJOE.*",
		".*GUARDIAN.*",
		".*MARBLE.*CRAZE.*",			".*MARBCRAZ.*",
		".*MEDIEVAL.*MAYHEM.*",
		".*MONDO.*PONG.*",
		".*NIGHT.*DRIVER.*",			".*NIGHTDRV.*",
		".*PARTY.*MIX.*",
		".*POKER.*PLUS.*",
		".*PONG.*SPORTS.*",
		".*SCSICIDE.*",
		".*SECRET.*AGENT.*",
		".*SOLAR.*STORM.*", 			".*SOLRSTRM.*",
		".*SPEEDWAY.*",
		".*STREET.*RACER.*", 			".*STRTRACE.*",
		".*STUNT.*CYCLE.*", 			".*STUNT.?1.*",
		".*TAC.?SCAN.*",
		".*MUSIC.*MACHINE.*", 			".*MUSCMACH.*",
		".*VONG.*",
		".*WARPLOCK.*"
	};

	
	private static final String HINTS_PREFIX_REGEX = "(|.*?(\\W|_|%20))";
	private static final String HINTS_SUFFIX_REGEX = "(|(\\W|_|%20).*)";
	
	private static final int FORMAT_NAME_BOOST = 50;
	private static final int FORMAT_HINT_BOOST = 100;

}


class FormatRomNameMatcher {
	public FormatRomNameMatcher(CartridgeFormat format, String[] namePatterns) {
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
