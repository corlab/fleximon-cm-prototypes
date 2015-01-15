package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import GUI.ImgButton;

import Enum.ActionType;
import Enum.StateType;
import Model.ConditionMonitoringModel;
import Model.ModelListener;

import rst.automation.ConditionMonitoring;

/**
 * Just a Test GUI...
 * 
 * @author tschuerm
 * 
 */
public class Graphicals extends JFrame implements MouseListener, Runnable, ModelListener {
	public static final Dimension Window_Size = new Dimension(700, 700);

	private ImgButton start_exploit_button;
	private ImgButton stop_exploit_button;
	private ImgButton clear_data_button;
	private ImgButton start_recording_button;
	private ImgButton stop_recording_button;
	private ImgButton write_data_button;
	private ImgButton load_data_button;
	private ImgButton learn_data_button;
	private ImgButton quit_button;
	
	private JPanel panelButton;
	private JPanel panelChangeDataSetNameArea;
	private JPanel panelmodelArea;
	private JPanel panelstateArea;
	private JLabel up;
	private JLabel data_set_name_label;
	private JLabel eventmemory_size_label;
	private JLabel eventmemory_num_label;
	private JLabel state_label;
	private JLabel ordering_label;
	private JLabel cumulativeconfidence_label;
	private String state_txt = "FlexiMon Condition Monitoring";

	private JTextField dataSetname_field= null;

	private final String start_exploit = "Start Exploitation";
	private final String stop_exploit = "Stop Exploitation";
	
	//Recording_Mode
	private final String start_recording = "Start Recording";
	private final String stop_recording = "Stop Recording";

	//Data_Mode
	private final String clear_data = "Clear Data";
	private final String write_data = "Write Data";
	private final String load_data = "Load Data";
	private final String learn_data = "Learn Data";
	
	private final String quit_program = "Quit";
	
	private ConditionMonitoringModel server = null;

	public Graphicals() {
		super("FlexiMon Condition Monitoring");
	}

