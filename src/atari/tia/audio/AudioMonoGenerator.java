// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

import parameters.Parameters;

public final class AudioMonoGenerator extends AudioGenerator {

	@Override
	protected void generateNextSamples(int quant) {
		for (int i = 0; i < quant; i++) {
			float mixedSample = channel0.nextSample() * MAX_MONO_CHANNEL_AMPLITUDE + channel1.nextSample() * MAX_MONO_CHANNEL_AMPLITUDE;
			samples[generatedSamples++] = ((byte) (mixedSample * (MAX_AMPLITUDE * 127)));
		}
	}

	private static final float MAX_AMPLITUDE = Parameters.TIA_AUDIO_MAX_AMPLITUDE;
	private static final float MAX_MONO_CHANNEL_AMPLITUDE = Parameters.TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE;
	
}
