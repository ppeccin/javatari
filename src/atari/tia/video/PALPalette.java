// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.video;

public final class PALPalette {

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
		0x282828,		// 02
		0x505050,		// 04
		0x747474,		// 06
		0x949494,		// 08
		0xb4b4b4,		// 0A
		0xd0d0d0,		// 0C
		0xf1f1f1,		// 0E
		0x000000,		// 10
		0x282828,		// 12
		0x505050,		// 14
		0x747474,		// 16
		0x949494,		// 18
		0xb4b4b4,		// 1A
		0xd0d0d0,		// 1C
		0xf1f1f1,		// 1E
		0x805800,		// 20
		0x947020,		// 22
		0xa8843c,		// 24
		0xbc9c58,		// 26
		0xccac70,		// 28
		0xdcc084,		// 2A
		0xecd09c,		// 2C
		0xfce0b0,		// 2E
		0x445c00,		// 30
		0x5c7820,		// 32
		0x74903c,		// 34
		0x8cac58,		// 36
		0xa0c070,		// 38
		0xb0d484,		// 3A
		0xc4e89c,		// 3C
		0xd4fcb0,		// 3E
		0x703400,		// 40
		0x885020,		// 42
		0xA0683C,		// 44
		0xB48458,		// 46
		0xC89870,		// 48
		0xDCAC84,		// 4A
		0xECC09C,		// 4C
		0xFCD4B0,		// 4E
		0x006414,		// 50
		0x208034,		// 52
		0x3C9850,		// 54
		0x58B06C,		// 56
		0x70C484,		// 58
		0x84D89C,		// 5A
		0x9CE8B4,		// 5C
		0xB0FCC8,		// 5E
		0x700014,		// 60
		0x882034,		// 62
		0xA03C50,		// 64
		0xB4586C,		// 66
		0xC87084,		// 68
		0xDC849C,		// 6A
		0xEC9CB4,		// 6C
		0xFCB0C8,		// 6E
		0x005C5C,		// 70
		0x207474,		// 72
		0x3C8C8C,		// 74
		0x58A4A4,		// 76
		0x70B8B8,		// 78
		0x84C8C8,		// 7A
		0x9CDCDC,		// 7C
		0xB0ECEC,		// 7E
		0x70005C,		// 80
		0x842074,		// 82
		0x943C88,		// 84
		0xA8589C,		// 86
		0xB470B0,		// 88
		0xC484C0,		// 8A
		0xD09CD0,		// 8C
		0xE0B0E0,		// 8E
		0x003C70,		// 90
		0x1C5888,		// 92
		0x3874A0,		// 94
		0x508CB4,		// 96
		0x68A4C8,		// 98
		0x7CB8DC,		// 9A
		0x90CCEC,		// 9C
		0xA4E0FC,		// 9E
		0x580070,		// A0
		0x6C2088,		// A2
		0x803CA0,		// A4
		0x9458B4,		// A6
		0xA470C8,		// A8
		0xB484DC,		// AA
		0xC49CEC,		// AC
		0xD4B0FC,		// AE
		0x002070,		// B0
		0x1C3C88,		// B2
		0x3858A0,		// B4
		0x5074B4,		// B6
		0x6888C8,		// B8
		0x7CA0DC,		// BA
		0x90B4EC,		// BC
		0xA4C8FC,		// BE
		0x3C0080,		// C0
		0x542094,		// C2
		0x6C3CA8,		// C4
		0x8058BC,		// C6
		0x9470CC,		// C8
		0xA884DC,		// CA
		0xB89CEC,		// CC
		0xC8B0FC,		// CE
		0x000088,		// D0
		0x20209C,		// D2
		0x3C3CB0,		// D4
		0x5858C0,		// D6
		0x7070D0,		// D8
		0x8484E0,		// DA
		0x9C9CEC,		// DC
		0xB0B0FC,		// DE
		0x000000,		// E0
		0x282828,		// E2
		0x505050,		// E4
		0x747474,		// E6
		0x949494,		// E8
		0xB4B4B4,		// EA
		0xD0D0D0,		// EC
		0xF1F1F1,		// EE
		0x000000,		// F0
		0x282828,		// F2
		0x505050,		// F4
		0x747474,		// F6
		0x949494,		// F8
		0xB4B4B4,		// FA
		0xD0D0D0,		// FC
		0xF1F1F1,		// FE
	};
		
}
