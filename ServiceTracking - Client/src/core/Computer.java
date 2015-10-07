package core;

import java.util.ArrayList;

public class Computer {
	private String name, image;
	private ArrayList<Update> patches;
	
	public Computer(String name, String image, ArrayList<Update> patches)
	{
		this.name = "" + name;
		this.image = "" + image;
		this.patches = new ArrayList<Update>();
		for(int i = 0; i<patches.size(); ++i)
		{
			this.patches.add(patches.get(i));
		}
		System.out.println("NEW PC");
	}
	
	public Computer(String name, String image)
	{
		this.name = "" + name;
		this.image = "" + image;
		this.patches = new ArrayList<Update>();
		System.out.println("NEW PC");
	}
	
	public void addPatch(Update u)
	{
		this.patches.add(u);
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
}
