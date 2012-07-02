// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.speaker;

import general.av.audio.AudioMonitor;
import general.av.audio.AudioSignal;
import general.board.Clock;
import general.board.ClockDriven;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import parameters.Parameters;

public class Speaker implements ClockDriven, AudioMonitor  {

	public Speaker() {
		super();
		fps = DEFAULT_FPS;
		clock = new Clock("Speaker", this, fps);
	}
	
	public void connect(AudioSignal signal) {	// Must be powered off to connect a signal
		this.signal = signal;
		signal.connectMonitor(this);
	}

	public void powerOn(){
		if (dataLine == null) getLine();
		if (dataLine == null) return;
		dataLine.start();
		clock.go();
	}

	public void powerOff(){
		if (dataLine == null) return;
		dataLine.stop();
		clock.pause();
	}

	public void destroy() {
		dataLine.close();
		dataLine = null;
		clock.terminate();
	}

	@Override
	public synchronized void nextSamples(byte[] buffer, int quant) {
		// Drop samples that don't fit the input buffer available capacity
		int ava = inputBuffer.remaining();
		if (ava > quant)
			ava = quant;
		// else
		//	System.out.println(">>>> DROPPED: " + (quant - ava));
		inputBuffer.put(buffer, 0, ava);
	}
	
	@Override
	public void synchOutput() {
		refresh();
	}

	@Override
	public void clockPulse() {
		synchOutput();
	}

	private void getLine() {
		if (signal == null) return;
		AudioFormat audioFormat = signal.getAudioFormat();
		try {
			dataLine = AudioSystem.getSourceDataLine(audioFormat);
			dataLine.open(audioFormat, OUTPUT_BUFFER_SIZE);
			inputBuffer = ByteBuffer.allocateDirect(INPUT_BUFFER_SIZE);
			tempBuffer = new byte[inputBuffer.capacity()];
			if (ADDED_THREAD_PRIORITY != 0) clock.setPriority(Thread.NORM_PRIORITY + ADDED_THREAD_PRIORITY);
			System.out.println("Sound Mixer Line: " + dataLine);
			System.out.println("Sound Output buffer: " + dataLine.getBufferSize());
		} catch (Exception ex) {
			System.out.println("Unable to acquire audio line:\n" + ex);
			dataLine = null;
		}
	}

	private synchronized int getFromInputBuffer(byte[] buffer, int quant) {
		inputBuffer.flip();
		int ava = inputBuffer.remaining();
		if (ava > quant)
			ava = quant;
		inputBuffer.get(buffer, 0, ava);
		inputBuffer.compact();
		return ava;
	}

	private void refresh() {
		int ava = dataLine.available();		// this is a little expensive... :-(
		if (ava == 0) {
			if (OUTPUT_BUFFER_FULL_SLEEP_TIME > 0 && fps < 0) {
				// System.out.println("Buffer Full, sleeping...");
				try { Thread.sleep(OUTPUT_BUFFER_FULL_SLEEP_TIME, 0); } catch (InterruptedException e) { }
			}
			return;
		}
		int data = getFromInputBuffer(tempBuffer, ava);
		// System.out.println(ava + ", " + data + ", " + inputBuffer.remaining());
		if (data == 0) {
			if (NO_DATA_SLEEP_TIME > 0 && fps < 0) {
				// System.out.println("NO DATA, sleeping...");
				try { Thread.sleep(NO_DATA_SLEEP_TIME, 0); } catch (InterruptedException e) { }
			}
			return;
		}
		dataLine.write(tempBuffer, 0, data);
	}


	public final Clock clock;
	private final double fps;

	private AudioSignal signal;
	
	private SourceDataLine dataLine;
	private ByteBuffer inputBuffer;
	private byte[] tempBuffer;		
	
	public static final double DEFAULT_FPS = Parameters.SPEAKER_DEFAULT_FPS;	
	public static final int INPUT_BUFFER_SIZE = Parameters.SPEAKER_INPUT_BUFFER_SIZE;					// In frames (samples)
	public static final int OUTPUT_BUFFER_SIZE = Parameters.SPEAKER_OUTPUT_BUFFER_SIZE;					// In frames (samples)
	public static final int OUTPUT_BUFFER_FULL_SLEEP_TIME = Parameters.SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME;	// In milliseconds
	public static final int NO_DATA_SLEEP_TIME = Parameters.SPEAKER_NO_DATA_SLEEP_TIME;					// In milliseconds
	public static final int ADDED_THREAD_PRIORITY = Parameters.SPEAKER_ADDED_THREAD_PRIORITY;

}
