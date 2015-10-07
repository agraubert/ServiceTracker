package core;

import java.util.ArrayList;
import java.util.Date;

public class Computer {
	private String name, image, drive = "Not Specified";
	private String serial = "Not_Specified", model = "Not Specified", user = "Not Specified", location = "Not Specified";
	private ArrayList<Update> patches;
	private Long modified;
	
	public Computer(String name, String image, ArrayList<Update> patches)
	{
		this.name = "" + name;
		this.image = "" + image;
		this.patches = new ArrayList<Update>();
		for(int i = 0; i<patches.size(); ++i)
		{
			this.patches.add(patches.get(i));
		}
	}
	
	public Computer(String name, String image)
	{
		this.name = "" + name;
		this.image = "" + image;
		this.patches = new ArrayList<Update>();
	}
	
	public void addPatch(Update u)
	{
		this.patches.add(u);
	}
	
	public void addTechPatch(Update u)
	{
		this.patches.add(0, u);
		this.newChange();
	}
	
	public String getName()
	{
		return this.name;
	}

	public String getImage()
	{
		return this.image;
	}
	
	public ArrayList<Update> getPatches()
	{
		return this.patches;
	}
	
	public void setModified(long l)
	{
		this.modified = l;
	}
	
	public void newChange()
	{
		this.setModified(new Date().getTime());
	}
	
	public long getModified()
	{
		return this.modified;
	}
	
	public void setDrive(String drive)
	{
		this.drive = drive;
	}
	
	public String getDrive()
	{
		return this.drive;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
