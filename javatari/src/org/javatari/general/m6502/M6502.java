// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502;

import static org.javatari.general.m6502.OperandType.ABS;
import static org.javatari.general.m6502.OperandType.ABS_X;
import static org.javatari.general.m6502.OperandType.ABS_Y;
import static org.javatari.general.m6502.OperandType.ACC;
import static org.javatari.general.m6502.OperandType.IMM;
import static org.javatari.general.m6502.OperandType.IND;
import static org.javatari.general.m6502.OperandType.IND_X;
import static org.javatari.general.m6502.OperandType.IND_Y;
import static org.javatari.general.m6502.OperandType.Z_PAGE;
import static org.javatari.general.m6502.OperandType.Z_PAGE_X;
import static org.javatari.general.m6502.OperandType.Z_PAGE_Y;
import static org.javatari.general.m6502.Register.rA;
import static org.javatari.general.m6502.Register.rSP;
import static org.javatari.general.m6502.Register.rX;
import static org.javatari.general.m6502.Register.rY;
import static org.javatari.general.m6502.StatusBit.bCARRY;
import static org.javatari.general.m6502.StatusBit.bDECIMAL_MODE;
import static org.javatari.general.m6502.StatusBit.bINTERRUPT_DISABLE;
import static org.javatari.general.m6502.StatusBit.bNEGATIVE;
import static org.javatari.general.m6502.StatusBit.bOVERFLOW;
import static org.javatari.general.m6502.StatusBit.bZERO;

import java.io.Serializable;

import org.javatari.general.board.BUS16Bits;
import org.javatari.general.board.Clock;
import org.javatari.general.board.ClockDriven;
import org.javatari.general.m6502.instructions.ADC;
import org.javatari.general.m6502.instructions.AND;
import org.javatari.general.m6502.instructions.ASL;
import org.javatari.general.m6502.instructions.BIT;
import org.javatari.general.m6502.instructions.BRK;
import org.javatari.general.m6502.instructions.Bxx;
import org.javatari.general.m6502.instructions.CLx;
import org.javatari.general.m6502.instructions.CPx;
import org.javatari.general.m6502.instructions.DEC;
import org.javatari.general.m6502.instructions.DEx;
import org.javatari.general.m6502.instructions.EOR;
import org.javatari.general.m6502.instructions.INC;
import org.javatari.general.m6502.instructions.INx;
import org.javatari.general.m6502.instructions.JMP;
import org.javatari.general.m6502.instructions.JSR;
import org.javatari.general.m6502.instructions.LDx;
import org.javatari.general.m6502.instructions.LSR;
import org.javatari.general.m6502.instructions.NOP;
import org.javatari.general.m6502.instructions.ORA;
import org.javatari.general.m6502.instructions.PHA;
import org.javatari.general.m6502.instructions.PHP;
import org.javatari.general.m6502.instructions.PLA;
import org.javatari.general.m6502.instructions.PLP;
import org.javatari.general.m6502.instructions.ROL;
import org.javatari.general.m6502.instructions.ROR;
import org.javatari.general.m6502.instructions.RTI;
import org.javatari.general.m6502.instructions.RTS;
import org.javatari.general.m6502.instructions.SBC;
import org.javatari.general.m6502.instructions.SEx;
import org.javatari.general.m6502.instructions.STx;
import org.javatari.general.m6502.instructions.Txx;
import org.javatari.general.m6502.instructions.uANC;
import org.javatari.general.m6502.instructions.uANE;
import org.javatari.general.m6502.instructions.uARR;
import org.javatari.general.m6502.instructions.uASR;
import org.javatari.general.m6502.instructions.uDCP;
import org.javatari.general.m6502.instructions.uISB;
import org.javatari.general.m6502.instructions.uKIL;
import org.javatari.general.m6502.instructions.uLAS;
import org.javatari.general.m6502.instructions.uLAX;
import org.javatari.general.m6502.instructions.uLXA;
import org.javatari.general.m6502.instructions.uNOP;
import org.javatari.general.m6502.instructions.uRLA;
import org.javatari.general.m6502.instructions.uRRA;
import org.javatari.general.m6502.instructions.uSAX;
import org.javatari.general.m6502.instructions.uSBX;
import org.javatari.general.m6502.instructions.uSHA;
import org.javatari.general.m6502.instructions.uSHS;
import org.javatari.general.m6502.instructions.uSHX;
import org.javatari.general.m6502.instructions.uSHY;
import org.javatari.general.m6502.instructions.uSLO;
import org.javatari.general.m6502.instructions.uSRE;
import org.javatari.utils.Debugger;


public final class M6502 implements ClockDriven {

	public M6502() {
	}

	public void connectBus(BUS16Bits bus) {
		this.bus = bus;
	}
	
	public void reset() {
		PC = memoryReadWord(POWER_ON_RESET_ADDRESS);
		INTERRUPT_DISABLE = true;
		currentInstruction = null;
		remainingCycles = -1;
	}
	
