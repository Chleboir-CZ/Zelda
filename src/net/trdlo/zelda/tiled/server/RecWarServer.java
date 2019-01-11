package net.trdlo.zelda.tiled.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Client implements Runnable {

	private final BufferedReader reader;
	private final Writer writer;
	private final Queue<String> queuedLines = new ConcurrentLinkedQueue<>();

	private volatile boolean terminated = false;
	private Exception terminationException;

	public Client(BufferedReader reader, Writer writer) {
		this.reader = reader;
		this.writer = writer;
	}

	public String readLine() {
		return queuedLines.poll();
	}

	public void writeLine(String line) {
		if (terminated) {
			return;
		}
		try {
			writer.write(line);
			writer.write("\n");
			writer.flush();
		} catch (IOException ex) {
			terminated = true;
			terminationException = ex;
		}
	}

	public boolean isTerminated() {
		return terminated;
	}

	public Exception getTerminationException() {
		return terminationException;
	}

	@Override
	public void run() {
		while (!terminated) {
			String line;
			try {
				line = reader.readLine();
				queuedLines.add(line);
			} catch (IOException ex) {
				terminated = true;
				terminationException = ex;
			}
		}
	}
}

public class RecWarServer implements Runnable {

	private static volatile boolean terminate = false;

	List<Client> clients = new CopyOnWriteArrayList<>();

	private static final File MAP_FILE = new File("maps/d.txt");
	private String mapContent;
	public static final String MAP_END_STR = "<<END>>";

	private void loadMapFile() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new FileReader(MAP_FILE));
		String line;
		boolean first = true;
		while((line = reader.readLine()) != null) {
			if (!line.isEmpty()) {
				if (first) {
					first = false;					
				} else {
					sb.append("\n");
				}
				sb.append(line);
			}
		}
		mapContent = sb.toString();
		System.out.println(mapContent);
	}
	
	private void sendMap(Client c) {
		c.writeLine(mapContent);
		c.writeLine(MAP_END_STR);
	}
	
	
	public static final Pattern PAT_GETMAP = Pattern.compile("^getmap\\z");

	@Override
	public void run() {
		try {
			loadMapFile();
		} catch (IOException ex) {
			System.err.println("Could not load map: " + ex.getMessage());
			terminate = true;
			return;
		}
		
		int replyCounter = 0;
		int lastClientsSize = -1;
		while (true) {
			int clientSize = clients.size();
			if (clientSize != lastClientsSize) {
				lastClientsSize = clientSize;
				System.out.println("Number of clients: " + clientSize);
			}
			for (Client c : clients) {
				if (c.isTerminated()) {
					System.out.println(c.toString() + " is terminated, reason: " + c.getTerminationException().getMessage());
					clients.remove(c);
				} else {
					String command;
					while ((command = c.readLine()) != null) {
						Matcher m;
						if (PAT_GETMAP.matcher(command).matches()) {
							sendMap(c);
						} else if ((m = PAT_GETMAP.matcher(command)).matches()) {
							
						} else {
							
						}

						String reply = "Reply " + replyCounter++;
						System.out.println(c.toString() + " wrote: " + command + ", replying: " + reply);
						c.writeLine(reply);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		RecWarServer server = new RecWarServer();
		new Thread(server).start();

		ServerSocket listenSocket = new ServerSocket(6789);
		while (!terminate) {
			Socket socket = listenSocket.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Writer writer = new OutputStreamWriter(socket.getOutputStream());

			Client client = new Client(reader, writer);
			new Thread(client).start();
			server.clients.add(client);
		}
	}
}
