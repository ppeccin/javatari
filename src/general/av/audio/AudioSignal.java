// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.av.audio;

import javax.sound.sampled.AudioFormat;

public interface AudioSignal {

	public AudioFormat getAudioFormat();

	public void connectMonitor(AudioMonitor monitor);

}
