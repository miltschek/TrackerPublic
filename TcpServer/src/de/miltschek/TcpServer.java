package de.miltschek;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.miltschek.tracker.BitUtility;

/**
 * Simple TCP server writing data to a file.
 * The data needs to be structured as:
 * - 4 bytes: a field denoting the size of the file in bytes (big endian integer)
 * - number of bytes as announced in the first field: the payload
 * The server responds with a single byte '5' after getting all data.
 * Single threaded (no parallel connections possible).
 */
public class TcpServer {
	private static boolean shouldRun = true;

	public static void main(String[] args) throws Exception {
		// get parameters (bind address and port)
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Address to bind to [0.0.0.0]: ");
		String address = br.readLine();
		
		System.out.print("Port to listen to [8080]: ");
		String portString = br.readLine();
		int port = portString.length() == 0 ? 8080 : Integer.parseInt(portString);
		
		// start a tcp server
		ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(address.length() == 0 ? "0.0.0.0" : address));
		
		// provide a dirty quit-method
		Thread closure = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String command;
					do {
						command = br.readLine();
					} while (!command.equals("quit"));
					
					shouldRun = false;
					server.close();
				} catch (Exception ex) {}
			}
		});
		
		closure.start();
		
		// main server's loop
		while (shouldRun) {
			System.out.println("Awaiting a client. Type 'quit' at any point of time to close the server.");
			Socket client = server.accept();
			System.out.println("Connection from " + client.getRemoteSocketAddress());
			InputStream is = client.getInputStream();
			
			// create a file
			String filename = "data_" + System.currentTimeMillis() + ".bin";
			FileOutputStream fos = new FileOutputStream(filename);
			System.out.println("Writing incoming data to " + filename);
			
			// transfer data to the file
			byte[] buffer = new byte[1024 * 1024];
			int read;
			int totalRead = 0;
			
			// read the file size
			read = is.read(buffer, 0, 4);
			if (read != 4) {
				System.err.println("Expected 4 bytes, but received only " + read + "; quitting...");
				return;
			}
			
			int fileSize = BitUtility.getInt(buffer, 0);
			
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
				totalRead += read;
				System.out.print(".");
				
				if (totalRead >= fileSize) {
					break;
				}
			}
			
			// inform the client, we have all data
			client.getOutputStream().write(5);
			client.close();
			
			System.out.println();
			System.out.println("Received " + totalRead + " Bytes.");
			
			if (totalRead != fileSize) {
				System.err.println("Something went wrong. We have got " + totalRead + " Bytes instead of " + fileSize + " Bytes that have been expected.");
			}
		}
		
		// close the server when out of the loop (it will be closed anyway already due to the form of the closure thread)
		server.close();
	}

}
