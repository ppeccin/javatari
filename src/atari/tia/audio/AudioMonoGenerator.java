// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

import parameters.Parameters;

public final class AudioMonoGenerator extends AudioGenerator {

	@Override
	protected void generateNextSamples(int quant) {
		for (int i = 0; i < quant; i++) {
			float mixedSample = channel0.nextSample() - channel1.nextSample();

			// Add a little damper effect to round the edges of the square wave
			if (mixedSample != lastSample) {
				mixedSample = (mixedSample * 9 + lastSample) / 10;
				lastSample = mixedSample;
			}
			
			samples[generatedSamples++] = (byte) (mixedSample * MAX_AMPLITUDE * 127);
		}
	}

	private float lastSample;
	
	private static final float MAX_AMPLITUDE = Parameters.TIA_AUDIO_MAX_AMPLITUDE;
	
}
