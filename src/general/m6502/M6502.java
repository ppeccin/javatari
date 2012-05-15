// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502;

import static general.m6502.OperandType.ABS;
import static general.m6502.OperandType.ABS_X;
import static general.m6502.OperandType.ABS_Y;
import static general.m6502.OperandType.ACC;
import static general.m6502.OperandType.IMM;
import static general.m6502.OperandType.IND;
import static general.m6502.OperandType.IND_X;
import static general.m6502.OperandType.IND_Y;
import static general.m6502.OperandType.Z_PAGE;
import static general.m6502.OperandType.Z_PAGE_X;
import static general.m6502.OperandType.Z_PAGE_Y;
import static general.m6502.Register.rA;
import static general.m6502.Register.rSP;
import static general.m6502.Register.rX;
import static general.m6502.Register.rY;
import static general.m6502.StatusBit.bCARRY;
import static general.m6502.StatusBit.bDECIMAL_MODE;
import static general.m6502.StatusBit.bINTERRUPT_DISABLE;
import static general.m6502.StatusBit.bNEGATIVE;
import static general.m6502.StatusBit.bOVERFLOW;
import static general.m6502.StatusBit.bZERO;
import general.board.BUS16Bits;
import general.board.Clock;
import general.board.ClockDriven;
import general.m6502.instructions.ADC;
import general.m6502.instructions.AND;
import general.m6502.instructions.ASL;
import general.m6502.instructions.BIT;
import general.m6502.instructions.BRK;
import general.m6502.instructions.Bxx;
import general.m6502.instructions.CLx;
import general.m6502.instructions.CPx;
import general.m6502.instructions.DEC;
import general.m6502.instructions.DEx;
import general.m6502.instructions.EOR;
import general.m6502.instructions.INC;
import general.m6502.instructions.INx;
import general.m6502.instructions.JMP;
import general.m6502.instructions.JSR;
import general.m6502.instructions.LDx;
import general.m6502.instructions.LSR;
import general.m6502.instructions.NOP;
import general.m6502.instructions.ORA;
import general.m6502.instructions.PHA;
import general.m6502.instructions.PHP;
import general.m6502.instructions.PLA;
import general.m6502.instructions.PLP;
import general.m6502.instructions.ROL;
import general.m6502.instructions.ROR;
import general.m6502.instructions.RTI;
import general.m6502.instructions.RTS;
import general.m6502.instructions.SBC;
import general.m6502.instructions.SEx;
import general.m6502.instructions.STx;
import general.m6502.instructions.Txx;
import general.m6502.instructions.uANC;
import general.m6502.instructions.uANE;
import general.m6502.instructions.uARR;
import general.m6502.instructions.uASR;
import general.m6502.instructions.uDCP;
import general.m6502.instructions.uISB;
import general.m6502.instructions.uKIL;
import general.m6502.instructions.uLAS;
import general.m6502.instructions.uLAX;
import general.m6502.instructions.uLXA;
import general.m6502.instructions.uNOP;
import general.m6502.instructions.uRLA;
import general.m6502.instructions.uRRA;
import general.m6502.instructions.uSAX;
import general.m6502.instructions.uSBX;
import general.m6502.instructions.uSHA;
import general.m6502.instructions.uSHS;
import general.m6502.instructions.uSHX;
import general.m6502.instructions.uSHY;
import general.m6502.instructions.uSLO;
import general.m6502.instructions.uSRE;

import java.io.Serializable;

import utils.Debugger;

public final class M6502 implements ClockDriven {

	public M6502() {
	}

	public M6502(BUS16Bits memory) {
		connectBus(memory);
	}

	public void connectBus(BUS16Bits bus) {
		this.memory = bus;
	}
	
	public void reset() {
		resetAt(memoryReadWord(POWER_ON_RESET_ADDRESS));
	}
	
	public void resetAt(char initialPC) {
		PC = initialPC;
		INTERRUPT_DISABLE = true;
		cyclesToExecute = 0;
		instructionToExecute = null;
	}
	
	/** This implementation executes all fetch operations on the FIRST cycle, 
	  * then read and write operations on the LAST cycle of each instruction, and skips the cycles in between */
	@Override
	public void clockPulse() {
		// If this is the last execution cycle of the instruction, execute it and IGNORE the !RDY signal 
		if (cyclesToExecute == 1)
			instructionToExecute.execute();
		else
			if (!RDY) return;						// CPU is halted
		if (--cyclesToExecute >= 0) return;			// CPU is still "executing" remaining instruction cycles
		if (trace) showTrace();
		instructionToExecute = instructions[toUnsignedByte(memory.readByte(PC++))];		// Reads the instruction to be executed
		cyclesToExecute = instructionToExecute.fetch() - 1;				// One cycle was just executed already!
	}

