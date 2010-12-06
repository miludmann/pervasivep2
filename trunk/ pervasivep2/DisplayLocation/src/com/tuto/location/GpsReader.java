package com.tuto.location;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class GpsReader {
	
	public static int sendDataToServer(String data)
	{
	      String hostname = "camel04.cs.au.dk"; //camel04 : 130.225.16.123
	      int port = 15340;
//	      hostname = "84.238.67.223"; //Home PC
	      Socket socket = new Socket();
	      SocketAddress addr = new InetSocketAddress(hostname, port);
	      try {      	
				socket.connect(addr);				   	
				OutputStream os = socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeUTF(data);
				dos.close();
				socket.close();
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
				return 1;
			}
	}
}
