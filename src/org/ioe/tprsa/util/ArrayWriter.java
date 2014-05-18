package org.ioe.tprsa.util;

/**
 * saves the array to file or console ...... supports various data types
 */
public final class ArrayWriter {
	/**
	 * display @param array 's content to console
	 * @param array input array
	 */
	public static void printDoubleArrayToConsole(double[] array) {
        for (double anArray : array) {
            System.out.println(anArray);
        }
	}

	/**
	 * display @param array 's content to console
	 * @param array input array
	 */
	public static void print2DTabbedDoubleArrayToConsole(double[][] array) {
        for (double[] anArray : array) {
            for (double anAnArray : anArray) {
                System.out.print(anAnArray + "\t");
            }
            System.out.println();
        }
	}

	/**
	 * display @param array 's content to console
	 * @param array input array
	 */
	public static void printStringArrayToConsole(String[] array) {
        for (String anArray : array) {
            System.out.println(anArray);
        }
	}
}