package Model;

// Author: Klaus Neumann, 2014

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jsat.clustering.EMGaussianMixture;
import jsat.linear.DenseVector;
import jsat.linear.Vec;

import org.apache.commons.io.IOUtils;

import Enum.ActionType;
import Enum.ActivityType;
import Enum.StateType;

import rsb.AbstractEventHandler;
import rsb.Event;

/*
modi
----

idle // dieser modus macht im wesentlichen nicht, nimmt keine daten auf und schreibt auch nichts auf den rsb scope
recording // dieser modus füllt eine interne variable mit daten aus rsb
exploitation // dieser modus nutzt gmms um confidenzsignale auf den rsb scope zu schreiben

control signals
---------------

idle / start exploitation // begin des exploitation modus. macht nur sinn, wenn zuvor gelernt wurde (nur wählbar im idle modus) 
exploitation / stop exploitation -> idle // beende den exploitation modus (nur wählbar im exploitation modus, wechselt in den idle modus)

idle, exploitation, recording / state // rückgabe des aktuellen zustands

idle / start recording // wechsel in den recording modus.  in diesem modus wird eine interne variable mit daten gefüllt
recording / stop recording -> idle // beende den recording modus (nur wählbar im recording modus, wechselt in den idle modus)

idle / change data name // setzen des internen datensatznamens, default: DSfleximon
idle / clear data // löschen des internen datensatzes
idle / load data // laden der textdatei mit internen datensatznamen und abspeichern in internen datensatz
idle / write data // schreiben der internen datei auf die festplatte
idle / learn data // trainieren des gmms mit internen datensatz

exploitation, recording / idle // wechsel in den idle modus
*/

public class ConditionMonitoringModel extends AbstractEventHandler implements Runnable {
		
	private Hashtable<String,ArrayList<Double>> EventMemory = new Hashtable<String,ArrayList<Double>>(); //stores recorded time intervals
	private Hashtable<String,EMGaussianMixture> GMMs = new Hashtable<String,EMGaussianMixture>(); //stores GMMs
	private Hashtable<String,Double> GMMscales = new Hashtable<String,Double>(); //stores highest value for respective GMM (normalization)
	private Hashtable<String,Double> ConfidenceValueMemory = new Hashtable<String,Double>(); //stores confidence values in [0,1]
	private ArrayList<String> RequestOrder = new ArrayList<String>(); //stores order of incoming Events
	private ActivityType ActiveOrdering = ActivityType.Inactive; //flag for ordering process
	private ActivityType ActiveRecording = ActivityType.Inactive; //flag for recording
	private ActivityType ActiveExploitation = ActivityType.Inactive; //flag for exploitation
	private double TimeOld = 0.0; //variable to calculate time intervals between incoming events
	private double TimeNew = 0.0; //variable to calculate time intervals between incoming events
	private StateType State = StateType.Idle; //flag for current state
	private double Threshold = 0.01; //threshold for distinguishing between failure and normal state
	private ModelListener modellistener; //a listener to update the GUI
	private  String DSname = "DSfleximon";
	
	public void ControlConditionMonitor(ActionType action) {
		//this method represents the condition monitoring state machine
		//states are idle, recording, and exploitation
    	if(this.State.equals(StateType.Idle)) {
	    	switch (action) {
				case StartExploitaion:  this.State = StateType.Exploitation; break;
				case StartRecording:	this.State = StateType.Recording; break;
				case ClearData:		 	this.ClearDS(); break;
				case LoadData:		 	try {this.LoadDataSet(this.DSname);}
				  						catch (IOException e) {e.printStackTrace();}
										break;
				case WriteData: 		this.writeFile(this.DSname); break;
				case LearnData:   	 	this.LearnGMMs(); break;
				default:
					break;
			}
    	}
    	else if(this.State.equals(StateType.Recording)) {
			if(action == ActionType.StopRecording) {
				this.State = StateType.Idle;
			} 
		} else if(this.State.equals(StateType.Exploitation)) {
			if(action == ActionType.StopExploitation) {
				this.State = StateType.Idle;
			} 
		}
    }