	/**
	 * Method that generates the UserInterfaces and adds all Components.
	 */
	private void generate_UI() {
		setLocation(50, 50);
		setPreferredSize(new Dimension(700,700));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getContentPane().setBackground(Color.white);
		getContentPane().setLayout(new BorderLayout(5, 5));

		int IBsize = 115;
		start_exploit_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, start_exploit, true);
		stop_exploit_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, stop_exploit, true);
		clear_data_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, clear_data, true);
		start_recording_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, start_recording, true);
		stop_recording_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, stop_recording, true);
		write_data_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, write_data, true);
		load_data_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, load_data, true);
		learn_data_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, learn_data, true);
		quit_button = new ImgButton(Component.CENTER_ALIGNMENT, IBsize, quit_program, true);

		panelButton = new JPanel(new GridLayout(10, 1));
		panelChangeDataSetNameArea = new JPanel(new GridLayout(3, 1));
		panelmodelArea = new JPanel(new GridLayout(10, 1));
		panelstateArea = new JPanel(new GridLayout(10, 1));
		
		panelButton.setBackground(Color.white);
		panelmodelArea.setBackground(Color.white);
		panelstateArea.setBackground(Color.white);

		dataSetname_field = new JTextField();
		dataSetname_field.setEditable(true);
		dataSetname_field.setText("DSfleximon");
		dataSetname_field.setSize(10, 140);
		data_set_name_label = new JLabel("Data set name: ");

		//label_c_workflow = new JLabel("CM RPC");
		//label_c_workflow.setHorizontalAlignment(JLabel.RIGHT);
		//label_l_step = new JLabel("CM  STATE");
		//label_l_step.setHorizontalAlignment(JLabel.RIGHT);

		panelButton.add(start_exploit_button);//start_exploit
		panelButton.add(stop_exploit_button);//stop_exploit
		panelButton.add(start_recording_button);//start_recording
		panelButton.add(stop_recording_button);
		panelButton.add(clear_data_button);//clear_data
		panelButton.add(write_data_button);
		panelButton.add(load_data_button);
		panelButton.add(learn_data_button);
		panelButton.add(quit_button);
		
		//panelmodelArea.add(label_c_workflow);
		//panelmodelArea.add(c_workflow_pane);
		//panelmodelArea.add(label_l_step);
		//panelmodelArea.add(l_step_pane);
		
		this.update_panel_CM();
		
		//panelChangeDataSetNameArea.add(eventmemory_size_label);
		panelChangeDataSetNameArea.add(data_set_name_label);
		panelChangeDataSetNameArea.add(dataSetname_field);

		// Listener für Buttons
		start_exploit_button.addMouseListener(this);
		stop_exploit_button.addMouseListener(this);
		clear_data_button.addMouseListener(this);
		start_recording_button.addMouseListener(this);
		stop_recording_button.addMouseListener(this);
		write_data_button.addMouseListener(this);
		load_data_button.addMouseListener(this);
		learn_data_button.addMouseListener(this);
		quit_button.addMouseListener(this);

		// Labels erzeugen
		up = new JLabel(state_txt);
		up.setFont(new Font("Serif", Font.BOLD, 30));
		// Label zentrieren
		up.setHorizontalAlignment(JLabel.CENTER);
		up.setPreferredSize(new Dimension(700,60));
		data_set_name_label.setHorizontalAlignment(JLabel.CENTER);
		dataSetname_field.setHorizontalAlignment(JTextField.CENTER);
		// Labels auf Frame packen
		getContentPane().add(BorderLayout.NORTH, up);
		// Panels auf Frame packen
		getContentPane().add(BorderLayout.WEST, panelButton);
		getContentPane().add(BorderLayout.CENTER, panelmodelArea);
		getContentPane().add(BorderLayout.SOUTH, panelChangeDataSetNameArea);
		getContentPane().add(BorderLayout.EAST, panelstateArea);
		
		pack();
		setVisible(true);
		
		start_exploit_button.setActivated(true);
		start_recording_button.setActivated(true);
		clear_data_button.setActivated(true);
		stop_exploit_button.setActivated(false);
		stop_recording_button.setActivated(false);
		write_data_button.setActivated(true);
		load_data_button.setActivated(true);
		learn_data_button.setActivated(true);
		quit_button.setActivated(true);
		
		getContentPane().validate();
    	getContentPane().repaint();
	}
	
	private void update_panel_CM() {
		this.panelmodelArea.removeAll();
		this.panelstateArea.removeAll();
		double cc = 1.0;
		for(int i=0; i<server.getRequestOrder().size(); i++) {
			//String key : this.server.getConfidenceValueMemory().keySet()
			String key = server.getRequestOrder().get(i);
			if(this.server.getConfidenceValueMemory().containsKey(key)) {
				double d = this.server.getConfidenceValueMemory().get(key);
				if(d>1.0) {d=1.0;}
				if(d<0.0) {
					String strTemp = String.format("%s", key);
					JLabel j = new JLabel(strTemp,JLabel.CENTER);
					j.setBorder(BorderFactory.createLineBorder(Color.BLUE));
					j.setForeground(Color.BLUE);
					this.panelmodelArea.add(j);
				} else {
					String strTemp = String.format("%s (%1.2f)", key, d);
					JLabel j = new JLabel(strTemp,JLabel.CENTER);
					j.setBorder(BorderFactory.createLineBorder(new Color((float)(1.0-d),(float)d,(float)0.0)));
					j.setForeground(new Color((float)(1.0-d),(float)d,(float)0.0));
					this.panelmodelArea.add(j);
					cc *= d;
				}
			} else {
				String strTemp = String.format("%s", key);
				JLabel j = new JLabel(strTemp,JLabel.CENTER);
				j.setBorder(BorderFactory.createLineBorder(Color.BLUE));
				j.setForeground(Color.BLUE);
				this.panelmodelArea.add(j);
			}
		}
		
		eventmemory_size_label = new JLabel("  Events recorded: " + this.server.getEventMemoryNum() + "   ");
		eventmemory_num_label = new JLabel("  Event types: " + this.server.getEventMemorySize() + "   ");
		state_label = new JLabel("  State: " + this.server.getStatusString() + "   ");
		ordering_label = new JLabel("  Ordering: " + this.server.getOrderingString() + "   ");
		cumulativeconfidence_label = new JLabel("  Confidence: " + String.format("%1.2f", cc) + "   ");
		
		panelstateArea.add(state_label);
		panelstateArea.add(ordering_label);
		panelstateArea.add(eventmemory_size_label);
		panelstateArea.add(eventmemory_num_label);
		panelstateArea.add(cumulativeconfidence_label);
		
		getContentPane().validate();
    	getContentPane().repaint();
	}

	public void mouseClicked(java.awt.event.MouseEvent e) {
		String ibname = ((ImgButton)e.getSource()).getName();
		String DSname = this.dataSetname_field.getText();
		if( ((ImgButton)e.getSource()).getActivated() == false) {
			return;
		}
		switch (ibname) {
		case start_exploit:
			server.ControlConditionMonitor(ActionType.StartExploitaion);
			break;
		case stop_exploit:
			server.ControlConditionMonitor(ActionType.StopExploitation);
			break;
		case clear_data:
			server.ControlConditionMonitor(ActionType.ClearData);
			break;
		case start_recording:
			server.ControlConditionMonitor(ActionType.StartRecording);
			break;
		case stop_recording:
			server.ControlConditionMonitor(ActionType.StopRecording);
			break;
		case write_data:
			server.setDSname(DSname);
			server.ControlConditionMonitor(ActionType.WriteData);
			break;
		case load_data:
			server.setDSname(DSname);
			server.ControlConditionMonitor(ActionType.LoadData);
			break;
		case learn_data:
			server.ControlConditionMonitor(ActionType.LearnData);
			break;
		case quit_program:
			System.out.println("Shutting down program...");
			System.exit(0);
		default:
			System.out.println("FAILED");
			break;
		}
		this.setanswer();
	}
	
	public void mouseEntered(java.awt.event.MouseEvent e) { }
	public void mouseExited(java.awt.event.MouseEvent e) { }
	public void mousePressed(java.awt.event.MouseEvent e) {	}
	public void mouseReleased(java.awt.event.MouseEvent e) { }

	public void setanswer() {
		if(server.getState() == StateType.Idle){
			start_exploit_button.setActivated(true);
			start_recording_button.setActivated(true);
			clear_data_button.setActivated(true);
			stop_exploit_button.setActivated(false);
			stop_recording_button.setActivated(false);
			write_data_button.setActivated(true);
			load_data_button.setActivated(true);
			learn_data_button.setActivated(true);
			quit_button.setActivated(true);
		}
		if(server.getState() == StateType.Exploitation){
			start_exploit_button.setActivated(false);
			stop_exploit_button.setActivated(true);
			start_recording_button.setActivated(false);
			stop_recording_button.setActivated(false);
			clear_data_button.setActivated(false);
			write_data_button.setActivated(false);
			load_data_button.setActivated(false);
			learn_data_button.setActivated(false);
			quit_button.setActivated(true);
		}
		if(server.getState() == StateType.Recording){
			start_exploit_button.setActivated(false);
			stop_exploit_button.setActivated(false);
			start_recording_button.setActivated(false);
			stop_recording_button.setActivated(true);
			clear_data_button.setActivated(false);
			write_data_button.setActivated(false);
			load_data_button.setActivated(false);
			learn_data_button.setActivated(false);
			quit_button.setActivated(true);
		}
		update_panel_CM();
	}

	public void run() {
		generate_UI();
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public void setServer(ConditionMonitoringModel server) {
		this.server = server;
	}

	@Override
	public void modelchanged() {
		update_panel_CM();
	}
}