package display;

import io.Comms;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HardwarePanel extends JPanel {
	private JTextField nameField;
	private JTextField imageField;
	private JTextArea notesText;
	private boolean firstClick;
	private Comms comms;
	private String lastDate = "";
	private DefaultListModel<String> inventoryModel;
	private boolean creationMode = false, syncOverride = false;
	private JButton btnSubmit;
	private boolean alpha = true;
	private JTextField driveField;
	private boolean driveClick = false;
	private JTextField txtSerial;
	private JTextField txtModel;
	private JTextField txtUser;
	private JTextField txtLocation;
	private String oldComputerName = "";
	private boolean editMode = false;
	private boolean warn = false;

	/**
	 * Create the panel.
	 */
	public HardwarePanel()
	{
		this(new Comms());
	}

	public HardwarePanel(Comms c) {
		setLayout(null);
		this.comms = c;
		JLabel lblHardwareInventory = new JLabel("Hardware Inventory");
		lblHardwareInventory.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblHardwareInventory.setBounds(10, 23, 105, 14);
		add(lblHardwareInventory);

		JScrollPane scrollInventory = new JScrollPane();
		scrollInventory.setBounds(10, 45, 180, 262);
		add(scrollInventory);

		inventoryModel = new DefaultListModel<String>();
		JButton btnRemoveSelected = new JButton("Remove Computer");
		btnRemoveSelected.setToolTipText("Remove the selected computer from the Hardware Inventory");
		JButton btnNewNote = new JButton("New Note");
		JButton btnEditComputer = new JButton("Edit Computer");
		btnEditComputer.setToolTipText("Edit the selected computer");
		btnEditComputer.setEnabled(false);

		JList<String> inventoryList = new JList<String>(inventoryModel);
		inventoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inventoryList.setFont(new Font("Tahoma", Font.PLAIN, 11));
		inventoryList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!syncOverride && !e.getValueIsAdjusting() && inventoryList.getSelectedIndex()!=-1)
				{
					creationMode = false;
					editMode = false;
					btnRemoveSelected.setEnabled(true);
					btnNewNote.setEnabled(true);
					btnSubmit.setEnabled(false);
					/*nameField.setEditable(false);
					txtSerial.setEditable(false);
					txtModel.setEditable(false);
					txtUser.setEditable(false);
					txtLocation.setEditable(false);
					driveField.setEditable(false);*/
					allEditable(false);
					clearAll();
					String cName = inventoryModel.getElementAt(inventoryList.getSelectedIndex());
					nameField.setText(cName);
					imageField.setText("--Loading Machine--");
					//notesText.setText(null);
					//driveField.setText("");
					new Thread(new Runnable(){

						@Override
						public void run() {
							synchronized(lblHardwareInventory)
							{
								btnRemoveSelected.setEnabled(false);
								btnNewNote.setEnabled(false);
								btnEditComputer.setEnabled(false);
								comms.writeUTF("request");
								comms.writeUTF("unit");
								comms.writeUTF(cName);
								if(comms.readUTF().compareTo("ok")==0)
								{
									String image = comms.readUTF();
									long timestamp = comms.readLong(); //Dates
									ClientDisplay.setDate(cName, timestamp); //Dates
									String drive = comms.readUTF(); //drive
									String serial = comms.readUTF(); 
									String model = comms.readUTF();
									String user = comms.readUTF();
									String loc = comms.readUTF();
									int size = comms.readInt();
									String notes = "";
									for(int i = 0; i<size; ++i)
									{
										notes +="Tech: " + comms.readUTF() + "\n";
										String date = comms.readUTF();
										if(i==0) lastDate = date;
										notes +="Date: " + date + "\n";
										notes +=comms.readUTF();
										if(i<size-1)
										{
											notes+="\n------------------\n";
										}
									}
									imageField.setText(image);
									driveField.setText(drive);
									notesText.setText(notes);
									txtSerial.setText(serial);
									txtModel.setText(model);
									txtUser.setText(user);
									txtLocation.setText(loc);
									txtSerial.setToolTipText("");
									txtModel.setToolTipText("");
									imageField.setToolTipText("");
								}
								else
								{
									imageField.setText("----");
									notesText.setText("Invalid Computer Name.\n"
											+ "Server could not find the requested machine\n"
											+ "Please re-sync and try again.");
								}
								btnRemoveSelected.setEnabled(true);
								btnNewNote.setEnabled(true);
								btnEditComputer.setEnabled(true);
							}
						}}).start();

				}

			}

		});
		scrollInventory.setViewportView(inventoryList);

		JLabel lblMachineName = new JLabel("Machine Name:");
		lblMachineName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblMachineName.setBounds(200, 95, 82, 14);
		add(lblMachineName);

		firstClick = false;
		nameField = new JTextField();
		nameField.setEditable(false);
		nameField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		nameField.setBounds(279, 92, 228, 20);
		add(nameField);
		nameField.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(creationMode && firstClick)
				{
					nameField.setText("");
					firstClick = false;
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});
		nameField.setColumns(10);

		JLabel lblMachineImage = new JLabel("Machine Image:");
		lblMachineImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblMachineImage.setBounds(200, 126, 82, 14);
		add(lblMachineImage);

		imageField = new JTextField();
		imageField.setEditable(false);
		imageField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		imageField.setBounds(289, 123, 218, 20);
		imageField.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(creationMode)
				{
					try
					{
						String choice =  JOptionPane.showInputDialog(ClientDisplay.getInstance(),
								"Choose the image for this machine", 
								"Set Image", 
								JOptionPane.QUESTION_MESSAGE, null, 
								ClientDisplay.getImages().toArray(), 
								ClientDisplay.getImages().toArray()[0]).toString();
						if(choice != null) imageField.setText(choice);
					}
					catch(NullPointerException err)
					{

					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});
		add(imageField);
		imageField.setColumns(10);

		JLabel lblServiceNotes = new JLabel("Service Notes:");
		lblServiceNotes.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblServiceNotes.setBounds(215, 178, 70, 14);
		add(lblServiceNotes);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(299, 178, 476, 185);
		add(scrollPane);

		notesText = new JTextArea();
		notesText.setEditable(false);
		notesText.setLineWrap(false);
		scrollPane.setViewportView(notesText);


		btnNewNote.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnNewNote.setBounds(200, 217, 89, 23);
		btnNewNote.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(creationMode)
				{
					JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
							"This is not allowed while registering a new machine", 
							"Cannot add note", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(inventoryList.isSelectionEmpty()) return;
				String name = JOptionPane.showInputDialog(ClientDisplay.getInstance(), 
						"Tech Name:", "");
				String note = JOptionPane.showInputDialog(ClientDisplay.getInstance(), 
						"Service Notes:", "");
				if(name != null && note != null)
				{
					new Thread(new Runnable(){

						@Override
						public void run() {
							comms.writeUTF("patch");
							String cName = inventoryModel.getElementAt(inventoryList.getSelectedIndex());
							comms.writeUTF(cName);
							if(comms.readUTF().compareTo("ok")==0)
							{
								comms.writeUTF(lastDate);
								String oldText = notesText.getText();
								if(comms.readUTF().compareTo("update")==0)
								{
									String image = comms.readUTF();
									String drive = comms.readUTF();
									String user = comms.readUTF();
									String loc = comms.readUTF();
									int size = comms.readInt();
									oldText = "";
									for(int i = 0; i<size; ++i)
									{
										oldText +="Tech: " + comms.readUTF() + "\n";
										String date = comms.readUTF();
										if(i==0) lastDate = date;
										oldText +="Date: " + date + "\n";
										oldText +=comms.readUTF();
										if(i<size-1)
										{
											oldText+="\n------------------\n";
										}
									}
									imageField.setText(image);
									driveField.setText(drive);
									txtUser.setText(user);
									txtLocation.setText(loc);
								}
								comms.writeUTF(name);
								comms.writeUTF(note);
								notesText.setText(null);
								notesText.setText("Tech: "+name+"\n"
										+ "Date: Just Now\n"
										+ note+((oldText.length()==0)?
												"":"\n"
												+ "------------------\n"+oldText));
								ClientDisplay.setDate(cName, new Date().getTime());

							}
							else
							{
								notesText.setText(null);
								notesText.setText("Invalid Computer Name.\n"
										+ "Try re-syncing with the server");
							}
						}}).start();
				}
			}

		});
		add(btnNewNote);

		JSeparator separator = new JSeparator();
		separator.setBounds(200, 82, 575, 2);
		add(separator);

		JButton btnNewButton = new JButton("Add Computer");
		btnNewButton.setToolTipText("Add a new computer to the Hardware Inventory");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnNewButton.setBounds(200, 43, 126, 23);
		btnNewButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized(lblHardwareInventory)
				{
					creationMode = true;
					warn = false;
					btnRemoveSelected.setEnabled(false);
					btnNewNote.setEnabled(false);
					btnEditComputer.setEnabled(false);
					btnSubmit.setEnabled(true);
					allEditable(true);
					nameField.setText("<Enter Computer's Name>");
					//nameField.setEditable(true);
					//driveField.setEditable(true);
					driveField.setText("");
					imageField.setText("Click here to select image");
					notesText.setText("");
					txtSerial.setText("");
					txtModel.setText("");
					txtUser.setText("");
					txtLocation.setText("");
					//txtSerial.setEditable(true);
					syncOverride = true;
					inventoryList.clearSelection();
					syncOverride = false;
					firstClick = true;
					driveClick = true;
					txtSerial.setToolTipText("Serial # cannot be changed once submitted");
					txtModel.setToolTipText("Model cannot be changed once submitted");
					imageField.setToolTipText("Click here to select an image");
				}

			}

		});
		add(btnNewButton);

		JLabel labelArrow = new JLabel("------>");
		labelArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
		labelArrow.setBounds(336, 47, 37, 14);
		add(labelArrow);

		btnSubmit = new JButton("Submit");
		btnSubmit.setToolTipText("Submit changes");
		btnSubmit.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnSubmit.setEnabled(false);
		btnSubmit.setBounds(400, 43, 89, 23);
		btnSubmit.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(editMode)
				{
					allEditable(false);
					String techName = JOptionPane.showInputDialog(ClientDisplay.getInstance(), 
							"Tech Name:", "");
					String n = nameField.getText();
					String im = imageField.getText();
					String dr = driveField.getText();
					String us = txtUser.getText();
					String lo = txtLocation.getText();
					comms.writeUTF("edit");
					comms.writeUTF(oldComputerName);
					if(comms.readUTF().compareTo("ok")==0)
					{
						comms.writeUTF(techName);
						comms.writeUTF(n);
						comms.writeUTF(im);
						comms.writeUTF(dr);
						comms.writeUTF(us);
						comms.writeUTF(lo);
						String result = comms.readUTF();
						if(result.compareTo("bad")==0)
						{
							clearAll();
							JOptionPane.showMessageDialog(ClientDisplay.getInstance(), "The requested computer was not found in inventory.\n"
									+ "Please re-sync and try again", "Could not edit", JOptionPane.ERROR_MESSAGE);
						}
						else 
						{
							if(result.compareTo("taken")==0)
							{
								//FIXME
								nameField.setText(oldComputerName);
								n = oldComputerName;
								JOptionPane.showMessageDialog(ClientDisplay.getInstance(), "The requested name was already taken.\n"
										+ "Please choose another name.\nAll other changes have been applied.", "Could not rename", JOptionPane.WARNING_MESSAGE);
							}
							else
							{
								syncOverride = true;
								inventoryModel.setElementAt(n, inventoryList.getSelectedIndex());
								syncOverride = false;
								oldComputerName = n;
							}
							int size = comms.readInt();
							String oldText = "";
							for(int i = 0; i<size; ++i)
							{
								oldText +="Tech: " + comms.readUTF() + "\n";
								String date = comms.readUTF();
								if(i==0) lastDate = date;
								oldText +="Date: " + date + "\n";
								oldText +=comms.readUTF();
								if(i<size-1)
								{
									oldText+="\n------------------\n";
								}
							}
							notesText.setText(null);
							notesText.setText(oldText);
							ClientDisplay.setDate(n, new Date().getTime());
							btnEditComputer.setEnabled(true);
							btnRemoveSelected.setEnabled(true);
							btnNewNote.setEnabled(true);
						}

						
					}
					else
					{
						clearAll();
						JOptionPane.showMessageDialog(ClientDisplay.getInstance(), "The requested computer was not found in inventory.\n"
								+ "Please re-sync and try again", "Could not edit", JOptionPane.ERROR_MESSAGE);
					}
					creationMode = false;
					editMode = false;
					btnSubmit.setEnabled(false);
					txtSerial.setToolTipText("");
					imageField.setToolTipText("");
					txtModel.setToolTipText("");
					return;
				}
				if(imageField.getText().compareTo("Click here to select image")==0)
				{
					JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
							"You must set an image", 
							"Cannot submit machine", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String techName =  JOptionPane.showInputDialog(ClientDisplay.getInstance(), 
						"Tech Name:", "");
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
				String dateString = sdf.format(d);
				new Thread(new Runnable(){

					@Override
					public void run() {
						String n = nameField.getText();
						String im = imageField.getText();
						String dr = driveField.getText();
						String se = txtSerial.getText();
						String mo = txtModel.getText();
						String us = txtUser.getText();
						String lo = txtLocation.getText();
						if(se.length() == 0 || se.contains(" "))
						{
							JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
									"You must provide a serial number.  It may not contain whitespace", 
									"Cannot submit machine", JOptionPane.ERROR_MESSAGE);
							warn = false;
							return;
						}
						String warns = "";
						if(dr.length() == 0)
						{
							if(!warn)
							{
								warns += "The machine drive was not specified.  This should include the drive size and type.\n";
							}
							else
							{
								dr = "Not Specified";
								driveField.setText("Not Specified");
							}
						}
						if(mo.length() == 0)
						{
							if(!warn) warns += "The model was not specified.\n";
							else
							{
								mo = "Not Specified";
								txtModel.setText("Not Specified");
							}
						}
						if(us.length() == 0)
						{
							if(!warn) warns += "The user was not specified.\n";
							else
							{
								us = "Not Specified";
								txtUser.setText("Not Specified");
							}
						}
						if(lo.length() == 0)
						{
							if(!warn) warns += "The location was not specified.\n";
							else
							{
								lo = "Not Specified";
								txtLocation.setText("Not Specified");
							}
						}
						if(warns.length() > 0 && !warn)
						{
							JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
									"The no data was entered in the following fields:\n"+warns+"Please check the listed fields.  You can also ignore this warning and submit anyway", 
									"Data not provided", JOptionPane.WARNING_MESSAGE);
							warn = true;
							return;
						}
						comms.writeUTF("track");
						comms.writeUTF(n);
						if(comms.readUTF().compareTo("ok")==0)
						{
							comms.writeUTF(im);
							comms.writeUTF(dr);
							comms.writeUTF(se);
							comms.writeUTF(mo);
							comms.writeUTF(us);
							comms.writeUTF(lo);
							//comms.writeInt(0);
							comms.writeInt(1);
							comms.writeUTF(techName);
							comms.writeUTF(dateString);
							comms.writeUTF("Computer added to inventory");
							syncOverride = true;
							ClientDisplay.getHardware().add(n);
							ClientDisplay.setDate(n, new Date().getTime());
							//inventoryModel.addElement(n);
							inventoryModel.add(0,n);
							//inventoryList.setSelectedIndex(inventoryModel.size() - 1);
							inventoryList.setSelectedIndex(0);
							/*if(alpha) sortByName();
							else sortByDate();*/
							creationMode = false;
							syncOverride = false;
							btnRemoveSelected.setEnabled(true);
							btnNewNote.setEnabled(true);
							btnEditComputer.setEnabled(true);
							btnSubmit.setEnabled(false);
							//nameField.setEditable(false);
							//driveField.setEditable(false);
							allEditable(false);
							txtSerial.setToolTipText("");
							txtModel.setToolTipText("");
							imageField.setToolTipText("");
							notesText.setText(null);
							notesText.setText("Tech: "+techName+"\n"
									+ "Date: "+dateString+"\n"
											+ "Computer added to inventory");
						}
						else
						{
							JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
									"A computer already exists with that name", "Could not submit", 
									JOptionPane.INFORMATION_MESSAGE);
						}

					}

				}).start();

			}

		});
		add(btnSubmit);


		btnRemoveSelected.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnRemoveSelected.setBounds(200, 19, 126, 23);
		btnRemoveSelected.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(creationMode)
				{
					JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
							"This is not allowed while registering a new machine.\n"
									+ "You can re-sync or select a machine in inventory to cancel"
									+ " creating a new machine", 
									"Cannot remove machine", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(inventoryList.isSelectionEmpty()) return;
				int index = inventoryList.getSelectedIndex();
				//System.out.println(index);
				if(index>=0 && index < ClientDisplay.getHardware().size())
				{
					int confirm = JOptionPane.showConfirmDialog(ClientDisplay.getInstance(),
							"Are you sure you want to delete the computer: "
									+ inventoryModel.getElementAt(index), "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION);
					if(confirm==JOptionPane.OK_OPTION)
					{
						new Thread(new Runnable(){

							@Override
							public void run() {
								comms.writeUTF("untrack");
								comms.writeUTF(inventoryModel.getElementAt(index));
								syncOverride = true;
								inventoryModel.remove(index);
								/*nameField.setText("");
								imageField.setText("");
								driveField.setText("");
								notesText.setText("");*/
								clearAll();
								syncOverride = false;
							}

						}).start();
					}
				}

			}

		});
		add(btnRemoveSelected);

		JLabel lblSortBy = new JLabel("Sort by:");
		lblSortBy.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSortBy.setBounds(10, 315, 46, 14);
		add(lblSortBy);

		JButton btnName = new JButton("Name");
		JButton btnDate = new JButton("Date");
		btnDate.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnName.setEnabled(false);
		btnName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnName.setBounds(75, 311, 89, 23);
		btnName.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				alpha = true;
				syncOverride = true;
				btnName.setEnabled(false);
				btnDate.setEnabled(true);
				btnRemoveSelected.setEnabled(false);
				btnNewNote.setEnabled(false);
				sortByName();
				syncOverride = false;
				inventoryList.clearSelection();

			}

		});
		add(btnName);


		btnDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alpha = false;
				syncOverride = true;
				btnName.setEnabled(true);
				btnDate.setEnabled(false);
				btnRemoveSelected.setEnabled(false);
				btnNewNote.setEnabled(false);
				sortByDate();
				syncOverride = false;
				inventoryList.clearSelection();
			}
		});
		btnDate.setBounds(75, 340, 89, 23);
		add(btnDate);

		driveField = new JTextField();
		driveField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		driveField.setEditable(false);
		driveField.setBounds(289, 154, 218, 20);
		/*driveField.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(creationMode && driveClick)
				{
					driveField.setText("");
					driveClick = false;
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});*/
		add(driveField);
		driveField.setColumns(10);

		JLabel lblDrive = new JLabel("Machine Drive:");
		lblDrive.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblDrive.setBounds(200, 153, 82, 14);
		add(lblDrive);

		btnEditComputer.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnEditComputer.setBounds(569, 43, 126, 23);
		btnEditComputer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized(lblHardwareInventory)
				{
					oldComputerName = nameField.getText();
					creationMode = true;
					editMode = true;
					warn = false;
					btnRemoveSelected.setEnabled(false);
					btnNewNote.setEnabled(false);
					btnEditComputer.setEnabled(false);
					btnSubmit.setEnabled(true);
					//allEditable(true);
					nameField.setEditable(true);
					driveField.setEditable(true);
					txtUser.setEditable(true);
					txtLocation.setEditable(true);
					txtSerial.setToolTipText("Serial # cannot be changed once submitted");
					txtModel.setToolTipText("Model cannot be changed once submitted");
					imageField.setToolTipText("Click here to select a new image");
					//nameField.setText("<Enter Computer's Name>");
					//nameField.setEditable(true);
					//driveField.setEditable(true);
					//driveField.setText("");
					//imageField.setText("Click here to select image");
					//notesText.setText("");
					//txtSerial.setText("");
					//txtModel.setText("");
					//txtUser.setText("");
					//txtLocation.setText("");
					//txtSerial.setEditable(true);
					//syncOverride = true;
					//inventoryList.clearSelection();
					//syncOverride = false;
					//firstClick = true;
					//driveClick = true;
				}

			}

		});
		add(btnEditComputer);

		JLabel label = new JLabel("<------");
		label.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label.setBounds(524, 47, 46, 14);
		add(label);

		JLabel lblSerial = new JLabel("Serial #:");
		lblSerial.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSerial.setBounds(517, 95, 46, 14);
		add(lblSerial);

		txtSerial = new JTextField();
		txtSerial.setEditable(false);
		txtSerial.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtSerial.setBounds(569, 92, 206, 20);
		add(txtSerial);
		txtSerial.setColumns(10);

		JLabel lblModel = new JLabel("Model:");
		lblModel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblModel.setBounds(517, 126, 46, 14);
		add(lblModel);

		txtModel = new JTextField();
		txtModel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtModel.setEditable(false);
		txtModel.setBounds(556, 123, 95, 20);
		add(txtModel);
		txtModel.setColumns(10);

		JLabel lblUser = new JLabel("User:");
		lblUser.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblUser.setBounds(658, 126, 46, 14);
		add(lblUser);

		txtUser = new JTextField();
		txtUser.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtUser.setEditable(false);
		txtUser.setBounds(686, 123, 89, 20);
		add(txtUser);
		txtUser.setColumns(10);

		JLabel lblLocation = new JLabel("Location:");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblLocation.setBounds(517, 157, 46, 14);
		add(lblLocation);

		txtLocation = new JTextField();
		txtLocation.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtLocation.setEditable(false);
		txtLocation.setBounds(569, 154, 206, 20);
		add(txtLocation);
		txtLocation.setColumns(10);


		this.comms.getPCS().addPropertyChangeListener("syncStart", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				syncOverride = true;
				inventoryModel.removeAllElements();
				/*nameField.setText("");
				imageField.setText("");
				driveField.setText("");
				notesText.setText("");*/
				clearAll();
				btnSubmit.setEnabled(false);
				creationMode = false;
				//btnRemoveSelected.setEnabled(true);
				//btnNewNote.setEnabled(true);
				btnRemoveSelected.setEnabled(false);
				btnNewNote.setEnabled(false);
				btnEditComputer.setEnabled(false);
				nameField.setEditable(false);
			}

		});

		this.comms.getPCS().addPropertyChangeListener("syncComplete", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				for(String s : ClientDisplay.getHardware())
				{
					inventoryModel.addElement(s);
				}
				if(alpha) sortByName();
				else sortByDate();
				syncOverride = false;

			}

		});

	}

	private void allEditable(boolean e)
	{
		nameField.setEditable(e);
		txtSerial.setEditable(e);
		txtModel.setEditable(e);
		txtUser.setEditable(e);
		txtLocation.setEditable(e);
		driveField.setEditable(e);
	}

	private void clearAll()
	{
		nameField.setText("");
		txtSerial.setText("");
		txtModel.setText("");
		txtUser.setText("");
		txtLocation.setText("");
		driveField.setText("");
		imageField.setText("");
		notesText.setText(null);
	}

	public void sortByName()
	{
		/*nameField.setText("");
		imageField.setText("");
		notesText.setText("");
		driveField.setText("");*/
		clearAll();
		btnSubmit.setEnabled(false);
		creationMode = false;
		qsortName(0, inventoryModel.getSize()-1);
	}

	public void qsortName(int p, int r)
	{
		if(p < r)
		{
			int q = partitionName(p, r);
			qsortName(p, q-1);
			qsortName(q+1, r);
		}

	}

	public int partitionName(int p, int r)
	{
		int i = p-1;
		int j = p-1;
		do
		{
			++j;
			if(inventoryModel.get(j).compareToIgnoreCase(inventoryModel.get(r))<=0)
			{
				++i;
				String temp = inventoryModel.get(i);
				inventoryModel.set(i, inventoryModel.get(j));
				inventoryModel.set(j, temp);
			}
		}while(j<r-1);
		String temp = inventoryModel.get(i+1);
		inventoryModel.set(i+1,  inventoryModel.get(r));
		inventoryModel.set(r, temp);
		return i+1; 
	}

	public void sortByDate()
	{
		/*nameField.setText("");
		imageField.setText("");
		driveField.setText("");
		notesText.setText("");*/
		clearAll();
		btnSubmit.setEnabled(false);
		creationMode = false;
		qsortDate(0, inventoryModel.getSize()-1);
	}

	public void qsortDate(int p, int r)
	{
		if(p < r)
		{
			int q = partitionDate(p, r);
			qsortDate(p, q-1);
			qsortDate(q+1, r);
		}

	}

	public int partitionDate(int p, int r)
	{
		int i = p-1;
		int j = p-1;
		do
		{
			++j;
			if(ClientDisplay.getDate(inventoryModel.get(j)) > ClientDisplay.getDate(inventoryModel.get(r)))
			{
				++i;
				String temp = inventoryModel.get(i);
				inventoryModel.set(i, inventoryModel.get(j));
				inventoryModel.set(j, temp);
			}
		}while(j<r-1);
		String temp = inventoryModel.get(i+1);
		inventoryModel.set(i+1,  inventoryModel.get(r));
		inventoryModel.set(r, temp);
		return i+1; 
	}
}
