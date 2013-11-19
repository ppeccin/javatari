// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.tia.audio;

import org.javatari.general.av.audio.AudioMonitor;
import org.javatari.general.av.audio.AudioSignal;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.board.ClockDriven;
import org.javatari.parameters.Parameters;


public abstract class AudioGenerator implements AudioSignal, ClockDriven {

	@Override
	public void connectMonitor(AudioMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void clockPulse() {
		if (frameSamples < samplesPerFrame) generateNextSamples(1);
		if (generatedSamples >= SEND_CHUNK) sendGeneratedSamples();
	}

	public AudioMonitor monitor() {
		return monitor;
	}

	public ChannelStream channel0() {
		return channel0;
	}

	public ChannelStream channel1() {
		return channel1;
	}

	public void videoStandard(VideoStandard standard) {
		// Perfect amount is 2 sample per scanline = 31440, 524 for NTSC(60Hz) and 624 for PAL(50hz)
		samplesPerFrame = (int) Math.round(SAMPLE_RATE / standard.fps);	
	}

	public void signalOff() {
		generatedSamples = 0;
		frameSamples = 0;
		if (monitor != null) monitor.nextSamples(null, 0);
	}

	public void finishFrame() {
		int missingSamples = samplesPerFrame - frameSamples;
		if (missingSamples > 0) generateNextSamples(missingSamples);
			
		int available = sendGeneratedSamples();

		// Check available samples on monitor to prevent starvation. Send additional samples if needed
		if (available >= 0 && available < MIN_MONITOR_BUFFER_CHUNKS * SEND_CHUNK) {
			int add = MIN_MONITOR_BUFFER_CHUNKS * SEND_CHUNK - available + SEND_CHUNK / MONITOR_BUFFER_CHUNKS_ADD_FACTOR;
			generateNextSamples(add);
			sendGeneratedSamples();
			// System.out.println("Available in Monitor: " + available + ", add: " + add);
		}
		
		frameSamples = 0;
	}

	private int sendGeneratedSamples() {
		int available = -1;
		if (monitor != null)
			available = monitor.nextSamples(samples, generatedSamples);
		generatedSamples = 0;
		return available;
	}

	protected abstract void generateNextSamples(int quant);

	
	protected final ChannelStream channel0 = new ChannelStream(); 
	protected final ChannelStream channel1 = new ChannelStream(); 
	protected final byte[] samples = new byte[2048];	// More than enough samples for a frame
	protected int generatedSamples = 0;
	protected int frameSamples = 0;
	private int samplesPerFrame = 0;

	private AudioMonitor monitor;

	
	private static final int SAMPLE_RATE = Parameters.TIA_AUDIO_SAMPLE_RATE;
	private static final int SEND_CHUNK = Parameters.TIA_AUDIO_SEND_CHUNK;
	private static final int MIN_MONITOR_BUFFER_CHUNKS = Parameters.TIA_AUDIO_MIN_MONITOR_BUFFER_CHUNKS;
	private static final int MONITOR_BUFFER_CHUNKS_ADD_FACTOR = Parameters.TIA_AUDIO_MONITOR_BUFFER_CHUNKS_ADD_FACTOR;

}
