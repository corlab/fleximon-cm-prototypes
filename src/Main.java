import GUI.Graphicals;
import Model.ConditionMonitoringModel;
import RSB.ServerInput;

/**
 * 
 */


/**
 * @author tschuerm
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.append("Prep pending ");
//		IOLog log = new IOLog();
		System.out.append(".");
		Graphicals g = new Graphicals();
		System.out.append(".");
		ConditionMonitoringModel serv = new ConditionMonitoringModel();
		serv.setModellistener(g);
		System.out.append(".");
		//MonitoringServer serverout = new MonitoringServer();
		System.out.append(".");
		g.setServer(serv);
		System.out.append(".");
		ServerInput input = new ServerInput(serv);
		System.out.append(".");
		Thread t1 = new Thread(g);
		System.out.append(".");
		Thread t2 = new Thread(serv);
		System.out.append(".");
		Thread t3 = new Thread(input);
		System.out.append(".");
		//serverout.init();
		//Thread t4 = new Thread(serverout);
		System.out.append("done\n");
		t1.start();
		t2.start();		
		t3.start();
		//t4.start();
	}
}
