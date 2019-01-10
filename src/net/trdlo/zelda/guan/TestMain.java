package net.trdlo.zelda.guan;

import java.util.regex.Matcher;

/**
 *
 * @author bayer
 */
public class TestMain {

	public static void main(String[] args) {
		Matcher m = World.PAT_IMAGE.matcher("img images/guan_bg.jpg");
		if (m.matches()) {
			System.out.println(m.group(1));
		}
	}
}
