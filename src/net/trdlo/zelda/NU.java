package net.trdlo.zelda;

/**
 * NU - Nano Utils class
 */
public final class NU {

	public static double sqr(double x) {
		return x * x;
	}

	public static boolean inRange(double low, double val, double high) {
		assert low < high;
		return (low < val) && (val < high);
	}

	public static boolean inRange2(double border1, double val, double border2) {
		return inRange(Math.min(border1, border2), val, Math.max(border1, border2));
	}

	public static double ensureRange(double low, double val, double high) {
		assert low < high;
		return (high > val) ? ((low < val) ? val : low) : high;
	}

	public static double ensureRange2(double border1, double val, double border2) {
		return ensureRange(Math.min(border1, border2), val, Math.max(border1, border2));
	}

}
