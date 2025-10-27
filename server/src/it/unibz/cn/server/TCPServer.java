package it.unibz.cn.server;

import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;

public class TCPServer {
	final static int SERVER_PORT = 7896;
	static List<Connection> allClients = new CopyOnWriteArrayList<>();

	public static void main(String args[]) {

		try (
			ServerSocket listenSocket = new ServerSocket(SERVER_PORT);
		){
			while (true) {
				Socket clientSocket = listenSocket.accept();
				
				@SuppressWarnings("unused")
				Connection c = new Connection(clientSocket);
			}
		} catch (IOException e) {
			System.out.println("Listen: " + e.getMessage());
		}
	}
}

class Connection extends Thread {

	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;

	public Connection(Socket aClientSocket) {
		try {
			this.clientSocket = aClientSocket;
			this.in = new DataInputStream(clientSocket.getInputStream());
			this.out = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.out.println("Connection: " + e.getMessage());
		}
	}

	public void run() {
		try { // an echo server
			String data = this.in.readUTF();
			this.out.writeUTF(data);
			this.out.flush();
		} catch (EOFException e) {
			System.out.println("EOF: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO:s a" + e.getMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				/* close failed */
			}
		}
	}
}