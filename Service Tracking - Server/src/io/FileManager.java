package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

import main.ServerMain;
import core.Computer;
import core.Update;

public class FileManager {
	private HashMap<String, String> fileIndex;
	private HashMap<String, String> invalidIndex;
	private HashMap<String, Computer> database;
	private HashMap<String, Long> timeDB;
	public boolean rerun = false;
	public static final boolean mailOK = false;
	private String imagesPath;
	private String indexPath; //a / style filepath to the database location
	private Logger log;

	public FileManager(String path, Logger l)
	{
		boolean foundImages = false;
		this.indexPath = path;
		this.fileIndex = new HashMap<String, String>();
		this.invalidIndex = new HashMap<String, String>();
		this.database = new HashMap<String, Computer>();
		this.timeDB = new HashMap<String, Long>();
		this.log = l;
		this.log.log("Starting up", "File manager");
		if(mailOK)
		{
			try {
				String serverName = InetAddress.getLocalHost().getHostName();
				this.sendMail("Server Starting Up", "WULIB Service Tracker Server v"+ServerMain.version+" starting up on"+serverName+"...");
			} catch (UnknownHostException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.indexPath+"/index.txt"));
			String line = reader.readLine();
			while(line!=null)
			{
				StringTokenizer splitter = new StringTokenizer(line);
				String key = splitter.nextToken();
				if(key.compareTo("images")==0)
				{
					//String extra = (this.indexPath.charAt(this.indexPath.length()-1) == '/') ? "" : "/";
					this.imagesPath = this.indexPath+splitter.nextToken();
					foundImages = true;
				}
				else
				{
					String file = splitter.nextToken();
					while(splitter.hasMoreTokens())
					{
						key +=" "+file;
						file = splitter.nextToken();
					}
					this.fileIndex.put(key, file);
					this.loadDate(key);
				}
				line = reader.readLine();
			}
			reader.close();
			if(!foundImages)
			{
				this.log.log("Unable to find images record in index", "File manager", "WARN");
				JOptionPane.showMessageDialog(null,
						"The server file index did not contain an entry for images.\n"
								+ "The images file keeps a record of all the registered images.\n"
								+ "The file and its entry in the index are required for the server.\n"
								+ "The server will attempt to fix this problem now by recreating the needed\n"
								+ "entry in the index.  If the images file is also missing, it will be\n"
								+ "automatically generated later on.",
								"Images not found", JOptionPane.ERROR_MESSAGE);
				this.imagesPath = this.indexPath+"/index.txt";
			}
		} catch (FileNotFoundException e) {
			int choice = JOptionPane.showConfirmDialog(null,
					"The server could not find the index file for its database\n"
							+ "Press OK if you would like the server to recreate the necessary files\n"
							+ "or press Cancel if you would like close the server to create them manually.",
							"Index not found", JOptionPane.OK_CANCEL_OPTION);
			if(choice == 0)
			{
				this.log.log("Unable to find index file.  Attempting automatic repairs", "File manager", "WARN");
				this.indexPath = "./";
				this.imagesPath = "./images.txt";
				try {
					PrintWriter writer = new PrintWriter(new FileWriter("./index.txt"));
					writer.println("images images.txt");
					writer.flush();
					writer.close();

					writer = new PrintWriter(new FileWriter("./images.txt"));
					writer.println("default image");
					writer.flush();
					writer.close();
					rerun = true;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					this.log.log("IO error while attempting to recreate critical database files. Message reads: "+Logger.Trace(e1), "File manager", "ERROR");
				}
			}
			else
			{
				this.log.log("Unable to find index file.  User opted for manual repair.  Shutting down", "File manager", "ERROR");
				this.log.shutdown();
				JOptionPane.showMessageDialog(null, 
						"You must create two files for the server to run properly\n"
						//+ "Place both files in the same folder as the server\n"
						+ "1)images.txt : Keeps a list of the server's images.  Image names are separated on new lines\n"
						+ "2)index.txt : Keeps an index of the database.  Each line contains a new entry\n"
						+ "   Must be placed in the same folder as the server\n"
						+ "   Must contain the following entry: images [path]\n"
						+ "   Substitue path with a valid relative filepath to the images.txt file.\n"
						+ "   If images.txt is in the same folder as the index, just type \"images.txt\" (without quotes or brackets)", 
						"Notice", JOptionPane.INFORMATION_MESSAGE);
				this.log.waitForClose();
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
			this.log.log("Encountered an unexpected IO error while loading database. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
			this.log.shutdown();
			JOptionPane.showMessageDialog(null, 
					"The server encountered an unknown error while loading data from the database.\n"
							+ "Please check that no other programs have the database files open, then try again.", 
							"Error loading database", JOptionPane.ERROR_MESSAGE);
			this.log.waitForClose();
			System.exit(0);
		}
		this.log.log("Startup complete, inventory loaded", "File manager");
	}

	public synchronized Computer fetchComputer(String name)
	{
		if(this.database.containsKey(name)) return this.database.get(name);
		if(!this.fileIndex.containsKey(name)) return null;
		try {
			//boolean autodate = false;
			BufferedReader reader = new BufferedReader(new FileReader(this.indexPath+"/"+this.fileIndex.get(name)));
			String line = reader.readLine();
			Computer output = new Computer(line, reader.readLine());
			//Date processing
			line = reader.readLine();
			if(line.compareTo("VER -17") == 0) //check for version stamp
			{
				line = reader.readLine(); //Serial - Model
				StringTokenizer splitter = new StringTokenizer(line);
				String serial = splitter.nextToken();
				String model = splitter.nextToken();
				while(splitter.hasMoreTokens()) model +=" "+splitter.nextToken();
				String user = reader.readLine();
				String loc = reader.readLine();
				output.setSerial(serial);
				output.setModel(model);
				output.setUser(user);
				output.setLocation(loc);
				line = reader.readLine(); //stamp - drive
			}
			else this.log.log("File for computer \""+name+"\" not in v-17 format (this will be fixed automatically when the computer is offloaded).  Using default data", "File manager", "WARN");
			StringTokenizer splitter = new StringTokenizer(line); //tokenize line
			try{
				long timeStamp = Long.parseLong(splitter.nextToken());
				output.setModified(timeStamp);

				if(splitter.hasMoreTokens()) //has drive
				{
					String dr = splitter.nextToken();
					while(splitter.hasMoreTokens())
					{
						dr+=" "+splitter.nextToken();
					}
					output.setDrive(dr);

				}
				else this.log.log("File for computer \""+name+"\" not in v-16 format (this will be fixed automatically when the computer is offloaded).  Using default drive", "File manager", "WARN");

				line = reader.readLine(); //now load tech data
			}
			catch(NumberFormatException e)
			{
				//autodate = true;
				this.log.log("File for computer \""+name+"\" not in v-15 format (this will be fixed automatically when the computer is offloaded).  Using timeDB timestamp.", "File manager");
				output.setModified(this.timeDB.get(name));
			}

			while(line!=null)
			{
				String tech = line;
				String date = reader.readLine();
				/*if(autodate)
				{
					this.log.log("Failed to load the timestamp from computer \""+name+"\".  Timestamps are kept since v-15", "File manager", "WARN");
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
					try {
						long l = sdf.parse(date).getTime();
						output.setModified(l);
						this.log.log("Successfully parsed timestamp from date of last patch for computer \""+output.getName()+"\"", "File manager");
					} catch (ParseException e) {
						this.log.log("Failed to parse a timestamp for computer \""+name+"\".  Auto-generating now", "File manager", "WARN");
						output.newChange();
					}
					autodate = false;
				}*/
				Update u = new Update(tech, date, reader.readLine());
				output.addPatch(u);
				line=reader.readLine();
			}
			/*if(autodate)
			{
				this.log.log("Failed to parse a timestamp for computer \""+name+"\".  Auto-generating now", "File manager", "WARN");
				output.newChange();
			}*/
			reader.close();
			this.database.put(name, output);
			this.timeDB.remove(name);
			this.log.log("Computer \""+name+"\" now loaded by request", "File manager");
			return output;
		} catch (FileNotFoundException e) {
			this.invalidIndex.put(name, this.fileIndex.get(name));
			this.fileIndex.remove(name);
			this.log.log("Invalid index entry on computer \""+name+"\". This will be temporarily removed from the index", "File manager", "WARN");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.invalidIndex.put(name, this.fileIndex.get(name));
			this.fileIndex.remove(name);
			this.log.log("IO error while loading computer \""+name+"\". This will be temporarily removed from the index. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}
		return null;
	}

	public Set<String> getHardwareInventory()
	{
		return this.fileIndex.keySet();
	}

	public synchronized Set<String> getImagesInventory()
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.imagesPath));
			Set<String> output = new HashSet<String>();
			String line = reader.readLine();
			while(line != null)
			{
				output.add(line);
				line = reader.readLine();
			}
			reader.close();
			return output;
		} catch (FileNotFoundException e) {
			this.log.log("Unable to open the images inventory at "+this.imagesPath, "File manager", "WARN");
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(this.imagesPath));
				writer.println("default image");
				writer.flush();
				writer.close();
				this.log.log("Recreated images inventory with default image", "File manager", "WARN");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				this.log.log("IO error while recreating images inventory. Message reads: "+Logger.Trace(e1), "File manager", "ERROR");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while loading images inventory. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}
		return null;
	}

	public synchronized void trackHardware(Computer c)
	{
		if(this.database.containsKey(c.getName())) return;
		if(this.fileIndex.containsKey(c.getName())) return;
		if(this.invalidIndex.containsKey(c.getName())) return;
		this.database.put(c.getName(), c);
		this.fileIndex.put(c.getName(), this.makeCanonical(c.getName()));
		this.log.log("Starting a sub thread to track new hardware: "+c.getName(), "File manager");
		if(mailOK)
		{
			String messageText = "A new machine has been added\n";
			messageText +="Name: "+c.getName();
			messageText +="\nAdded by: ";
			if(c.getPatches().size()>0)
			{
				messageText+=c.getPatches().get(0).getTech();
			}
			this.sendMail("New machine added", messageText);
		}
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					PrintWriter writer = new PrintWriter(new FileWriter(fileIndex.get(c.getName())));
					writer.println(c.getName());
					writer.println(c.getImage());
					writer.println("VER -17");
					writer.println(c.getSerial()+" "+c.getModel());
					writer.println(c.getUser());
					writer.println(c.getLocation());
					String timeAndDrive = ""+c.getModified()+" "+c.getDrive();
					writer.println(timeAndDrive);
					ArrayList<Update> p = c.getPatches();
					for(int i = 0; i<p.size(); ++i)
					{
						Update u = p.get(i);
						writer.println(u.getTech());
						writer.println(u.getDate());
						writer.println(u.getNotes());
					}
					writer.flush();
					writer.close();
					log.log("New computer added: "+c.getName(), "File manager->Sub thread");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.log("IO error while adding new hardware \""+c.getName()+"\". Message reads: "+Logger.Trace(e), "File manager->Sub thread", "ERROR");
				}

			}

		}).start();
	}

	public synchronized void untrackHardware(String s)
	{
		if(this.database.containsKey(s)) this.database.remove(s);
		if(this.fileIndex.containsKey(s)) 
		{
			this.fileIndex.remove(s);
			this.log.log("Computer \""+s+"\" removed from inventory.", "File manager");
			if(mailOK)
			{
				String messageText = "A machine has been removed\n";
				messageText +="Name: "+s;
				this.sendMail("Machine removed", messageText);
			}
		}
	}

	public synchronized void addImage(String s)
	{
		try {
			Set<String> output = this.getImagesInventory();
			if(!output.contains(s))
			{
				output.add(s);
				PrintWriter writer = new PrintWriter(new FileWriter(this.imagesPath));
				for(String str : output)
				{
					writer.println(str);
				}
				writer.close();
				this.log.log("New image \""+s+"\" added to image inventory", "File manager");
			}

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while adding image to images inventory. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}
	}

	public synchronized void removeImage(String s)
	{
		Set<String> images = this.getImagesInventory();
		if(!images.contains(s)) return;
		this.log.log("Removing image \""+s+"\" from inventory", "File manager");
		images.remove(s);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(this.imagesPath));
			for(String str : images)
			{
				writer.println(str);
			}
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while removing image \""+s+"\" from images inventory. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}
	}

	public String makeCanonical(String temp) {
		temp = temp.toLowerCase();
		temp = temp.replaceAll(" ", "_");
		temp = temp.replaceAll("\\*", "");
		temp = temp.replaceAll(",", "");
		temp = temp.replaceAll("\\.", "-");
		temp = temp.replaceAll("\\\\", "");
		temp = temp.replaceAll("\\?", "");
		temp = temp.replaceAll("/", "");
		temp = temp.replaceAll(":", "");
		temp = temp.replaceAll("\\\"", "");
		temp = temp.replaceAll("<", "");
		temp = temp.replaceAll(">", "");
		temp = temp.replaceAll("|", "");
		return temp+".txt";
	}

	public boolean exists(String name)
	{
		return this.fileIndex.values().contains(this.makeCanonical(name)) ||
				this.fileIndex.keySet().contains(name)/* ||
				this.invalidIndex.values().contains(this.makeCanonical(name)) ||
				this.invalidIndex.keySet().contains(name)*/;
	}

	public synchronized void dumpHardware()
	{
		if(this.database.isEmpty())
		{
			this.log.log("Database empty.  Offload processs skipped.", "File manager");
			return;
		}
		this.log.log("Preparing to offload all hardware from memory", "File manager");
		for(String s : this.database.keySet())
		{
			//System.out.println(s + " loaded in database");
			//System.out.println(s + " in fileIndex: "+this.fileIndex.get(s));
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(this.fileIndex.get(s)));
				Computer c = this.database.get(s);
				this.timeDB.put(s, c.getModified());
				writer.println(c.getName());
				writer.println(c.getImage());
				writer.println("VER -17");
				writer.println(c.getSerial()+" "+c.getModel());
				writer.println(c.getUser());
				writer.println(c.getLocation());
				String timeAndDrive = ""+c.getModified()+" "+c.getDrive();
				writer.println(timeAndDrive);
				ArrayList<Update> p = c.getPatches();
				for(int i = 0; i<p.size(); ++i)
				{
					Update u = p.get(i);
					writer.println(u.getTech());
					writer.println(u.getDate());
					writer.println(u.getNotes());
				}
				writer.flush();
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.log.log("IO error while updating file for computer \""+s+"\". Message reads: "+Logger.Trace(e), "File manager", "ERROR");
			}
		}
		int size = this.database.size();
		this.database.clear();
		this.log.log("Offloaded "+size+" computers from memory", "File manager");
	}

	public synchronized void shutdown()
	{
		this.log.log("Shutdown initiated", "File manager");
		if(this.invalidIndex.size() > 0)
		{
			String all = "";
			for(String s: this.invalidIndex.keySet()) all+=s+", ";
			this.log.log("The following file paths were invalid: "+all, "File manager", "WARN");
			this.log.log("Please check their index entries, as they will be availiable again when the server next starts", "File manager", "WARN");
			this.fileIndex.putAll(this.invalidIndex);
		}
		/*for(String s : this.database.keySet())
		{
			//System.out.println(s + " loaded in database");
			//System.out.println(s + " in fileIndex: "+this.fileIndex.get(s));
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(this.fileIndex.get(s)));
				Computer c = this.database.get(s);
				writer.println(c.getName());
				writer.println(c.getImage());
				ArrayList<Update> p = c.getPatches();
				for(int i = 0; i<p.size(); ++i)
				{
					Update u = p.get(i);
					writer.println(u.getTech());
					writer.println(u.getDate());
					writer.println(u.getNotes());
				}
				writer.flush();
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.log.log("IO error while updating file for computer \""+s+"\". Message reads: "+e, "File manager", "ERROR");
			}
		}*/
		this.dumpHardware();
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(this.indexPath+"/index.txt"));
			for(String s : this.fileIndex.keySet())
			{
				writer.println(s+" "+this.fileIndex.get(s));
			}
			//System.out.println(this.imagesPath);
			String tmp = this.imagesPath.replaceFirst("\\./", "");
			//tmp = tmp.replaceFirst("/", "");
			//tmp = tmp.replaceFirst("/", "");
			//System.out.println(tmp);
			writer.println("images "+tmp);
			//writer.println("images images.txt");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.log.log("IO error while saving file index. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}
		if(mailOK)
		{
			this.sendMail("Server shutting down", "The Service Tracker Server has been shut down");
		}
		this.log.log("Shutdown complete.", "File manager");

	}

	public synchronized int getDatabaseSize()
	{
		return this.database.size();
	}

	public synchronized long fetchDate(String name)
	{
		if(!this.exists(name)) return -1;
		if(this.database.containsKey(name)) return this.database.get(name).getModified();
		return this.timeDB.get(name);
	}

	private void loadDate(String name)
	{
		if(!this.exists(name)) return;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.indexPath+"/"+this.fileIndex.get(name)));
			String line = reader.readLine(); //name
			reader.readLine(); //image
			//Date processing
			line = reader.readLine(); //timestamp, last tech name, or null
			if(line.compareTo("VER -17")==0)
			{
				reader.readLine();//serial - model
				reader.readLine();//user
				reader.readLine();//Loc
				line = reader.readLine();//time - drive
			}
			StringTokenizer splitter = new StringTokenizer(line); //tokenize line
			try{
				long timeStamp = Long.parseLong(splitter.nextToken()); //works if timestamp
				this.timeDB.put(name, timeStamp);
				reader.close();
				return;
			}
			catch(NumberFormatException e) //if line was not timestamp
			{
				//pass
			}
			this.log.log("No timestamp for computer \""+name+"\".  Timestamps are kept since v-15.  Will attempt to parse from patch notes", "File manager", "WARN");
			if(line!=null) //if line is last tech name
			{
				String date = reader.readLine(); //last update date
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
				try {
					long l = sdf.parse(date).getTime();
					this.timeDB.put(name, l); //good parse
					this.log.log("Successfully parsed timestamp from date of last patch for computer \""+name+"\"", "File manager");
					reader.close();
					return;
				} catch (ParseException e) {
					//bad parse
				}
			}
			//line is null or failed to parse
			this.log.log("Failed to parse a timestamp for computer \""+name+"\".  Auto-generating now", "File manager", "WARN");
			this.timeDB.put(name, new Date().getTime());
			reader.close();
			return;
		} catch (FileNotFoundException e) {
			this.invalidIndex.put(name, this.fileIndex.get(name));
			this.fileIndex.remove(name);
			this.log.log("Invalid index entry on computer \""+name+"\". This will be temporarily removed from the index", "File manager", "WARN");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.invalidIndex.put(name, this.fileIndex.get(name));
			this.fileIndex.remove(name);
			this.log.log("IO error while loading computer \""+name+"\". This will be temporarily removed from the index. Message reads: "+Logger.Trace(e), "File manager", "ERROR");
		}

	}

	public synchronized Computer change(String name, Computer data, String who)
	{
		if(!this.exists(name)) return null;
		Computer from = this.fetchComputer(name);
		Computer to = new Computer(data.getName(), data.getImage(), from.getPatches());
		String patchLog = "";
		boolean changed = false;
		boolean renamed = false;
		if(data.getName().compareTo(from.getName())!=0)
		{
			if(!( this.database.containsKey(data.getName()) || this.fileIndex.containsKey(data.getName())
					|| this.invalidIndex.containsKey(data.getName()) ))
			{
				changed = true;
				renamed = true;
				patchLog += "Name has been changed from \""+from.getName()+"\" to \""+data.getName()+"\".  ";
			}
		}
		if(data.getImage().compareTo(from.getImage())!=0)
		{
			changed = true;
			patchLog += "Image has been changed from \""+from.getImage()+"\" to \""+data.getImage()+"\".  ";
		}
		to.setDrive(data.getDrive());
		if(data.getDrive().compareTo(from.getDrive())!=0)
		{
			changed = true;
			patchLog += "Drive has been changed from \""+from.getDrive()+"\" to \""+data.getDrive()+"\".  ";
		}
		to.setSerial(from.getSerial());
		to.setModel(from.getModel());
		to.setUser(data.getUser());
		if(data.getUser().compareTo(from.getUser())!=0)
		{
			changed = true;
			patchLog += "User has been changed from \""+from.getUser()+"\" to \""+data.getUser()+"\".  ";
		}
		to.setLocation(data.getLocation());
		if(data.getLocation().compareTo(from.getLocation())!=0)
		{
			changed = true;
			patchLog += "Location has been changed from \""+from.getLocation()+"\" to \""+data.getLocation()+"\".";
		}
		if(changed)
		{
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
			String date = sdf.format(d);
			to.addTechPatch(new Update(who, date, patchLog));
			this.database.put(to.getName(), to);
			if(renamed)
			{
				this.database.remove(name);
				String path = this.fileIndex.get(name);
				this.fileIndex.put(to.getName(), path);
				this.fileIndex.remove(name);
				this.log.log("Computer \""+from.getName()+"\" has been renamed to \""+data.getName()+"\".  File index is being updated.", "File manager", "WARN");
			}
			this.log.log("Computer \""+to.getName()+"\" has been updated", "File manager");
			if(mailOK)
			{
				String messageText = "A machine has been modified\n";
				messageText +="Name: "+to.getName();
				messageText+="\nChanged by: "+who+"\n";
				messageText+="Changes made: "+patchLog;
				this.sendMail("Machine removed", messageText);
			}
			return to;
		}
		return from;
	}

	public void sendMail(String subject, String messageText)
	{
		Properties props = new Properties();
		props.put("mail.smtp.host", "mail_server_hostname.wustl.edu");
		Session sesh = Session.getInstance(props, null);
		try{
			MimeMessage msg = new MimeMessage(sesh);
			msg.setFrom("service_tracker_noreply@wustl.edu");
			msg.setRecipients(Message.RecipientType.TO, "my_supervisor's_email@wustl.edu");
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setText(messageText);
			Transport.send(msg);
		}
		catch (MessagingException mex)
		{
			this.log.log("Failed to send email: "+mex, "File Manager Mailer", "ERROR");
		}
	}



}
