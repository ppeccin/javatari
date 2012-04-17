// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

import general.av.audio.AudioMonitor;
import general.av.audio.AudioSignal;

import javax.sound.sampled.AudioFormat;

import parameters.Parameters;

public abstract class AudioGenerator implements AudioSignal {

	@Override
	public abstract AudioFormat getAudioFormat();

	@Override
	public void connectMonitor(AudioMonitor monitor) {
		this.monitor = monitor;
	}

	public abstract void generateNextSamples(int samples);

	public ChannelStream channel0() {
		return channel0;
	}

	public ChannelStream channel1() {
		return channel1;
	}

	public void sendGeneratedSamplesToMonitor() {
		if (monitor != null)
			monitor.nextSamples(samples, generatedSamples);
		generatedSamples = 0;
	}

	protected final ChannelStream channel0 = new ChannelStream(); 
	protected final ChannelStream channel1 = new ChannelStream(); 

	public AudioMonitor monitor;
	
	public int generatedSamples = 0;
	
	protected final byte[] samples = new byte[1048];

	protected static final float MAX_AMPLITUDE = Parameters.TIA_AUDIO_MAX_AMPLITUDE;

}
