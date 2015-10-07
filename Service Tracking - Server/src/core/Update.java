package core;

public class Update {
	private String tech, date, notes;

	public Update(String tech, String date, String notes) {
		this.tech = tech;
		this.date = date;
		this.notes = notes;
	}

	public String getTech() {
		return tech;
	}

	public String getDate() {
		return date;
	}

	public String getNotes() {
		return notes;
	}

}
