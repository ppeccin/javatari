// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils;

import java.security.AccessControlException;

public class Terminator {

	public static void terminate() {
		System.out.println("<<<<<<<<<<<<  EXIT   >>>>>>>>>>>>>");
		try {
			System.exit(0);
		} catch(AccessControlException ex) {
			// Ignore
		}
	}
	
}