	/** This implementation executes all fetch operations on the FIRST cycle, 
	  * then read and write operations on the LAST cycle of each instruction, doing nothing in cycles in between */
	@Override
	public void clockPulse() {
		// If this is the last execution cycle of the instruction, execute it ignoring the !RDY signal 
		if (remainingCycles == 1) {
			// if (trace) showDebug(">>> TRACE");
			currentInstruction.execute();
			remainingCycles = 0;
			return;
		}
		if (!RDY) return;						// CPU is halted
		if (remainingCycles-- > 0) return;		// CPU is still "executing" remaining instruction cycles
		currentInstruction = instructions[toUnsignedByte(bus.readByte(PC++))];	// Reads the instruction to be executed
		remainingCycles = currentInstruction.fetch() - 1;						// One cycle was just executed already!
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
		// Nothing
	}

	public void fetchImpliedAddress() {
		bus.readByte(PC);						// Worthless read, discard data. PC unchanged
	}											// TODO Make instructions call here

	public int fetchImmediateAddress() {		// No memory being read here!
		return PC++;
	}

	public int fetchRelativeAddress() {
		int res = bus.readByte(PC++) + PC;  	// PC should be get AFTER the increment and be added to the offset that was read
		pageCrossed = (res & 0xff00) != (PC & 0xff00);		// TODO Implement additional bad reads
		return res;		
	}

	public int fetchZeroPageAddress() {
		return toUnsignedByte(bus.readByte(PC++));
	}

	public int fetchZeroPageXAddress() {
		byte base = bus.readByte(PC++);
		bus.readByte(toUnsignedByte(base));		// Additional bad read, discard data
		return toUnsignedByte(base + X);		// Sum should wrap the byte and always be in range 00 - ff
	}

	public int fetchZeroPageYAddress() {
		byte base = bus.readByte(PC++);
		bus.readByte(toUnsignedByte(base));		// Additional bad read, discard data
		return toUnsignedByte(base + Y);		// Sum should wrap the byte and always be in range 00 - ff
	}
	
	public int fetchAbsoluteAddress() {
		return memoryReadWord((PC+=2) - 2);		// PC should be get BEFORE the double increment 
	}

	public int fetchAbsoluteXAddress() {
		final int addr = fetchAbsoluteAddress();
		final int res = addr + toUnsignedByte(X);
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		if (pageCrossed) bus.readByte((addr & 0xff00) | (res & 0x00ff));		// Additional bad read, discard data
		return res;
	}

	public int fetchAbsoluteYAddress() {
		final int addr = fetchAbsoluteAddress();
		final int res = addr + toUnsignedByte(Y);
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		if (pageCrossed) bus.readByte((addr & 0xff00) | (res & 0x00ff));		// Additional bad read, discard data
		return res;
	}

	public int fetchIndirectAddress() {
		return memoryReadWordWrappingPage(fetchAbsoluteAddress());				// Should wrap page reading effective address
	}

	public int fetchIndirectXAddress() {
		return memoryReadWordWrappingPage(fetchZeroPageXAddress());				// Should wrap page (the zero page) reading effective address
	}

	public int fetchIndirectYAddress() {
		final int addr = memoryReadWordWrappingPage(fetchZeroPageAddress());	// Should wrap page (the zero page) reading effective address
		final int res = addr + toUnsignedByte(Y);
		pageCrossed = (res & 0xff00) != (addr & 0xff00);
		if (pageCrossed) bus.readByte((addr & 0xff00) | (res & 0x00ff));		// Additional bad read, discard data
		return res; 
	}

	public int memoryReadWord(int address) {
		return toUnsignedByte(bus.readByte(address)) | (toUnsignedByte(bus.readByte(address + 1)) << 8);	// Address + 1 may wrap, LSB first
	}

	public int memoryReadWordWrappingPage(int address) {	// Accounts for the page-cross problem  (should wrap page)
		if ((address & 0xff) == 0xff)		
			// Get hi byte from the page-wrap &xx00 (addr + 1 wraps to beginning of page)
			return toUnsignedByte(bus.readByte(address)) | (toUnsignedByte(bus.readByte(address & 0xff00)) << 8);
		else
			return memoryReadWord(address);
	}

	public void pushByte(byte b) {
		bus.writeByte(STACK_PAGE + toUnsignedByte(SP--), b);
	}

	public void dummyStackRead() {
		bus.readByte(STACK_PAGE + toUnsignedByte(SP));		// Additional dummy stack read before SP increment, discard data
	}
	
	public byte pullByte() {
		return bus.readByte(STACK_PAGE + toUnsignedByte(++SP));
	}
	
	public void pushWord(int w) {
		pushByte((byte) (w >>> 8));
		pushByte((byte) w);
	}

	public int pullWord() {
		return (pullByte() & 0xff) | ((pullByte() & 0xff) << 8);
	}

