package main;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Font;

public class ServerDisplay extends JFrame {

	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerDisplay frame = new ServerDisplay();
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
	public ServerDisplay() {
		this(null);
	}
	
	public ServerDisplay(ServerMain sm)
	{
		super("WULIB Service Tracker");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 217, 199);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblActiveClients = new JLabel("Active Clients");
		lblActiveClients.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblActiveClients.setBounds(10, 24, 74, 14);
		contentPane.add(lblActiveClients);
		
		textField = new JTextField();
		textField.setText("0");
		textField.setEditable(false);
		textField.setBounds(80, 21, 86, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JButton btnShutdownNow = new JButton("Force Shutdown");
		btnShutdownNow.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnShutdownNow.setToolTipText("Save data and shut down immediately");
		JButton btnShutdown = new JButton("Shutdown");
		btnShutdown.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnShutdown.setToolTipText("Stop accepting new clients, and wait up to 2 minutes for clients to disconnect before shutting down");
		btnShutdownNow.setBounds(45, 101, 121, 23);
		btnShutdownNow.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				sm.setShutdown(2);
				btnShutdown.setEnabled(false);
				btnShutdownNow.setEnabled(false);
			}
			
		});
		contentPane.add(btnShutdownNow);
		
		
		btnShutdown.setBounds(45, 69, 121, 23);
		btnShutdown.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				sm.setShutdown(1);
				btnShutdown.setEnabled(false);
				btnShutdownNow.setEnabled(false);
			}
			
		});
		contentPane.add(btnShutdown);
		
		JLabel lblByAaronGraubert = new JLabel("By Aaron Graubert");
		lblByAaronGraubert.setFont(new Font("Tahoma", Font.PLAIN, 8));
		lblByAaronGraubert.setBounds(10, 135, 74, 14);
		contentPane.add(lblByAaronGraubert);
		
		JLabel lblVersion = new JLabel("Version "+ServerMain.version);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblVersion.setBounds(10, 49, 156, 14);
		contentPane.add(lblVersion);
		
		sm.getPCS().addPropertyChangeListener("clientCount", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textField.setText(evt.getNewValue().toString());
			}
			
		});
		
		sm.getPCS().addPropertyChangeListener("shutdown", new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				//System.out.println("Server has finished shutdown.  Exiting thread");
				System.exit(0);
				
			}
			
		});
		
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				
				int choice = JOptionPane.showConfirmDialog(e.getWindow(),
						"Click OK to close this window and shutdown the server.\n"
						+ "The server may run for a few seconds in the background to shutdown properly",
						"Shutdown message", JOptionPane.OK_CANCEL_OPTION);
				if(choice == 0)
				{
					sm.setShutdown(3);
					setVisible(false);
				}
				//System.out.println("Triggered hard shutdown");
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
}
