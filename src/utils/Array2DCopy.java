package utils;

public class Array2DCopy {

	public static int[][] copy(int[][] original) {
		int[][] copy = new int[original.length][];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = original[i].clone();
		}
		return copy;
	}

}
