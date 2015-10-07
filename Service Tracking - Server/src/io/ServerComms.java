package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerComms {
	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Logger log;
	private String name;
	
	public ServerComms(Socket s, Logger l, String parentName)
	{
		this.log = l;
		this.name = parentName+"->Comms";
		try {
			s.setSoTimeout(0);
			this.dis = new DataInputStream(s.getInputStream());
			this.dos = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while opening streams. Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
	}
	
	public synchronized void writeUTF(String s)
	{
		try {
			this.dos.writeUTF(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while writing string \""+s+"\". Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
	}
	
	public synchronized String readUTF()
	{
		try {
			return this.dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while reading string. Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
		return null;
	}
	
	public synchronized void writeInt(int i)
	{
		try {
			this.dos.writeInt(i);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while writing int \""+i+"\". Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
	}
	
	public synchronized int readInt()
	{
		try {
			return this.dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while reading int. Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
		return 0;
	}
	
	public synchronized void writeLong(long l)
	{
		try {
			this.dos.writeLong(l);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while writing long \""+l+"\". Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
	}
	
	public void close()
	{
		if(s == null || s.isClosed()) return;
		try {
			this.s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while closing socket. Message reads: "+Logger.Trace(e), this.name, "ERROR");
		}
	}

}
