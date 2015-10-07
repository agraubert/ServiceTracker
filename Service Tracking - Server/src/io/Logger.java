package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.ServerMain;

public class Logger implements Runnable{
	private ArrayList<String> messageQueue;
	private PrintWriter writer;
	private boolean logging = true;
	public boolean closeOK = false;
	private ArrayList<String> errors;
	private ArrayList<String> warnings;

	public Logger(String path)
	{
		this.messageQueue = new ArrayList<String>();
		this.errors = new ArrayList<String>();
		this.warnings = new ArrayList<String>();
		try {
			this.writer = new PrintWriter(new FileWriter(path));
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.writer.println("---LOG STARTED---");
		this.writer.println("WULIB Service Tracking Server (Version "+ServerMain.version+") made by Aaron Graubert");
		this.writer.flush();
		while(this.logging)
		{
			while(this.messageQueue.size() == 0 )
			{
				try {
					synchronized(this)
					{
						this.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			synchronized(this)
			{
				String message = this.messageQueue.remove(0);
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
				String date = sdf.format(d);
				String formatted = "["+date+"] "+message;
				this.writer.println(formatted);
				this.writer.flush();
			}
		}
		synchronized(this)
		{
			while(this.messageQueue.size() > 0)
			{
				String message = this.messageQueue.remove(0);
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
				String date = sdf.format(d);
				String formatted = "["+date+"] "+message;
				this.writer.println(formatted);
				this.writer.flush();
			}
			if(this.warnings.size() > 0)
			{
				this.writer.println("---Warning summary---");
				for(int i = 0; i < this.warnings.size(); ++i) this.writer.println(this.warnings.get(i));
				this.writer.println("It is recommended that you review each warning to avoid further problems");
			}
			if(this.errors.size() > 0)
			{
				this.writer.println("---Error summary---");
				for(int i = 0; i < this.errors.size(); ++i) this.writer.println(this.errors.get(i));
				this.writer.println("Please resolve these errors before starting the server again");
			}
			this.writer.println("---LOG CLOSED---");
			this.writer.flush();
			this.writer.close();
			this.closeOK = true;
		}

	}
	
	public synchronized void log(String message, String sender, String line)
	{
		if(!this.logging) return;
		String formatted = "["+line+"] ["+sender+"] : "+message;
		this.messageQueue.add(formatted);
		if(line.compareTo("ERROR")==0) this.errors.add(formatted);
		else if(line.compareTo("WARN")==0) this.warnings.add(formatted);
		this.notifyAll();
	}
	
	public synchronized void log(String message, String sender)
	{
		this.log(message, sender, "INFO");
	}

	public synchronized void log(String message)
	{
		this.log(message, "ANONYMOUS", "INFO");
	}

	public synchronized void shutdown()
	{
		if(!logging) return;
		this.log("Logging queue closed.  Log shutdown initiated.", "LOGGER");
		this.logging = false;
		//String formatted = "[LOGGER] : "+"Logging queue closed.  Log shutdown initiated.";
		//this.messageQueue.add(formatted);
		//this.notifyAll();
	}

	public void waitForClose()
	{
		while(!this.closeOK)
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String Trace(Throwable t)
	{
		String out =t.toString()+" @ ";
		StackTraceElement[] ste = t.getStackTrace();
		for(int i = 0; i<ste.length; ++i)
		{
			out+=ste[i];
			if(i<ste.length-1) out+=" --> ";
		}
		return out;
	}

}
