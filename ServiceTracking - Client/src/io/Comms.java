package io;

import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import display.ClientDisplay;

public class Comms {

	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;
	private PropertyChangeSupport pcs;

	public Comms()
	{
		this.pcs = new PropertyChangeSupport(this);
	}

	public synchronized void connect(String address)
	{
		if(s!=null && !s.isClosed()) return;
		try {
			this.s = new Socket();
			this.s.connect(new InetSocketAddress(address, 4242), 5000);
			this.dis = new DataInputStream(s.getInputStream());
			this.dos = new DataOutputStream(s.getOutputStream());
			this.s.setSoTimeout(5000);
			this.writeUTF("Hello.  Service Tracker");
			this.writeInt(ClientDisplay.version);
			String response = this.readUTF();
			if(response.compareTo("bad version")==0)
			{
				JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
						"The server is running a different version than this client.\n"
								+ "Make sure you're running the most recent version.", 
								"Invalid version number", JOptionPane.ERROR_MESSAGE);
				this.close();
				System.exit(0);
			}
			else if(response.compareTo("bye")==0)
			{
				JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
						"The server rejected this connection.", 
						"Server rejected client", JOptionPane.ERROR_MESSAGE);
				this.close();
				System.exit(0);
			}
			else if(response.compareTo("ok")!=0)
			{
				JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
						"The server gave an invalid response.\n"
								+ "Check that you have the correct address\n"
								+ "and that there are no other servers running on "
								+ "port 4242 at this address.", 
								"Invalid response from server", JOptionPane.ERROR_MESSAGE);
				this.close();
				System.exit(0);
			}
		}
		catch(SocketTimeoutException e)
		{
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"Unable to reach the server.\n"
							+ "Please try again in a few minutes", 
							"Cannot contact server", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"Unable resolve this hostname.\n"
							+ "Please check that you have the right address", 
							"Cannot resolve hostname", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "open a connection to the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
	}

	public synchronized void connect()
	{
		this.connect("localhost");
	}

	public synchronized void writeUTF(String s)
	{
		if(this.s == null) this.connect();
		try {
			this.dos.writeUTF(s);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "communicate with the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
	}

	public synchronized String readUTF()
	{
		if(this.s == null) this.connect();
		try {
			return this.dis.readUTF();
		} 
		catch(SocketTimeoutException e)
		{
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"Unable to reach the server.\n"
							+ "Please try again in a few minutes", 
							"Cannot contact server", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "communicate with the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		return null;
	}

	public synchronized void writeInt(int i)
	{
		if(this.s == null) this.connect();
		try {
			this.dos.writeInt(i);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "communicate with the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
	}

	public synchronized int readInt()
	{
		if(this.s == null) this.connect();
		try {
			return this.dis.readInt();
		} 
		catch(SocketTimeoutException e)
		{
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"Unable to reach the server.\n"
							+ "Please try again in a few minutes", 
							"Cannot contact server", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "communicate with the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		return 0;
	}
	
	public synchronized long readLong()
	{
		if(this.s == null) this.connect();
		try {
			return this.dis.readLong();
		} 
		catch(SocketTimeoutException e)
		{
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"Unable to reach the server.\n"
							+ "Please try again in a few minutes", 
							"Cannot contact server", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
							+ "communicate with the server.\n"
							+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
			this.close();
			System.exit(0);
		}
		return 0L;
	}

	public void close()
	{
		if(this.s == null) return;
		try {
			this.s.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
					"An unknown error occurred while trying to "
					+ "communicate with the server.\n"
					+ "Please try again later", 
							"Unknown Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public PropertyChangeSupport getPCS()
	{
		return this.pcs;
	}

}