	public void powerOn() {	// Initializes the CPU as if it were just powered on
		PC = 0;
		SP = STACK_INITIAL_SP;
		A = X = Y = 0;
		PS((byte)0);
		INTERRUPT_DISABLE = true;
		RDY = true;
		reset();
	}
	
	public void powerOff() {
	}

	public char fetchImmediateAddress() {
		return PC++;
	}

	public char fetchRelativeAddress() {
		char res = (char) (memory.readByte(PC++) + PC);  // PC should be get AFTER the increment and be added to the offset that was read
		pageCrossed = (res & 0xff00) != (PC & 0xff00);
		return res;		
	}

	public char fetchZeroPageAddress() {
		return (char) toUnsignedByte((memory.readByte(PC++)));
	}

	public char fetchZeroPageXAddress() {
		return (char) toUnsignedByte(memory.readByte(PC++) + X);		// Sum should wrap the byte and always be in range 0 - ff
	}

	public int fetchZeroPageYAddress() {
		return (char) toUnsignedByte(memory.readByte(PC++) + Y);		// Sum should wrap the byte and always be in range 0 - ff
	}
	
	public char fetchAbsoluteAddress() {
		return memoryReadWord((PC+=2) - 2);		// PC should be get BEFORE the double increment 
	}

	public char fetchAbsoluteXAddress() {
		final char addr = fetchAbsoluteAddress();
		final char res = (char) (addr + toUunsignedByte(X));
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		return res;
	}

	public char fetchAbsoluteYAddress() {
		final char addr = fetchAbsoluteAddress();
		final char res = (char) (addr + toUunsignedByte(Y));
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		return res;
	}

	public int fetchIndirectAddress() {
		return memoryReadWordWrappingPage(fetchAbsoluteAddress());		// Should wrap page reading effective address
	}

	public int fetchIndirectXAddress() {
		return memoryReadWordWrappingPage(fetchZeroPageXAddress());		// Should wrap page (the zero page) reading effective address
	}

	public char fetchIndirectYAddress() {
		final char addr = memoryReadWordWrappingPage(fetchZeroPageAddress());		// Should wrap page (the zero page) reading effective address
		final char res = (char) (addr + toUunsignedByte(Y));
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		return res; 
	}

	public char memoryReadWord(int address) {
		return (char) (toUnsignedByte(memory.readByte(address)) + (toUnsignedByte(memory.readByte(address + 1)) << 8));		// Address + 1 may wrap, LSB first
	}

	public char memoryReadWordWrappingPage(int address) {		// Accounts for the page-cross problem  (should wrap page)
		if ((address & 0xff) == 0xff)		
			return (char) (toUnsignedByte(memory.readByte(address)) + (toUnsignedByte(memory.readByte(address & 0xff00)) << 8));	// Gets hi byte from the page-wrap &xx00 (addr + 1 wraps to begin of page)
		else
			return memoryReadWord(address);
	}

	public void pushByte(byte b) {
		// if ((SP & 0xff) < 0x80) showDebug(String.format("PUSH: %02x", b));		// Pushing into TIA area!!!
		memory.writeByte(STACK_PAGE + toUunsignedByte(SP--), b);
	}

	public byte pullByte() {
		return memory.readByte(STACK_PAGE + toUunsignedByte(++SP));
	}
	
	public void pushWord(char w) {
		pushByte((byte) ((w >>> 8) & 0xff));
		pushByte((byte) (w & 0xff));
	}

	public char pullWord() {
		return (char) ((pullByte() & 0xff) + ((pullByte() & 0xff) << 8));
	}

	public byte PS() {
		byte b = (byte)(
			(NEGATIVE?0x80:0) + (OVERFLOW?0x40:0) + (BREAK_COMMAND?0x10:0) +
			(DECIMAL_MODE?0x08:0) + (INTERRUPT_DISABLE?0x04:0) + (ZERO?0x02:0) + (CARRY?0x01:0));
		return b;
	}
	
