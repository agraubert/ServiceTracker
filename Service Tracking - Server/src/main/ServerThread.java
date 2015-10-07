package main;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import core.*;
import io.FileManager;
import io.Logger;
import io.ServerComms;

public class ServerThread implements Runnable{
	private ServerComms comms;
	private ServerMain parent;
	private FileManager files;
	private Logger log;
	private String name;
	private int id;

	public ServerThread(Socket s, ServerMain p, FileManager fm, Logger l, int size)
	{
		if(s == null) s.isClosed();
		this.parent = p;
		this.files = fm;
		this.log = l;
		this.name = "Thread "+size;
		this.id = size;
		this.comms = new ServerComms(s, l, this.name);
	}

	@Override
	public void run() {
		this.parent.checkIn();
		this.log.log("Checked in new client", this.name);
		//System.out.println("Checked in");
		try
		{
			this.service();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			this.log.log("Encountered an error while connected to client. Message reads: "+Logger.Trace(t), this.name, "ERROR");
		}
		finally
		{
			//System.out.println("Checked out");
			this.parent.checkOut(this.id);
			this.comms.close();
			this.log.log("Client checked out", this.name);
		}
	}

	public void service()
	{
		String greeting = this.comms.readUTF();
		int version = this.comms.readInt();
		if(greeting.compareTo("Hello.  Service Tracker")!=0) //it's a very friendly protocol
		{
			this.comms.writeUTF("bye");
			this.comms.close();
			this.log.log("Client provided invalid greeting: "+greeting, this.name);
			return;
		}
		if(version != ServerMain.version)
		{
			this.comms.writeUTF("bad version");
			this.comms.close();
			this.log.log("Client provided invalid version number", this.name);
			return;
		}
		this.comms.writeUTF("ok");
		//System.out.println("New Client Initialized");
		while(true)
		{
			String command = this.comms.readUTF();
			if(command.compareTo("request")==0)
			{
				//System.out.println("List request");
				String list = this.comms.readUTF();
				if(list.compareTo("computers")==0)
				{
					this.log.log("Providing hardware inventory", this.name);
					Set<String> inventory = this.files.getHardwareInventory();
					this.comms.writeInt(inventory.size());
					/*for(String s : inventory)
					{
						Computer c = this.files.fetchComputer(s);
						this.comms.writeUTF(c.getName());
						this.comms.writeUTF(c.getImage());
						ArrayList<Update> p = c.getPatches();
						this.comms.writeInt(p.size());
						for(int i = 0; i<p.size(); ++i)
						{
							Update u = p.get(i);
							this.comms.writeUTF(u.getTech());
							this.comms.writeUTF(u.getDate());
							this.comms.writeUTF(u.getNotes());
						}
					}*/
					for(String s : inventory) 
					{
						this.comms.writeUTF(s);
						this.comms.writeLong(this.files.fetchDate(s)); //Dates
					}
				}
				else if(list.compareTo("images")==0)
				{
					this.log.log("Providing images inventory", this.name);
					Set<String> images = this.files.getImagesInventory();
					this.comms.writeInt(images.size());
					for(String s: images)
					{
						this.comms.writeUTF(s);
					}
				}
				else if(list.compareTo("unit")==0)
				{
					//System.out.println("Unit request");
					String name = this.comms.readUTF();
					Computer c = this.files.fetchComputer(name);
					if(c==null)
					{
						this.comms.writeUTF("bad");
						this.log.log("Client requested bad computer: "+name, this.name);
					}
					else
					{
						this.log.log("Providing requested computer: "+name, this.name);
						this.comms.writeUTF("ok");
						this.comms.writeUTF(c.getImage());
						this.comms.writeLong(c.getModified()); //Dates
						this.comms.writeUTF(c.getDrive()); //Drives
						this.comms.writeUTF(c.getSerial()); //serial
						this.comms.writeUTF(c.getModel()); //model
						this.comms.writeUTF(c.getUser()); //user
						this.comms.writeUTF(c.getLocation()); //loc
						ArrayList<Update> p = c.getPatches();
						this.comms.writeInt(p.size());
						for(int i = 0; i<p.size(); ++i)
						{
							Update u = p.get(i);
							this.comms.writeUTF(u.getTech());
							this.comms.writeUTF(u.getDate());
							this.comms.writeUTF(u.getNotes());
						}
					}
				}
			}
			else if(command.compareTo("patch")==0)
			{
				String name = this.comms.readUTF();
				if(!this.files.getHardwareInventory().contains(name)) this.comms.writeUTF("bad");
				else
				{
					this.comms.writeUTF("ok");
					Computer c = this.files.fetchComputer(name);
					String lastDate = this.comms.readUTF();
					if(c.getPatches().size() > 0 && lastDate.compareTo(c.getPatches().get(0).getDate())!=0)
					{
						this.log.log("Transmitting patch for computer: "+name, this.name);
						this.comms.writeUTF("update");
						this.comms.writeUTF(c.getImage()); //.image
						this.comms.writeUTF(c.getDrive()); //drive
						this.comms.writeUTF(c.getUser()); //user
						this.comms.writeUTF(c.getLocation()); //loc
						ArrayList<Update> p = c.getPatches();
						this.comms.writeInt(p.size());
						for(int i = 0; i<p.size(); ++i)
						{
							Update u = p.get(i);
							this.comms.writeUTF(u.getTech());
							this.comms.writeUTF(u.getDate());
							this.comms.writeUTF(u.getNotes());
						}
					}
					else this.comms.writeUTF("ok");
					this.log.log("Client added note to computer: "+name, this.name);
					String tech = this.comms.readUTF();
					String notes = this.comms.readUTF();
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
					String date = sdf.format(d);
					this.files.fetchComputer(name).addTechPatch(new Update(tech, date, notes));
				}
			}
			else if(command.compareTo("track")==0)
			{
				String name = this.comms.readUTF();
				if(this.files.exists(name)) this.comms.writeUTF("bad");
				else
				{
					this.comms.writeUTF("ok");
					String image = this.comms.readUTF();
					String drive = this.comms.readUTF(); //Drive
					String serial = this.comms.readUTF(); //serial
					String model = this.comms.readUTF(); //model
					String user = this.comms.readUTF(); //user
					String loc = this.comms.readUTF(); //loc
					int patches = this.comms.readInt();
					Computer c = new Computer(name, image);
					for(int i = 0; i<patches; ++i)
					{
						String tech = this.comms.readUTF();
						String date = this.comms.readUTF();
						String notes = this.comms.readUTF();
						c.addPatch(new Update(tech, date, notes));
					}
					c.newChange();
					c.setDrive(drive);
					c.setSerial(serial);
					c.setModel(model);
					c.setUser(user);
					c.setLocation(loc);
					this.files.trackHardware(c);
				}
			}
			else if(command.compareTo("edit")==0)
			{
				String oldName = this.comms.readUTF();
				if(!this.files.exists(oldName))
				{
					this.comms.writeUTF("bad");
					this.log.log("Client requested edit on bad computer: "+oldName, this.name);
				}
				else
				{
					this.log.log("Preparing to edit requested computer: "+oldName, this.name);
					this.comms.writeUTF("ok");
					String who = this.comms.readUTF();
					String newName = this.comms.readUTF();
					String newImage = this.comms.readUTF();
					String newDrive = this.comms.readUTF();
					String newUser = this.comms.readUTF();
					String newLoc = this.comms.readUTF();
					Computer c = new Computer(newName, newImage);
					c.setDrive(newDrive);
					c.setUser(newUser);
					c.setLocation(newLoc);
					Computer r = this.files.change(oldName, c, who);
					if(r==null)
					{
						this.comms.writeUTF("bad");
					}
					else
					{
						if(r.getName().compareTo(newName)!=0) this.comms.writeUTF("taken");
						else this.comms.writeUTF("ok");
						ArrayList<Update> p = r.getPatches();
						this.comms.writeInt(p.size());
						for(int i = 0; i<p.size(); ++i)
						{
							Update u = p.get(i);
							this.comms.writeUTF(u.getTech());
							this.comms.writeUTF(u.getDate());
							this.comms.writeUTF(u.getNotes());
						}
					}
				}
			}
			else if(command.compareTo("add")==0)
			{
				this.files.addImage(this.comms.readUTF());
			}
			else if(command.compareTo("untrack")==0)
			{
				this.files.untrackHardware(this.comms.readUTF());
			}
			else if(command.compareTo("remove")==0)
			{
				this.files.removeImage(this.comms.readUTF());
			}
			else if(command.compareTo("logout")==0)
			{
				this.comms.close();
				return;
			}
			else if(command.compareTo("shutdown")==0)
			{
				this.log.log("Client initated standard shutdown", this.name);
				this.parent.setShutdown(1);
				this.comms.close();
				return;
			}
			else if(command.compareTo("shutdown-now")==0)
			{
				this.log.log("Client initiated force shutdown", this.name);
				this.parent.setShutdown(2);
				this.comms.close();
				return;
			}
			else
			{
				this.log.log("Client sent unknown command\""+command+"\"", this.name);
				this.comms.close();
				return;
			}
		}
	}

}
