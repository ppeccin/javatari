; Copyright 2011-2014 Paulo Augusto Peccin. See licence.txt distributed with this file.
; Arcadia/Starpath/Supercharger Mock BIOS

                processor 6502

StartRoutAddr = $00fa                 ; RAM address for the Start Routine
PartNoToLoad  = $fa                   ; RAM position containing Part do BE loaded
ControlReg    = $80                   ; RAM position containing Part that WAS loaded

VBLANK        = $01                   ; Some TIA registers needed
WSYNC         = $02
RESP0         = $10
RESP1         = $11
HMP0          = $20
HMP1          = $21
HMOVE         = $2a
AUDV0         = $19
AUDV1         = $1A


                seg   LoadPart        ; Load Part entry point
LoadPart:       org   $f800

				LDX   #$00            ; Ensure Bank configuration, BIOS powered ON and Writes OFF
                CMP   $f000,X
                CMP   $fff8

                LDX   PartNoToLoad
                STA   EmuLoadHotspot,X       ; Trigger Emulator hotspot to actually load Part X
                LDA   iPartLoadedOK          ; Check if Part sucessfuly loaded (0 = fail, 1 = success)
                BNE   LoadOK
                JMP   FailedLoad             ; If it failed, handle failure

LoadOK:         JMP   StartPart


                seg   FailedLoad
FailedLoad:     org   $f880

                LDA   #02             ; Try to put the TIA in a "clean" state
                STA   VBLANK
                LDA   #00
                STA   AUDV0
                STA   AUDV1
                JMP   LoadPart        ; Try again, possible for ever...


                seg   StartPart       ; Finish preparations and start Part loaded
StartPart:      org   $f900

				LDA   #00             ; Clear TIA registers and RAM $80 - $9d as per original BIOS
                LDX   #$9e
LoopClrSome:    DEX
                STA   $00,X
                BNE   LoopClrSome

                LDA   #$10            ; Set Players position as per original BIOS
                STA   HMP1
                STA   WSYNC
                LDX   #$07
                DEX
LoopPos:        DEX
                BNE   LoopPos
                LDA   #00
                STA   HMP0
                STA   RESP0
                STA   RESP1
                STA   WSYNC
                STA   HMOVE

                LDX   #3              ; Load Start Routine code into RAM
LoopLoadSR:     LDA   StartRoutCode,X
                STA   StartRoutAddr,X
                DEX
                BPL   LoopLoadSR
                LDX   #1              ; Set Start Address into Start Routine in RAM
LoopSetSA:      LDA   iStartAddr,X
                STA   StartRoutAddr + 4,X
                DEX
                BPL   LoopSetSA

                LDX   iControlReg     ; Finish preparations of registers as per original BIOS
                STX   ControlReg      ; Store ControlReg
                CMP   $f000,X         ; Signal value to set to ControlReg
                LDA   iRandomSeed     ; Prepare final A, X, Y, SP contents as per original BIOS
                LDY   #00
                LDX   #$ff
                TXS
                JMP   StartRoutAddr   ; Final jump to Start Routine


                seg    StartRoutineCode     ; Will be copied to and be run from RAM
StartRoutCode:  org    $fa00

                CMP    $fff8          ; Finally set ControlReg
                JMP    $0000          ; Jump to ROM Startup Address


                seg    EmuInterface   ; Interface area with the Emulator
                org    $fb00

iPartNoToLoad:  byte   #00
iControlReg:    byte   #00
iStartAddr:     word   #0000
iRandomSeed:    byte   #00
iPartLoadedOK:  byte   #00


                seg    EmuLoadHotspots      ; Signals the Emulator to load Part specified
EmuLoadHotspot: org    $fc00				; From $fc00 to $fcff


                seg    SystemReset    ; Reset routine
SysReset:       org    $fd00

				SEI
				CLD

                LDA   #00             ; Clear RAM and TIA
                TAX
LoopClrMem:     DEX
                STA   $00,X
                BNE   LoopClrMem

                STA    PartNoToLoad   ; Order to load Part 0
                JMP    LoadPart


                seg    Vectors
                org    $fffa

NMI:            word   SysReset
RESET:          word   SysReset
IRQ:            word   SysReset