	public void handleEvent(Event event) {
		//event handling method
		String EventName = nameEvent(event); //get the name of the incoming event
		
		//TODO: this "this.checkEvent(event).equals("request")" check should be changed at some point
		if(this.checkEvent(event).equals("request")) { //check if event is useful for condition monitoring
			if(this.State.equals(StateType.Exploitation)) {
				this.handleEvent_exploitation(event); //do this in exploitation mode
			} else if(this.State.equals(StateType.Recording)) {
				this.handleEvent_recording(event); //do this in recording mode
			} else { //otherwise ignore events
				System.out.println("event ignored (" + EventName + ").");
				this.ActiveRecording = ActivityType.Inactive; //wait one event for recording
				this.ActiveExploitation = ActivityType.Inactive; //wait one event for exploitation
			}
		
			//build a request order if ActiveOrdering is Active (used by the GUI)
			if(this.ActiveOrdering==ActivityType.Active && !this.RequestOrder.contains(EventName)) {
	    		this.RequestOrder.add(EventName);
	    		System.out.println("added " + EventName + " to request order.");
	    	}
	        
			//TODO: eliminate hardcoding
			//TODO: the beginning of the order should be detected by observing an event with new RFID and not hardcoded!
	        if(EventName.equals("Stopper4")) {
	        	if(this.ActiveOrdering == ActivityType.Inactive) {
	        		this.ActiveOrdering = ActivityType.Active;
	        	} else if(this.ActiveOrdering == ActivityType.Active) {
	        		this.ActiveOrdering = ActivityType.Finished;
	        		System.out.println("ordering successfully finished.");
	        	}	        	
	        }
		}
        
		this.modellistener.modelchanged(); //change event for the GUI
	}	
	
	public void handleEvent_exploitation(Event event) {
		//handle event in exploitation mode
		//mainly the time inbetween two events is compared to one-dimensional GMMs
        TimeNew = event.getMetaData().getCreateTime();
        	
        if(TimeOld == 0) { 
        	TimeOld = TimeNew; 
        	return; 
        }
        
        String EventName = nameEvent(event);
        
        if(this.ActiveExploitation==ActivityType.Active) {
	        if(GMMs.containsKey(EventName)) {
	        	double d = GMMs.get(EventName).pdf((TimeNew-TimeOld)/100000.0) / GMMscales.get(EventName);
	        	d = Math.pow(d, 1.0/1.0); // weaken the deviation from the GMM
	        	this.ConfidenceValueMemory.put(EventName,d);
	        } else {
	        	System.out.println("key for event (" + EventName + ") not available");
	        }
	        
	        if(EventName.equals("Stopper4")) {
	        	for ( String key : this.ConfidenceValueMemory.keySet() ) {
	        		this.ConfidenceValueMemory.put(key,-1.0);
	        	}        	
	        }
        } else { //wait one event for exploitation
			this.ActiveExploitation = ActivityType.Active;
			System.out.println("exploitation started.");
		}
        	
        this.TimeOld = this.TimeNew;
        System.out.println("event analyzed (" + EventName + ").");
    }

	public void handleEvent_recording(Event event) {   
		//handle event in recording mode
		//mainly the time inbetween two events is recorded and saved in EventMemory
		TimeNew = event.getMetaData().getCreateTime();
    	
        if(TimeOld == 0) { 
        	TimeOld = TimeNew; 
        	return; 
        }
        
		if(this.ActiveRecording==ActivityType.Active) {  	
	        String EventName = nameEvent(event);
	        if(!this.EventMemory.containsKey(EventName)) {
	        	ArrayList<Double> EventsTimeTemp = new ArrayList<Double>();
	           	EventsTimeTemp.add((this.TimeNew-this.TimeOld)/100000.0);
	           	this.EventMemory.put(EventName, EventsTimeTemp);
	        } else {
	        	ArrayList<Double> EventsTimeTemp = this.EventMemory.get(EventName);
	           	EventsTimeTemp.add((this.TimeNew-this.TimeOld)/100000.0);
	           	this.EventMemory.put(EventName, EventsTimeTemp);
	        }
	        System.out.println("event recorded (" + EventName + ").");
		} else { //wait one event for recording
			this.ActiveRecording = ActivityType.Active;
			System.out.println("recording started.");
		}
		
		this.TimeOld = this.TimeNew;
    }
    