	public byte PS() {
		byte b = (byte)(
			(NEGATIVE?0x80:0) | (OVERFLOW?0x40:0) | (DECIMAL_MODE?0x08:0) | 
			(INTERRUPT_DISABLE?0x04:0) | (ZERO?0x02:0) | (CARRY?0x01:0) |
			BREAK_COMMAND_FLAG	// Software instructions always push PS with BREAK_COMMAND set;
		);
		return b;
	}
	
	public void PS(byte b) {
		NEGATIVE = (b & 0x80) != 0; OVERFLOW = (b & 0x40) != 0;
		DECIMAL_MODE = (b & 0x08) != 0; INTERRUPT_DISABLE = (b & 0x04) != 0; ZERO = (b & 0x02) != 0; CARRY = (b & 0x01) != 0;
		// BREAK_COMMAND actually does not exist as a real flag
	}

	public String printState() {
		String str = "";
		str = str + 
		"A: " + String.format("%02x", A) +
		", X: " + String.format("%02x", X) +
		", Y: " + String.format("%02x", Y) +
		", SP: " + String.format("%02x", SP) +
		", PC: " + String.format("%04x", (int)PC) +
		", Flags: " + String.format("%08d", Integer.parseInt(Integer.toBinaryString(PS() & 0xff))) +
		", Instr: " + (currentInstruction != null ? currentInstruction.getClass().getSimpleName() : "none" ) +  
		", RemCycles: " + remainingCycles;
		return str;
	}

	public String printMemory(int fromAddress, int count) {
		String str = "";
		for(int i = 0; i < count; i++)
			str = str + String.format("%02x ", bus.readByte(fromAddress + i));
		return str;
	}

	public void debug(String title) {
		if (debug) {
			System.out.println(title);
			if (trace) showDebug(title);
		}
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

	public M6502State saveState() {
		M6502State state = new M6502State();
		state.PC = PC; state.A = A; state.X = X; state.Y = Y; state.SP = SP; 
		state.CARRY = CARRY; state.ZERO = ZERO; state.OVERFLOW = OVERFLOW; state.NEGATIVE = NEGATIVE;
		state.DECIMAL_MODE = DECIMAL_MODE; state.INTERRUPT_DISABLE = INTERRUPT_DISABLE;
		state.RDY = RDY;
		state.trace = trace; state.debug = debug;
		state.pageCrossed = pageCrossed;
		if (currentInstruction != null) state.currentInstruction = currentInstruction.clone();
		state.remainingCycles = remainingCycles;
		return state;
	}
	
	public void loadState(M6502State state) {
		PC = state.PC; A = state.A; X = state.X; Y = state.Y; SP = state.SP; 
		CARRY = state.CARRY; ZERO = state.ZERO; OVERFLOW = state.OVERFLOW; NEGATIVE = state.NEGATIVE;
		DECIMAL_MODE = state.DECIMAL_MODE; INTERRUPT_DISABLE = state.INTERRUPT_DISABLE;
		RDY = state.RDY;
		trace = state.trace; debug = state.debug;
		pageCrossed = state.pageCrossed;
		currentInstruction = state.currentInstruction;
		if (currentInstruction != null)	currentInstruction.cpu = this;
		remainingCycles = state.remainingCycles;
	}


	// Public real 6502 registers and memory, for instructions and general access

	public byte A, X, Y, SP;
	public int PC;			// Assumes anyone reading PC should mask it to 0xffff
	public boolean CARRY, ZERO, OVERFLOW, NEGATIVE, DECIMAL_MODE, INTERRUPT_DISABLE;
	public boolean RDY;		// RDY pin, used to halt the processor
	public BUS16Bits bus;


	// Auxiliary flags and variables for internal, debugging and instructions use
	
	public boolean trace = false;
	public boolean debug = false;
	public boolean pageCrossed = false;
	private int remainingCycles = -1;
	private Instruction currentInstruction;
	

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
	/* # EF - uISB - Absolute      */  new uISB(this, ABS),
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
	public static final byte STACK_INITIAL_SP = (byte)0xff;
	public static final int STACK_PAGE = 0x0100;
	public static final byte BREAK_COMMAND_FLAG = 0x10;
	
	// Vectors
	public static final int NMI_HANDLER_ADDRESS = 0xfffa;
	public static final int POWER_ON_RESET_ADDRESS = 0xfffc;
	public static final int IRQ_HANDLER_ADDRESS = 0xfffe;

	
	// Convenience methods
	public static int toUnsignedByte(byte b) {	// ** NOTE does not return a real byte for signed operations
		return b & 0xff;
	}
	public static int toUnsignedByte(int i) {	// ** NOTE does not return a real byte for signed operations
		return i & 0xff;
	}

	// Used to save/load states
	public static class M6502State implements Serializable {
		byte A, X, Y, SP;
		int PC;
		boolean CARRY, ZERO, OVERFLOW, NEGATIVE, DECIMAL_MODE, INTERRUPT_DISABLE, BREAK_COMMAND;
		boolean RDY;
		boolean trace;
		boolean debug;
		boolean pageCrossed;
		Instruction currentInstruction;
		int remainingCycles;

		public static final long serialVersionUID = 2L;
	}

}


