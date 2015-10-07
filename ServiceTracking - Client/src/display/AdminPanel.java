package display;

import io.Comms;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AdminPanel extends JPanel {
	private JTextField textField;
	private Comms comms;

	/**
	 * Create the panel.
	 */
	public AdminPanel() {
		this(new Comms());
	}
	public AdminPanel(Comms c)
	{
		this.comms = c;
		setLayout(null);

		JLabel lblAddNewImage = new JLabel("Add New Image");
		lblAddNewImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAddNewImage.setBounds(10, 39, 82, 14);
		add(lblAddNewImage);

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 11));
		textField.setBounds(90, 36, 136, 20);
		add(textField);
		textField.setColumns(10);

		JComboBox<String> comboBox = new JComboBox<String>();
		JButton btnAdd = new JButton("Add");
		btnAdd.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnAdd.setBounds(236, 35, 89, 23);
		btnAdd.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = textField.getText();
				if(txt.length() > 0)
				{
					comms.writeUTF("add"); //tell the server to add the following image
					comms.writeUTF(txt);
					ClientDisplay.getImages().add(txt); //add it to the local list
					comboBox.addItem(txt); //and the display
					textField.setText("");
				}
			}

		});
		add(btnAdd);

		JLabel lblRemoveImage = new JLabel("Remove Image");
		lblRemoveImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblRemoveImage.setBounds(10, 89, 82, 14);
		add(lblRemoveImage);


		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 11));
		comboBox.setBounds(90, 86, 136, 20);
		add(comboBox);

		JButton btnRemove = new JButton("Remove");
		btnRemove.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnRemove.setBounds(236, 85, 89, 23);
		btnRemove.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = comboBox.getSelectedIndex();
				comms.writeUTF("remove"); //remove from the server
				comms.writeUTF(comboBox.getItemAt(i));
				comboBox.removeItemAt(i); //remove from local display
				ClientDisplay.getImages().remove(i); //remove from local list
			}

		});
		add(btnRemove);

		JButton btnShutdownServer = new JButton("Shutdown Server");
		btnShutdownServer.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnShutdownServer.setToolTipText("Server waits up to 2 minutes for clients to disconnect before saving and shutting down");
		btnShutdownServer.setBounds(10, 161, 145, 23);
		btnShutdownServer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				comms.writeUTF("shutdown");
				comms.close();
				comms.getPCS().firePropertyChange("disconnect", null, null);
			}

		});
		add(btnShutdownServer);

		JButton btnForceShutdownServer = new JButton("Force Shutdown Server");
		btnForceShutdownServer.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnForceShutdownServer.setToolTipText("Server saves and shuts down without waiting for clients");
		btnForceShutdownServer.setBounds(201, 161, 173, 23);
		btnForceShutdownServer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				comms.writeUTF("shutdown-now");
				comms.close();
				comms.getPCS().firePropertyChange("disconnect", null, null);
			}

		});
		add(btnForceShutdownServer);

		this.comms.getPCS().addPropertyChangeListener("syncComplete", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textField.setText("");
				comboBox.removeAllItems();
				for(int i = 0; i<ClientDisplay.getImages().size(); ++i)
				{
					comboBox.addItem(ClientDisplay.getImages().get(i));
				}
			}

		});
	}
}
