package net.trdlo.zelda.tiled.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class TestClient {

	public static void main(String[] args) throws IOException {
		Socket socket = new Socket("127.0.0.1", 6789);
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Writer writer = new OutputStreamWriter(socket.getOutputStream());

		String msg = "getmap";//"Hi there, it's " + (System.nanoTime() / 1000L);
		writer.write(msg + "\n");
		writer.flush();
		while (true) {
			//System.out.println("Wrote: " + msg);
			System.out.println(reader.readLine());
		}
	}
}
