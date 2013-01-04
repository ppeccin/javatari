// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.video;

public final class NTSCPalette {

	public static int[] getPalette() {
		int[] pal = new int[256];
		for (int i = 0; i < baseRGB.length; i++) {
			pal[i*2] = pal[i*2+1] = (baseRGB[i] | 0xff000000);		// Adds 100% alpha for ARGB use
		}

		pal[0] = pal[1] = 0;	// Full transparency for blacks. Needed for CRT emulation modes

		return pal;
	}
	
	public static int[] baseRGB = {
		0x000000,		// 00
		0x404040,		// 02
		0x6c6c6c,		// 04
		0x909090,		// 06
		0xb0b0b0,		// 08
		0xc8c8c8,		// 0A
		0xdcdcdc,		// 0C
		0xf4f4f4,		// 0E
		0x444400,		// 10
		0x646410,		// 12
		0x848424,		// 14
		0xa0a034,		// 16
		0xb8b840,		// 18
		0xd0d050,		// 1A
		0xe8e85c,		// 1C
		0xfcfc68,		// 1E
		0x702800,		// 20
		0x844414,		// 22
		0x985c28,		// 24
		0xac783c,		// 26
		0xbc8c4c,		// 28
		0xcca05c,		// 2A
		0xdcb468,		// 2C
		0xecc878,		// 2E
		0x841800,		// 30
		0x983418,		// 32
		0xac5030,		// 34
		0xc06848,		// 36
		0xd0805c,		// 38
		0xe09470,		// 3A
		0xeca880,		// 3C
		0xfcbc94,		// 3E
		0x880000,		// 40
		0x9c2020,		// 42
		0xb03c3c,		// 44
		0xc05858,		// 46
		0xd07070,		// 48
		0xe08888,		// 4A
		0xeca0a0,		// 4C
		0xfcb4b4,		// 4E
		0x78005c,		// 50
		0x8c2074,		// 52
		0xa03c88,		// 54
		0xb0589c,		// 56
		0xc070b0,		// 58
		0xd084c0,		// 5A
		0xdc9cd0,		// 5C
		0xecb0e0,		// 5E
		0x480078,		// 60
		0x602090,		// 62
		0x783ca4,		// 64
		0x8c58b8,		// 66
		0xa070cc,		// 68
		0xb484dc,		// 6A
		0xc49cec,		// 6C
		0xd4b0fc,		// 6E
		0x140084,		// 70
		0x302098,		// 72
		0x4c3cac,		// 74
		0x6858c0,		// 76
		0x7c70d0,		// 78
		0x9488e0,		// 7A
		0xa8a0ec,		// 7C
		0xbcb4fc,		// 7E
		0x000088,		// 80
		0x1c209c,		// 82
		0x3840b0,		// 84
		0x505cc0,		// 86
		0x6874d0,		// 88
		0x7c8ce0,		// 8A
		0x90a4ec,		// 8C
		0xa4b8fc,		// 8E
		0x00187c,		// 90
		0x1c3890,		// 92
		0x3854a8,		// 94
		0x5070bc,		// 96
		0x6888cc,		// 98
		0x7c9cdc,		// 9A
		0x90b4ec,		// 9C
		0xa4c8fc,		// 9E
		0x002c5c,		// A0
		0x1c4c78,		// A2
		0x386890,		// A4
		0x5084ac,		// A6
		0x689cc0,		// A8
		0x7cb4d4,		// AA
		0x90cce8,		// AC
		0xa4e0fc,		// AE
		0x003c2c,		// B0
		0x1c5c48,		// B2
		0x387c64,		// B4
		0x509c80,		// B6
		0x68b494,		// B8
		0x7cd0ac,		// BA
		0x90e4c0,		// BC
		0xa4fcd4,		// BE
		0x003c00,		// C0
		0x205c20,		// C2
		0x407c40,		// C4
		0x5c9c5c,		// C6
		0x74b474,		// C8
		0x8cd08c,		// CA
		0xa4e4a4,		// CC
		0xb8fcb8,		// CE
		0x143800,		// D0
		0x345c1c,		// D2
		0x507c38,		// D4
		0x6c9850,		// D6
		0x84b468,		// D8
		0x9ccc7c,		// DA
		0xb4e490,		// DC
		0xc8fca4,		// DE
		0x2c3000,		// E0
		0x4c501c,		// E2
		0x687034,		// E4
		0x848c4c,		// E6
		0x9ca864,		// E8
		0xb4c078,		// EA
		0xccd488,		// EC
		0xe0ec9c,		// EE
		0x442800,		// F0
		0x644818,		// F2
		0x846830,		// F4
		0xa08444,		// F6
		0xb89c58,		// F8
		0xd0b46c,		// FA
		0xe8cc7c,		// FC
		0xfce08c,		// FE
	};
		
}
