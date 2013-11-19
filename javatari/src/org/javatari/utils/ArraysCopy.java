// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

public final class ArraysCopy {

	public static int[][] copy2D(int[][] original) {
		int[][] copy = new int[original.length][];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = original[i].clone();
		}
		return copy;
	}

	public static void copyWithStride(int[] src, int srcPos, int dest[], int destPos, int length, int chunk, int stride) {
		int total = length;
		while(total > 0) {
			System.arraycopy(src, srcPos, dest, destPos, chunk);
			srcPos += stride;
			destPos += chunk;
			total -= chunk;
		}
	}

}