	public void PS(byte b) {
		int i = b & 0xff; 
		NEGATIVE = (i & 0x80) > 0; OVERFLOW = (i & 0x40) > 0; BREAK_COMMAND = (i & 0x10) > 0;
		DECIMAL_MODE = (i & 0x08) > 0; INTERRUPT_DISABLE = (i & 0x04) > 0; ZERO = (i & 0x02) > 0; CARRY = (i & 0x01) > 0;
	}

	public String printState() {
		String str = "";
		str = str + 
		"A: " + String.format("%02x", A) +
		", X: " + String.format("%02x", X) +
		", Y: " + String.format("%02x", Y) +
		", SP: " + String.format("%02x", SP) +
		", PC: " + String.format("%04x", (int)PC) +
		",  Flags: " + String.format("%08d", Integer.parseInt(Integer.toBinaryString(PS() & 0xff)));
		return str;
	}

	public String printMemory(int fromAddress, int count) {
		String str = "";
		for(int i = 0; i < count; i++)
			str = str + String.format("%02x ", memory.readByte(fromAddress + i));
		return str;
	}

	public void debug(String title) {
		if (trace) showDebug(title);
		else if (debug) System.out.println(title);
	}
	
	public void showDebug(String title) {
		System.out.println("PROCESSOR PAUSED\n" + printState());
		int res;
		do {
			res = Debugger.show(title, "PROCESSOR STATUS:\n\n" + printState() + "\n\n", new String[] {"Continue", trace?"Stop Trace":"Start Trace", "Abort"}); 
			if (res == 1) trace = !trace;
		} while(res != 0 && res != 2);
		if (res == 2) ((Clock)Thread.currentThread()).terminate();
	}

	private void showTrace() {
		showDebug(">>> TRACE");
	}
	
	public M6502State saveState() {
		M6502State state = new M6502State();
		state.PC = PC; state.A = A; state.X = X; state.Y = Y; state.SP = SP; 
		state.CARRY = CARRY; state.ZERO = ZERO; state.OVERFLOW = OVERFLOW; state.NEGATIVE = NEGATIVE;
		state.DECIMAL_MODE = DECIMAL_MODE; state.INTERRUPT_DISABLE = INTERRUPT_DISABLE; state.BREAK_COMMAND = BREAK_COMMAND;
		state.RDY = RDY;
		state.trace = trace; state.debug = debug;
		state.pageCrossed = pageCrossed;
		state.instructionToExecute = instructionToExecute;
		state.cyclesToExecute = cyclesToExecute;
		return state;
	}
	
	public void loadState(M6502State state) {
		PC = state.PC; A = state.A; X = state.X; Y = state.Y; SP = state.SP; 
		CARRY = state.CARRY; ZERO = state.ZERO; OVERFLOW = state.OVERFLOW; NEGATIVE = state.NEGATIVE;
		DECIMAL_MODE = state.DECIMAL_MODE; INTERRUPT_DISABLE = state.INTERRUPT_DISABLE; BREAK_COMMAND = state.BREAK_COMMAND;
		RDY = state.RDY;
		trace = state.trace; debug = state.debug;
		pageCrossed = state.pageCrossed;
		instructionToExecute = state.instructionToExecute;
		if (instructionToExecute != null)
			instructionToExecute.cpu = this;
		cyclesToExecute = state.cyclesToExecute;
	}


	// Public real 6502 registers and memory, for instructions and general access

	public byte A, X, Y, SP;
	public char PC;
	public boolean CARRY, ZERO, OVERFLOW, NEGATIVE, DECIMAL_MODE, INTERRUPT_DISABLE, BREAK_COMMAND;
	public boolean RDY;		// RDY pin, used to halt the processor
	public BUS16Bits memory;


	// Auxiliary flags and variables for internal, debugging and instructions use
	
	public boolean trace = false;
	public boolean debug = false;
	public boolean pageCrossed = false;
	private int cyclesToExecute = 0;
	private Instruction instructionToExecute;
	

	// Instructions map. # = Undocumented Instruction
	
