package display;
//Author: Aaron Graubert  agraubert@wustl.edu
//Edited from origional source to remove any sensitive information

import io.Comms;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Toolkit;

public class ClientDisplay extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8835077323936726060L;
	private JPanel contentPane;
	private Comms comms;
	private static ArrayList<String> hardwareInventory;
	private static ArrayList<String> images;
	private static HashMap<String, Long> dates;
	private static ClientDisplay instance;
	public static final int version = 2;
	private boolean connected = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientDisplay frame = new ClientDisplay();
					//frame.setUndecorated(true);
					//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientDisplay() {
		super("WULIB Hardware Service Traking v"+version);
		hardwareInventory = new ArrayList<String>();
		images = new ArrayList<String>();
		instance = this;
		dates = new HashMap<String, Long>();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //we must inform the server that we've disconnected when we close
		this.comms = new Comms();
		setBounds(100, 100, 808, 445);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(contentPane);
		contentPane.setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//tabbedPane.setBorder(null);
		tabbedPane.setBounds(5, 5, 534, 399);
		tabbedPane.add("Sync", new SyncPanel("address_or_hostname_of_server.wustl.edu", this.comms));
		tabbedPane.add("Hardware Inventory", new HardwarePanel(this.comms));
		tabbedPane.add("Admins", new AdminPanel(this.comms));
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
		this.comms.getPCS().addPropertyChangeListener("syncComplete", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, true);
				connected = true;
			}

		});
		//add(tabbedPane);
		this.setContentPane(tabbedPane);

		this.comms.getPCS().addPropertyChangeListener("disconnect", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				tabbedPane.setEnabledAt(1, false);
				tabbedPane.setEnabledAt(2, false);
				tabbedPane.setSelectedIndex(0);
				connected = false;
				JOptionPane.showMessageDialog(ClientDisplay.getInstance(), 
						"The server has initiated shutdown\n"
						+ "Press OK to close the client", 
						"Shutdown Confirmation", 
						JOptionPane.OK_OPTION);
				ClientDisplay.getInstance().dispatchEvent(new WindowEvent(ClientDisplay.getInstance(),
						WindowEvent.WINDOW_CLOSING));
			}

		});
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				if(connected)
				{
					comms.writeUTF("logout");
					comms.close();
				}
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}

	public Comms getComms()
	{
		return this.comms;
	}

	public static ArrayList<String> getHardware()
	{
		return hardwareInventory;
	}

	public static ArrayList<String> getImages()
	{
		return images;
	}

	public static JFrame getInstance()
	{
		return instance;
	}
	
	public static void setDate(String s, long l)
	{
		dates.put(s, l);
	}
	
	public static long getDate(String s)
	{
		Long l = dates.get(s);
		if(l != null) return l.longValue();
		return -1L;
	}
}
