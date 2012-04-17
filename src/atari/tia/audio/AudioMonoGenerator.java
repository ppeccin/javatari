// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

import javax.sound.sampled.AudioFormat;

import parameters.Parameters;

public final class AudioMonoGenerator extends AudioGenerator {

	@Override
	public void generateNextSamples(int quant) {
		for (int i = 0; i < quant; i++) {
			if (generatedSamples >= samples.length) return;
			float mixedSample = channel0.nextSample() * MAX_MONO_CHANNEL_AMPLITUDE + channel1.nextSample() * MAX_MONO_CHANNEL_AMPLITUDE;
			samples[generatedSamples++] = ((byte) (mixedSample * (MAX_AMPLITUDE * 127)));
		}
	}

	@Override
	public AudioFormat getAudioFormat() {
		return AUDIO_FORMAT;
	}

	private static AudioFormat AUDIO_FORMAT = new AudioFormat(Parameters.TIA_AUDIO_SAMPLE_RATE, 8, 1, true, false);

	private static final float MAX_MONO_CHANNEL_AMPLITUDE = Parameters.TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE;
	
}