	public final Instruction[] instructions = {
	/*   00 - BRK                  */  new BRK(this),
	/*   01 - ORA  - (Indirect,X)  */  new ORA(this, IND_X),
	/* # 02 - uKIL                 */  new uKIL(this),
	/* # 03 - uSLO - (Indirect,X)  */  new uSLO(this, IND_X),
	/* # 04 - uNOP - Zero Page     */  new uNOP(this, Z_PAGE),
	/*   05 - ORA  - Zero Page     */  new ORA(this, Z_PAGE),
	/*   06 - ASL  - Zero Page     */  new ASL(this, Z_PAGE),
	/* # 07 - uSLO - Zero Page     */  new uSLO(this, Z_PAGE),
	/*   08 - PHP                  */  new PHP(this),
	/*   09 - ORA  - Immediate     */  new ORA(this, IMM),
	/*   0A - ASL  - Accumulator   */  new ASL(this, ACC),
	/* # 0B - uANC - Immediate     */  new uANC(this),
	/* # 0C - uNOP - Absolute      */  new uNOP(this, ABS),
	/*   0D - ORA  - Absolute      */  new ORA(this, ABS),
	/*   0E - ASL  - Absolute      */  new ASL(this, ABS),
	/* # 0F - uSLO - Absolute      */  new uSLO(this, ABS),
	/*   10 - BPL                  */  new Bxx(this, bNEGATIVE, false),
	/*   11 - ORA  - (Indirect),Y  */  new ORA(this, IND_Y),
	/* # 12 - uKIL                 */  new uKIL(this),
	/* # 13 - uSLO - (Indirect),Y  */  new uSLO(this, IND_Y),
	/* # 14 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   15 - ORA  - Zero Page,X   */  new ORA(this, Z_PAGE_X),
	/*   16 - ASL  - Zero Page,X   */  new ASL(this, Z_PAGE_X),
	/* # 17 - uSLO - Zero Page,X   */  new uSLO(this, Z_PAGE_X),
	/*   18 - CLC                  */  new CLx(this, bCARRY),
	/*   19 - ORA  - Absolute,Y    */  new ORA(this, ABS_Y),
	/* # 1A - uNOP                 */  new NOP(this),
	/* # 1B - uSLO - Absolute,Y    */  new uSLO(this, ABS_Y),
	/* # 1C - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   1D - ORA  - Absolute,X    */  new ORA(this, ABS_X),
	/*   1E - ASL  - Absolute,X    */  new ASL(this, ABS_X),
	/* # 1F - uSLO - Absolute,X    */  new uSLO(this, ABS_X),
	/*   20 - JSR                  */  new JSR(this),
	/*   21 - AND  - (Indirect,X)  */  new AND(this, IND_X),
	/* # 22 - uKIL                 */  new uKIL(this),
	/* # 23 - uRLA - (Indirect,X)  */  new uRLA(this, IND_X),
	/*   24 - BIT  - Zero Page     */  new BIT(this, Z_PAGE),
	/*   25 - AND  - Zero Page     */  new AND(this, Z_PAGE),
	/*   26 - ROL  - Zero Page     */  new ROL(this, Z_PAGE),
	/* # 27 - uRLA - Zero Page     */  new uRLA(this, Z_PAGE),
	/*   28 - PLP                  */  new PLP(this),
	/*   29 - AND  - Immediate     */  new AND(this, IMM),
	/*   2A - ROL  - Accumulator   */  new ROL(this, ACC),
	/* # 2B - uANC - Immediate     */  new uANC(this),
	/*   2C - BIT  - Absolute      */  new BIT(this, ABS),
	/*   2D - AND  - Absolute      */  new AND(this, ABS),
	/*   2E - ROL  - Absolute      */  new ROL(this, ABS),
	/* # 2F - uRLA - Absolute      */  new uRLA(this, ABS),
	/*   30 - BMI                  */  new Bxx(this, bNEGATIVE, true),
	/*   31 - AND  - (Indirect),Y  */  new AND(this, IND_Y),
	/* # 32 - uKIL                 */  new uKIL(this),
	/* # 33 - uRLA - (Indirect),Y  */  new uRLA(this, IND_Y),
	/* # 34 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   35 - AND  - Zero Page,X   */  new AND(this, Z_PAGE_X),
	/*   36 - ROL  - Zero Page,X   */  new ROL(this, Z_PAGE_X),
	/* # 37 - uRLA - Zero Page,X   */  new uRLA(this, Z_PAGE_X),
	/*   38 - SEC                  */  new SEx(this, bCARRY),
	/*   39 - AND  - Absolute,Y    */  new AND(this, ABS_Y),
	/* # 3A - uNOP                 */  new NOP(this),
	/* # 3B - uRLA - Absolute,Y    */  new uRLA(this, ABS_Y),
	/* # 3C - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   3D - AND  - Absolute,X    */  new AND(this, ABS_X),
	/*   3E - ROL  - Absolute,X    */  new ROL(this, ABS_X),
	/* # 3F - uRLA - Absolute,X    */  new uRLA(this, ABS_X),
	/*   40 - RTI                  */  new RTI(this),
	/*   41 - EOR  - (Indirect,X)  */  new EOR(this, IND_X),
	/* # 42 - uKIL                 */  new uKIL(this),
	/* # 43 - uSRE - (Indirect,X)  */  new uSRE(this, IND_X),
	/* # 44 - uNOP - Zero Page     */  new uNOP(this, Z_PAGE),
	/*   45 - EOR  - Zero Page     */  new EOR(this, Z_PAGE),
	/*   46 - LSR  - Zero Page     */  new LSR(this, Z_PAGE),
	/* # 47 - uSRE - Zero Page     */  new uSRE(this, Z_PAGE),
	/*   48 - PHA                  */  new PHA(this),
	/*   49 - EOR  - Immediate     */  new EOR(this, IMM),
	/*   4A - LSR  - Accumulator   */  new LSR(this, ACC),
	/* # 4B - uASR - Immediate     */  new uASR(this),
	/*   4C - JMP  - Absolute      */  new JMP(this, ABS),
	/*   4D - EOR  - Absolute      */  new EOR(this, ABS),
	/*   4E - LSR  - Absolute      */  new LSR(this, ABS),
	/* # 4F - uSRE - Absolute      */  new uSRE(this, ABS),
	/*   50 - BVC                  */  new Bxx(this, bOVERFLOW, false),
	/*   51 - EOR  - (Indirect),Y  */  new EOR(this, IND_Y),
	/* # 52 - uKIL                 */  new uKIL(this),
	/* # 53 - uSRE - (Indirect),Y  */  new uSRE(this, IND_Y),
	/* # 54 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   55 - EOR  - Zero Page,X   */  new EOR(this, Z_PAGE_X),
	/*   56 - LSR  - Zero Page,X   */  new LSR(this, Z_PAGE_X),
	/* # 57 - uSRE - Zero Page,X   */  new uSRE(this, Z_PAGE_X),
	/*   58 - CLI                  */  new CLx(this, bINTERRUPT_DISABLE),
	/*   59 - EOR  - Absolute,Y    */  new EOR(this, ABS_Y),
	/* # 5A - uNOP                 */  new NOP(this),
	/* # 5B - uSRE - Absolute,Y    */  new uSRE(this, ABS_Y),
	/* # 5C - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   5D - EOR  - Absolute,X    */  new EOR(this, ABS_X),
	/*   5E - LSR  - Absolute,X    */  new LSR(this, ABS_X),
	/* # 5F - uSRE - Absolute,X    */  new uSRE(this, ABS_X),
	/*   60 - RTS                  */  new RTS(this),
	/*   61 - ADC  - (Indirect,X)  */  new ADC(this, IND_X),
	/* # 62 - uKIL                 */  new uKIL(this),
	/* # 63 - uRRA - (Indirect,X)  */  new uRRA(this, IND_X),
	/* # 64 - uNOP - Zero Page     */  new uNOP(this, Z_PAGE),
	/*   65 - ADC  - Zero Page     */  new ADC(this, Z_PAGE),
	/*   66 - ROR  - Zero Page     */  new ROR(this, Z_PAGE),
	/* # 67 - uRRA - Zero Page     */  new uRRA(this, Z_PAGE),
	/*   68 - PLA                  */  new PLA(this),
	/*   69 - ADC  - Immediate     */  new ADC(this, IMM),
	/*   6A - ROR  - Accumulator   */  new ROR(this, ACC),
	/* # 6B - uARR - Immediate     */  new uARR(this),
	/*   6C - JMP  - Indirect      */  new JMP(this, IND),
	/*   6D - ADC  - Absolute      */  new ADC(this, ABS),
	/*   6E - ROR  - Absolute      */  new ROR(this, ABS),
	/* # 6F - uRRA - Absolute      */  new uRRA(this, ABS),
	/*   70 - BVS                  */  new Bxx(this, bOVERFLOW, true),
	/*   71 - ADC  - (Indirect),Y  */  new ADC(this, IND_Y),
	/* # 72 - uKIL                 */  new uKIL(this),
	/* # 73 - uRRA - (Indirect),Y  */  new uRRA(this, IND_Y),
	/* # 74 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   75 - ADC  - Zero Page,X   */  new ADC(this, Z_PAGE_X),
	/*   76 - ROR  - Zero Page,X   */  new ROR(this, Z_PAGE_X),
	/* # 77 - uRRA - Zero Page,X   */  new uRRA(this, Z_PAGE_X),
	/*   78 - SEI                  */  new SEx(this, bINTERRUPT_DISABLE),
	/*   79 - ADC  - Absolute,Y    */  new ADC(this, ABS_Y),
	/* # 7A - uNOP                 */  new NOP(this),
	/* # 7B - uRRA - Absolute,Y    */  new uRRA(this, ABS_Y),
	/* # 7C - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   7D - ADC  - Absolute,X    */  new ADC(this, ABS_X),
	/*   7E - ROR  - Absolute,X    */  new ROR(this, ABS_X),
	/* # 7F - uRRA - Absolute,X    */  new uRRA(this, ABS_X),
	/* # 80 - uNOP - Immediate     */  new uNOP(this, IMM),
	/*   81 - STA  - (Indirect,X)  */  new STx(this, rA, IND_X),
	/* # 82 - uNOP - Immediate     */  new uNOP(this, IMM),
	/* # 83 - uSAX - (Indirect,X)  */  new uSAX(this, IND_X),
	/*   84 - STY  - Zero Page     */  new STx(this, rY, Z_PAGE),
	/*   85 - STA  - Zero Page     */  new STx(this, rA, Z_PAGE),
	/*   86 - STX  - Zero Page     */  new STx(this, rX, Z_PAGE),
	/* # 87 - uSAX - Zero Page     */  new uSAX(this, Z_PAGE),
	/*   88 - DEY                  */  new DEx(this, rY),
	/* # 89 - uNOP - Immediate     */  new uNOP(this, IMM),
	/*   8A - TXA                  */  new Txx(this, rX, rA),
	/* # 8B - uANE - Immediate     */  new uANE(this),
	/*   8C - STY  - Absolute      */  new STx(this, rY, ABS),
	/*   8D - STA  - Absolute      */  new STx(this, rA, ABS),
	/*   8E - STX  - Absolute      */  new STx(this, rX, ABS),
	/* # 8F - uSAX - Absolute      */  new uSAX(this, ABS),
	/*   90 - BCC                  */  new Bxx(this, bCARRY, false),
	/*   91 - STA  - (Indirect),Y  */  new STx(this, rA, IND_Y),
	/* # 92 - uKIL                 */  new uKIL(this),		// Only Implied
	/* # 93 - uSHA - (Indirect),Y  */  new uSHA(this, IND_Y),
	/*   94 - STY  - Zero Page,X   */  new STx(this, rY, Z_PAGE_X),
	/*   95 - STA  - Zero Page,X   */  new STx(this, rA, Z_PAGE_X),
	/*   96 - STX  - Zero Page,Y   */  new STx(this, rX, Z_PAGE_Y),
	/* # 97 - uSAX - Zero Page,Y   */  new uSAX(this, Z_PAGE_Y),
	/*   98 - TYA                  */  new Txx(this, rY, rA),
	/*   99 - STA  - Absolute,Y    */  new STx(this, rA, ABS_Y),
	/*   9A - TXS                  */  new Txx(this, rX, rSP),
	/* # 9B - uSHS - Absolute,Y    */  new uSHS(this),
	/* # 9C - uSHY - Absolute,X    */  new uSHY(this),
	/*   9D - STA  - Absolute,X    */  new STx(this, rA, ABS_X),
	/* # 9E - uSHX - Absolute,Y    */  new uSHX(this),
	/* # 9F - uSHA - Absolute, Y   */  new uSHA(this, ABS_Y),
	/*   A0 - LDY  - Immediate     */  new LDx(this, rY, IMM),
	/*   A1 - LDA  - (Indirect,X)  */  new LDx(this, rA, IND_X),
	/*   A2 - LDX  - Immediate     */  new LDx(this, rX, IMM),
	/* # A3 - uLAX - (Indirect,X)  */  new uLAX(this, IND_X),
	/*   A4 - LDY  - Zero Page     */  new LDx(this, rY, Z_PAGE),
	/*   A5 - LDA  - Zero Page     */  new LDx(this, rA, Z_PAGE),
	/*   A6 - LDX  - Zero Page     */  new LDx(this, rX, Z_PAGE),
	/* # A7 - uLAX - Zero Page     */  new uLAX(this, Z_PAGE),
	/*   A8 - TAY                  */  new Txx(this, rA, rY),
	/*   A9 - LDA  - Immediate     */  new LDx(this, rA, IMM),
	/*   AA - TAX                  */  new Txx(this, rA, rX),
	/* # AB - uLXA - Immediate     */  new uLXA(this),
	/*   AC - LDY  - Absolute      */  new LDx(this, rY, ABS),
	/*   AD - LDA  - Absolute      */  new LDx(this, rA, ABS),
	/*   AE - LDX  - Absolute      */  new LDx(this, rX, ABS),
	/* # AF - uLAX - Absolute      */  new uLAX(this, ABS),
	/*   B0 - BCS                  */  new Bxx(this, bCARRY, true),
	/*   B1 - LDA  - (Indirect),Y  */  new LDx(this, rA, IND_Y),
	/* # B2 - uKIL                 */  new uKIL(this),
	/* # B3 - uLAX - (Indirect),Y  */  new uLAX(this, IND_Y),
	/*   B4 - LDY  - Zero Page,X   */  new LDx(this, rY, Z_PAGE_X),
	/*   BS - LDA  - Zero Page,X   */  new LDx(this, rA, Z_PAGE_X),
	/*   B6 - LDX  - Zero Page,Y   */  new LDx(this, rX, Z_PAGE_Y),
	/* # B7 - uLAX - Zero Page,Y   */  new uLAX(this, Z_PAGE_Y),
	/*   B8 - CLV                  */  new CLx(this, bOVERFLOW),
	/*   B9 - LDA  - Absolute,Y    */  new LDx(this, rA, ABS_Y),
	/*   BA - TSX                  */  new Txx(this, rSP, rX),
	/* # BB - uLAS - Absolute,Y    */  new uLAS(this),
	/*   BC - LDY  - Absolute,X    */  new LDx(this, rY, ABS_X),
	/*   BD - LDA  - Absolute,X    */  new LDx(this, rA, ABS_X),
	/*   BE - LDX  - Absolute,Y    */  new LDx(this, rX, ABS_Y),
	/* # BF - uLAX - Absolute,Y    */  new uLAX(this, ABS_Y),
	/*   C0 - CPY  - Immediate     */  new CPx(this, rY, IMM),
	/*   C1 - CMP  - (Indirect,X)  */  new CPx(this, rA, IND_X),
	/* # C2 - uNOP - Immediate     */  new uNOP(this, IMM),
	/* # C3 - uDCP - (Indirect,X)  */  new uDCP(this, IND_X),
	/*   C4 - CPY  - Zero Page     */  new CPx(this, rY, Z_PAGE),
	/*   C5 - CMP  - Zero Page     */  new CPx(this, rA, Z_PAGE),
	/*   C6 - DEC  - Zero Page     */  new DEC(this, Z_PAGE),
	/* # C7 - uDCP - Zero Page     */  new uDCP(this, Z_PAGE),
	/*   C8 - INY                  */  new INx(this, rY),
	/*   C9 - CMP  - Immediate     */  new CPx(this, rA, IMM),
	/*   CA - DEX                  */  new DEx(this, rX),
	/* # CB - uSBX - Immediate     */  new uSBX(this),
	/*   CC - CPY  - Absolute      */  new CPx(this, rY, ABS),
	/*   CD - CMP  - Absolute      */  new CPx(this, rA, ABS),
	/*   CE - DEC  - Absolute      */  new DEC(this, ABS),
	/* # CF - uDCP - Absolute      */  new uDCP(this, ABS),
	/*   D0 - BNE                  */  new Bxx(this, bZERO, false),
	/*   D1 - CMP  - (Indirect),Y  */  new CPx(this, rA, IND_Y),
	/* # D2 - uKIL                 */  new uKIL(this),
	/* # D3 - uDCP - (Indirect),Y  */  new uDCP(this, IND_Y),
	/* # D4 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   D5 - CMP  - Zero Page,X   */  new CPx(this, rA, Z_PAGE_X),
	/*   D6 - DEC  - Zero Page,X   */  new DEC(this, Z_PAGE_X),
	/* # D7 - uDCP - Zero Page, X  */  new uDCP(this, Z_PAGE_X),
	/*   D8 - CLD                  */  new CLx(this, bDECIMAL_MODE),
	/*   D9 - CMP  - Absolute,Y    */  new CPx(this, rA, ABS_Y),
	/* # DA - uNOP                 */  new NOP(this),
	/* # DB - uDCP - Absolute,Y    */  new uDCP(this, ABS_Y),
	/* # DC - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   DD - CMP  - Absolute,X    */  new CPx(this, rA, ABS_X),
	/*   DE - DEC  - Absolute,X    */  new DEC(this, ABS_X),
	/* # DF - uDCP - Absolute,X    */  new uDCP(this, ABS_X),
	/*   E0 - CPX  - Immediate     */  new CPx(this, rX, IMM),
	/*   E1 - SBC  - (Indirect,X)  */  new SBC(this, IND_X),
	/* # E2 - uNOP - Immediate     */  new uNOP(this, IMM),
	/* # E3 - uISB - (Indirect,X)  */  new uISB(this, IND_X),
	/*   E4 - CPX  - Zero Page     */  new CPx(this, rX, Z_PAGE),
	/*   E5 - SBC  - Zero Page     */  new SBC(this, Z_PAGE),
	/*   E6 - INC  - Zero Page     */  new INC(this, Z_PAGE),
	/* # E7 - uISB - Zero Page     */  new uISB(this, Z_PAGE),
	/*   E8 - INX                  */  new INx(this, rX),
	/*   E9 - SBC  - Immediate     */  new SBC(this, IMM),
	/*   EA - NOP                  */  new NOP(this),
	/* # EB - uSBC - Immediate     */  new SBC(this, IMM),
	/*   EC - CPX  - Absolute      */  new CPx(this, rX, ABS),
	/*   ED - SBC  - Absolute      */  new SBC(this, ABS),
	/*   EE - INC  - Absolute      */  new INC(this, ABS),
	/* @ EF - uISB - Absolute      */  new uISB(this, ABS),
	/*   F0 - BEQ                  */  new Bxx(this, bZERO, true),
	/*   F1 - SBC  - (Indirect),Y  */  new SBC(this, IND_Y),
	/* # F2 - uKIL                 */  new uKIL(this),
	/* # F3 - uISB - (Indirect),Y  */  new uISB(this, IND_Y),
	/* # F4 - uNOP - Zero Page,X   */  new uNOP(this, Z_PAGE_X),
	/*   F5 - SBC  - Zero Page,X   */  new SBC(this, Z_PAGE_X),
	/*   F6 - INC  - Zero Page,X   */  new INC(this, Z_PAGE_X),
	/* # F7 - uISB - Zero Page,X   */  new uISB(this, Z_PAGE_X),
	/*   F8 - SED                  */  new SEx(this, bDECIMAL_MODE),
	/*   F9 - SBC  - Absolute,Y    */  new SBC(this, ABS_Y),
	/* # FA - uNOP                 */  new NOP(this),
	/* # FB - uISB - Absolute,Y    */  new uISB(this, ABS_Y),
	/* # FC - uNOP - Absolute,X    */  new uNOP(this, ABS_X),
	/*   FD - SBC  - Absolute,X    */  new SBC(this, ABS_X),
	/*   FE - INC  - Absolute,X    */  new INC(this, ABS_X),
	/* # FF - uISB - Absolute,X    */  new uISB(this, ABS_X) 
	};
	

	// Constants
	
	public static char NMI_HANDLER_ADDRESS = 0xfffa;
	public static char POWER_ON_RESET_ADDRESS = 0xfffc;
	public static char IRQ_HANDLER_ADDRESS = 0xfffe;

	public static byte STACK_INITIAL_SP = (byte)0xff;
	public static char STACK_PAGE = (char)0x0100;
	
	// Convenience methods
	
	public static int toUunsignedByte(byte b) {	// ** NOTE does not return a real byte for signed operations
		return b & 0xff;
	}
	public static int toUnsignedByte(int i) {	// ** NOTE does not return a real byte for signed operations
		return toUunsignedByte((byte)i);
	}

	//Used to save/load states
	public static class M6502State implements Serializable {
		byte A, X, Y, SP;
		char PC;
		boolean CARRY, ZERO, OVERFLOW, NEGATIVE, DECIMAL_MODE, INTERRUPT_DISABLE, BREAK_COMMAND;
		boolean RDY;
		boolean trace;
		boolean debug;
		boolean pageCrossed;
		Instruction instructionToExecute;
		int cyclesToExecute;

		public static final long serialVersionUID = 2L;
	}

}


