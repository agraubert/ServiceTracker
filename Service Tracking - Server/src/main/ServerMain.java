package main;

import io.FileManager;
import io.Logger;

import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;


public class ServerMain implements Runnable{
	public static final int version = 2;
	public static final boolean display = true;
	private int shutdownMode = 0;
	private int active = 0;
	//private ServerListener sl;
	private ArrayList<Thread> threads;
	private ArrayList<Integer> openSlots;
	private ServerSocket ss;
	private PropertyChangeSupport pcs;
	private FileManager files;
	private Logger log;

	public static void main(String[] args) {
		new ServerMain().run();
	}

	public ServerMain()
	{
		this.openSlots = new ArrayList<Integer>();
		this.threads = new ArrayList<Thread>();
		this.pcs = new PropertyChangeSupport(this);
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy@H-mm");
		String date = sdf.format(d);
		//log directory checking
		if(!new File("./logs").exists()) new File("./logs").mkdir();
		this.log = new Logger("./logs/log_"+date+".txt");
		try {
			this.ss = new ServerSocket(4242);
			this.ss.setSoTimeout(5000);
		}
		catch (BindException e)
		{
			JOptionPane.showMessageDialog(null, 
					"Unable to bind to the port.\n"
							+ "Please check that no other servers are running on port 4242", 
							"Bind Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void setShutdown(int i)
	{
		this.shutdownMode = i;
	}

	public synchronized void checkIn()
	{
		this.active += 1;
		this.pcs.firePropertyChange("clientCount", null, ""+this.active);
	}

	public synchronized void checkOut(int i)
	{
		this.openSlots.add(i);
		//this.log.log("Slot "+i+" now open", "DEBUG->Server main");
		this.active -= 1;
		this.pcs.firePropertyChange("clientCount", null, ""+this.active);
		if(this.active == 0 && this.files.getDatabaseSize() > 25)
		{
			this.log.log("Activating hardware inventory cleanup", "Server main->Thread "+i);
			this.files.dumpHardware();
		}
	}

	public FileManager getFM()
	{
		return this.files;
	}

	public PropertyChangeSupport getPCS()
	{
		return this.pcs;
	}

	@Override
	public void run() {
		Thread logThread = new Thread(this.log);
		logThread.start();
		this.log.log("Server startup initiated", "Server main");
		this.files = new FileManager("./", this.log);
		if(this.files.rerun)
		{
			this.log.log("Restarting...", "Server main");
			this.log.shutdown();
			try {
				logThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.files.rerun = false;
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy@H-mm");
			String date = sdf.format(d);
			this.log = new Logger("./logs/log_"+date+"_restart.txt");
			this.run();
			return;
		}
		if(ServerMain.display) 
		{
			new ServerDisplay(this).setVisible(true);
			this.log.log("Server display active", "Server main");
		}
		this.log.log("*****Startup complete.  Server is now live*****", "Server main");
		while(this.shutdownMode == 0)
		{
			try {
				Socket s = this.ss.accept();
				synchronized(this)
				{
					int slot = 0;
					if(this.openSlots.isEmpty())
					{
						//slot = this.threads.size();
						slot = -1;
						//this.log.log("All slots full.  Allocating slot #"+slot, "DEBUG->Server main");
						this.log.log("Allocating new connection slot", "Server main");
					}
					else slot = this.openSlots.remove(0);
					ServerThread st = new ServerThread(s, this, this.files, this.log, 
							(slot == -1)?this.threads.size():slot);
					Thread t = new Thread(st);
					t.setDaemon(true);
					//this.threads.add(slot, t);
					if(slot == -1)
					{
						this.threads.add(t);
					}
					else this.threads.set(slot, t);
					t.start();
					//this.threads.get(this.threads.size()-1).setDaemon(true);
					//this.threads.get(this.threads.size()-1).start();
				}
			} 
			catch (SocketTimeoutException e)
			{
				
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.log.log("IO error while listening for clients.  Message reads: "+Logger.Trace(e), "Server main", "ERROR");
			}
		}
		//System.out.println("Main thread began shutdown");
		this.log.log("*****Shutdown initiated with mode: "+this.shutdownMode+"*****", "Server main");
		try {
			this.ss.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.log.log("IO error while closing port.  Message reads: "+Logger.Trace(e1), "Server main", "ERROR");
		}

		if(this.shutdownMode == 1 && this.active > 0) //soft shutdown. Stop accepting clients, and give 2 minutes to all active clients
		{
			long endtime = 120000l + System.currentTimeMillis();
			for(Thread t : this.threads)
			{
				long remainder = endtime - System.currentTimeMillis();
				if(remainder > 0)
					try {
						t.join(remainder);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			this.log.log("Finished waiting for threads", "Server main");

		}
		this.files.shutdown();
		this.log.log("All components shutdown.  Ready to close log", "Server main");
		this.log.shutdown();
		try {
			logThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pcs.firePropertyChange("shutdown", null, null);

	}

	public void waitForIdle(long maxmillis)
	{
		while(this.active>0)
		{
			if(maxmillis == 0) return;
			long amt = 1000l;
			if(maxmillis > 0 && amt > maxmillis) amt = maxmillis;
			try {
				Thread.sleep(amt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(maxmillis > 0) maxmillis -= amt;
		}
	}

}
