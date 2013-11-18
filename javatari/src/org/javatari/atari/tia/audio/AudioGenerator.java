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
		if (generatedSamples >= 106) sendGeneratedSamples();
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
		sendGeneratedSamples();
		frameSamples = 0;
	}

	private void sendGeneratedSamples() {
		if (monitor != null) {
			int more = monitor.nextSamples(samples, generatedSamples);
			if (more > 0) {
				generateNextSamples(more);
				monitor.nextSamples(samples, generatedSamples);
			}
		}
		generatedSamples = 0;
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

}
