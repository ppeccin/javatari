// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

import general.av.audio.AudioMonitor;
import general.av.audio.AudioSignal;
import general.av.video.VideoStandard;

import javax.sound.sampled.AudioFormat;

import parameters.Parameters;

public abstract class AudioGenerator implements AudioSignal {

	@Override
	public abstract AudioFormat getAudioFormat();

	@Override
	public void connectMonitor(AudioMonitor monitor) {
		this.monitor = monitor;
	}

	public void generateNextSamples(int samples) {
		int remainingSamples = Math.max(desiredSamplesPerFrame() - generatedSamples, 0);
		internalGenerateNextSamples(Math.min(samples, remainingSamples));
	}

	public ChannelStream channel0() {
		return channel0;
	}

	public ChannelStream channel1() {
		return channel1;
	}

	public void sendSamplesFrameToMonitor() {
		int missingSamples = desiredSamplesPerFrame() - generatedSamples;
		if (missingSamples > 0) generateNextSamples(missingSamples);
		if (monitor != null) monitor.nextSamples(samples, generatedSamples);
		generatedSamples = 0;
	}

	protected abstract void internalGenerateNextSamples(int min);

	private int desiredSamplesPerFrame() {
		return videoStandard.height * 2;		// Perfect amount is 2 samples per scan line
	}

	protected final ChannelStream channel0 = new ChannelStream(); 
	protected final ChannelStream channel1 = new ChannelStream(); 

	public AudioMonitor monitor;
	public VideoStandard videoStandard;

	protected int generatedSamples = 0;
	
	protected final byte[] samples = new byte[1024];	// More than enough samples for a frame

	protected static final float MAX_AMPLITUDE = Parameters.TIA_AUDIO_MAX_AMPLITUDE;

}
