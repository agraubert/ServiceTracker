package display;

import io.Comms;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JProgressBar;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SyncPanel extends JPanel {
	private JTextField addressField;
	private JTextField statusField;
	private JProgressBar progressHardware, progressImages;
	private Comms comms;

	/**
	 * Create the panel.
	 */
	public SyncPanel()
	{
		this("localhost", new Comms());
	}

	public SyncPanel(String address, Comms c) {
		this.comms = c;
		setLayout(null);
		JLabel lblServerAddress = new JLabel("Server Address:");
		lblServerAddress.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblServerAddress.setBounds(10, 11, 88, 14);
		add(lblServerAddress);

		addressField = new JTextField();
		addressField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		addressField.setBounds(94, 8, 210, 20);
		addressField.setText(address);
		add(addressField);
		addressField.setColumns(10);

		JButton btnBeginSync = new JButton("Begin Sync");
		btnBeginSync.setBounds(314, 7, 89, 23);
		btnBeginSync.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnBeginSync.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){

					@Override
					public void run() {
						synchronized(lblServerAddress)
						{
							statusField.setText("Contacting server...");
							comms.connect(addressField.getText());
							comms.getPCS().firePropertyChange("syncStart", null, null);
							progressHardware.setValue(0);
							progressImages.setValue(0);
							ClientDisplay.getHardware().clear();
							statusField.setText("Downloading Hardware Inventory...");
							comms.writeUTF("request");
							comms.writeUTF("computers");
							int size = comms.readInt();
							progressHardware.setMaximum(size);
							progressHardware.setMinimum(0);
							progressHardware.setValue(0);
							for(int i = 0; i<size; ++i)
							{
								String s = comms.readUTF();
								ClientDisplay.getHardware().add(s);
								ClientDisplay.setDate(s, comms.readLong()); //Dates
								comms.getPCS().firePropertyChange("syncHardwareProgress", i, i+1);
							}
							ClientDisplay.getImages().clear();
							statusField.setText("Downloading Image Inventory...");
							comms.writeUTF("request");
							comms.writeUTF("images");
							size = comms.readInt();
							progressImages.setMaximum(size);
							progressImages.setMinimum(0);
							progressImages.setValue(0);
							for(int i = 0; i<size; ++i)
							{
								ClientDisplay.getImages().add(comms.readUTF());
								comms.getPCS().firePropertyChange("syncImagesProgress", i, i+1);
							}
							comms.getPCS().firePropertyChange("syncComplete", null, null);
							statusField.setText("Sync Complete");
							addressField.setEditable(false);
						}
					}

				}).start();

			}

		});
		add(btnBeginSync);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 36, 430, 5);
		add(separator);

		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblStatus.setBounds(10, 54, 46, 14);
		add(lblStatus);

		statusField = new JTextField();
		statusField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		statusField.setBounds(51, 52, 253, 20);
		statusField.setEditable(false);
		statusField.setText("Waiting to begin sync");
		add(statusField);
		statusField.setColumns(10);

		JLabel lblHardware = new JLabel("Hardware:");
		lblHardware.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblHardware.setBounds(10, 95, 69, 14);
		add(lblHardware);

		progressHardware = new JProgressBar();
		progressHardware.setFont(new Font("Tahoma", Font.PLAIN, 11));
		progressHardware.setBounds(74, 95, 329, 14);
		this.comms.getPCS().addPropertyChangeListener("syncHardwareProgress",
				new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				progressHardware.setValue((int) evt.getNewValue());

			}

		});
		add(progressHardware);

		JLabel lblImages = new JLabel("Images:");
		lblImages.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblImages.setBounds(10, 156, 46, 14);
		add(lblImages);

		progressImages = new JProgressBar();
		progressImages.setFont(new Font("Tahoma", Font.PLAIN, 11));
		progressImages.setBounds(74, 156, 329, 14);
		this.comms.getPCS().addPropertyChangeListener("syncImagesProgress",
				new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				progressImages.setValue((int) evt.getNewValue());

			}

		});
		add(progressImages);

		JLabel lblByAaronGraubert = new JLabel("By Aaron Graubert");
		lblByAaronGraubert.setFont(new Font("Tahoma", Font.PLAIN, 8));
		lblByAaronGraubert.setBounds(10, 181, 69, 14);
		add(lblByAaronGraubert);

	}

}
