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

	public static double floorToMultipleOf(double number, double divisor) {
		return divisor * Math.floor(number / divisor);
	}

	public static double ceilToMultipleOf(double number, double divisor) {
		return divisor * Math.ceil(number / divisor);
	}

	public static double roundToMultipleOf(double number, double divisor) {
		return divisor * Math.round(number / divisor);
	}

	public static int radToDeg(double rad) {
		return (int) (180 * rad / Math.PI);
	}

	public static double degToRad(double deg) {
		return Math.PI * deg / 180;
	}

	/**
	 * Calculates an angle in range (-PI ; PI>
	 *
	 * @param rad	input angle, works only with numbers (not NANs and INFs)
	 * @return	angle in range (-PI ; PI>
	 */
	public static double normalizeAngle(double rad) {
		while (rad > Math.PI) {
			rad -= 2 * Math.PI;
		}
		while (rad <= -Math.PI) {
			rad += 2 * Math.PI;
		}
		return rad;
	}
}