    public void writeFile(String DSname) { //write a file
    	System.out.println("prepare to save data to file...");
        String fnstr = "";
        for ( String key : this.EventMemory.keySet() ) {
        	fnstr += key + " ";
        	ArrayList<Double> EventsTimeTemp = this.EventMemory.get(key);
        	for (double d : EventsTimeTemp) {
        		fnstr += d + " ";
        	}
        	fnstr += "\n";
        	System.out.println("key (" + key + ") saved...");
        }        	
    	FileOutputStream fos = null;
    	File file;
    	file = new File(DSname);
    	try {
    	fos = new FileOutputStream(file);
    	if (!file.exists()) {
    	   file.createNewFile();
    	}
    	byte[] bytesArray = fnstr.getBytes();
    	fos.write(bytesArray);
    	fos.flush();
    	fos.close();
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally { }
    	System.out.println("data saved to file successfully.");
    }
    
    public void LoadDataSet(String DSname) throws IOException {
    	System.out.println("preparing to load data...");
		FileInputStream inputStream = new FileInputStream(DSname);
		String filecontent;
	    try {filecontent = IOUtils.toString(inputStream);}
	    finally {inputStream.close();}
		
		String delimrow = "\n";
		String[] tokens = filecontent.split(delimrow);
		int numGMMs = tokens.length;
		
		for(int i=0; i<numGMMs; i++) {
			ArrayList<Double> ds = new ArrayList<Double>(); 
			ds.clear();
			String delimcolumn = " ";
			String[] elemTemp = tokens[i].split(delimcolumn);
			int numSamples = elemTemp.length-1;
			for(int j=1; j<numSamples+1; j++) {
				ds.add(Double.parseDouble(elemTemp[j]));
			}
			this.EventMemory.put(elemTemp[0],ds);
			System.out.println("data loaded for GMM " + Integer.toString(i) + " (" + elemTemp[0] + ") ...");
		}
		System.out.println("data loaded successfully.");
	}
	
	public void LearnGMMs() {	
		System.out.println("preparing to learn data with GMMs...");
		for ( String key : this.EventMemory.keySet() ) {
			EMGaussianMixture gmm = new EMGaussianMixture();
			ArrayList<Double> EMTemp = this.EventMemory.get(key);
			List<Vec> VecList = new ArrayList<Vec>();
			for (Double d : EMTemp) {
				VecList.add(DenseVector.toDenseVec(d));
			}
			System.out.println(key);
			//TODO: check success and stopping of learning "gho" data set is problematic (maybe because only one gaussian per data point is created and has no defined sigma)
			gmm.setUsingData(VecList);
			this.GMMs.put(key, gmm);	
			
			double maxval = 0;
			for (Vec s : gmm.getMeans()) {
				if(maxval < gmm.pdf(s)){
					maxval = gmm.pdf(s);
				}
			}
			this.GMMscales.put(key, maxval);
			
			System.out.println("GMM for " + key + " learned...");
		}
		System.out.println("data learned successfully.");
	}
	
	public void ClearDS() {
		//clear the recorded/loaded data internally
		System.out.println("preparing to clear data...");
		this.EventMemory.clear();
		System.out.println("data cleared successfully.");
	}
    
	public String checkEvent(Event event) {
		// /fleximon/schraubmodul/server/request/  eg. Stopper1 etc.
		String delimrow = "/";
		String[] tokens = event.getScope().toString().split(delimrow);
		if(tokens.length == 6 && tokens[tokens.length-2].equals("request")) {
			return "request";
		} else {
			return "unknown";
		}
	}
	
	public String nameEvent(Event event) {
		String delimrow = "/";
		String[] tokens = event.getScope().toString().split(delimrow);
        return tokens[tokens.length-1];
	}

	public void run() { }
	
	public Hashtable<String, Double> getConfidenceValueMemory() {
		return ConfidenceValueMemory;
	}

	public void setConfidenceValueMemory(
			Hashtable<String, Double> confidenceValueMemory) {
		ConfidenceValueMemory = confidenceValueMemory;
	}

	public StateType getState() {
		return State;
	}

	public void setState(StateType state) {
		State = state;
	}

	public double getThreshold() {
		return Threshold;
	}

	public int getEventMemorySize() {
		return this.EventMemory.size();
	}
	
	public int getEventMemoryNum() {
		int len = 0;
		for ( String key : this.EventMemory.keySet() ) {
			len += this.EventMemory.get(key).size();
		}
		return len;
	}
	
	public String getStatusString() {
		if(this.State == StateType.Exploitation) {
			return "Exploitation";
		} else if(this.State == StateType.Recording) {
			return "Recording";
		} else {
			return "Idle";
		}
	}
	
	public double getCumulativeConfidence() {
		double cc = 1.0;
		for ( String key : this.ConfidenceValueMemory.keySet() ) {
			cc *= this.ConfidenceValueMemory.get(key);
		}
		return cc;
	}
	
	public String getOrderingString() {
		if(this.ActiveOrdering == ActivityType.Active) {
			return "active (" + this.RequestOrder.size() + ")";
		} else if(this.ActiveOrdering == ActivityType.Inactive) {
			return "waiting";
		} else {
			return "finished";
		}
	}
	
	public void setThreshold(double threshold) {
		Threshold = threshold;
	}

	public ArrayList<String> getRequestOrder() {
		return RequestOrder;
	}

	public void setRequestOrder(ArrayList<String> requestOrder) {
		RequestOrder = requestOrder;
	}
	
    public ModelListener getModellistener() {
		return modellistener;
	}

	public void setModellistener(ModelListener modellistener) {
		this.modellistener = modellistener;
	}
	
	public String getDSname() {
		return DSname;
	}

	public void setDSname(String dSname) {
		DSname = dSname;
	}
}

    

