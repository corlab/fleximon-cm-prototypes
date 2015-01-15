package RSB;
import Model.ConditionMonitoringModel;
import rsb.Event;
import rsb.Factory;
import rsb.InitializeException;
import rsb.Listener;
import rsb.RSBException;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.automation.CoordinationStateType.CoordinationState;

/**
 * @author tschuerm
 * 
 */
public class ServerInput implements Runnable {
	/**
	 * the RSB scope
	 */
	//private Scope scope = new Scope("/fleximon/conditionmonitoring");
	private Scope scope = new Scope("/fleximon/schraubmodul/server/request/");
	
	private ConditionMonitoringModel CM;
	
	// private CommunicationControl commcontr = null;
	// private ServerOutput serverout;

	/**
	 * Constructor.
	 * @param log2 
	 */
	public ServerInput(ConditionMonitoringModel CM) {
		super();
		this.CM = CM;
	}

	/**
	 * More Comments in cwar1-processmodell.rsbcommunication.serverinput
	 * (non-Javadoc)
	 * 
	 * @see subsystem.SubSystem#run() Here: Basic Setup of the RSB-input for the
	 *      ProcessḾodel.
	 */
	public void run() {

		// RSB stuff
		final ProtocolBufferConverter<rst.automation.ScrewType.Screw> converterScrew = new ProtocolBufferConverter<rst.automation.ScrewType.Screw>(
				rst.automation.ScrewType.Screw.getDefaultInstance());
		DefaultConverterRepository.getDefaultConverterRepository().addConverter(converterScrew);

		final ProtocolBufferConverter<rst.devices.rfid.TagType.Tag> converterTag = new ProtocolBufferConverter<rst.devices.rfid.TagType.Tag>(
				rst.devices.rfid.TagType.Tag.getDefaultInstance());
		DefaultConverterRepository.getDefaultConverterRepository().addConverter(converterTag);
		
		final ProtocolBufferConverter<CoordinationState> converterCoordination = new ProtocolBufferConverter<CoordinationState>(
				CoordinationState.getDefaultInstance());
		DefaultConverterRepository.getDefaultConverterRepository().addConverter(converterCoordination);
		
		final ProtocolBufferConverter<rst.automation.ConditionMonitoring.CMonitoring> converterCM = new ProtocolBufferConverter<rst.automation.ConditionMonitoring.CMonitoring>(
				rst.automation.ConditionMonitoring.CMonitoring.getDefaultInstance());
		DefaultConverterRepository.getDefaultConverterRepository().addConverter(converterCM);

		final Factory factory = Factory.getInstance();

		Listener listener;
		
		try {
			listener = factory.createListener(scope);
			listener.addHandler(this.CM, true);

			listener.activate();

		} catch (InitializeException ex) {
			System.out.println("EXCEPTION IN LISTENER ACTIVATION");
		} catch (RSBException | InterruptedException ex) {
			System.out.println("EXCEPTION IN LISTENER ACTIVATION");
		}

		// reads the incoming objects
		// and starts the desired action
//		Event request;
//
//		while (true) {
//			try {
//				// Handling of Queues is a BLOCKING Call, there for the
//				// IF-Statements
//				
//				if (!handler.getQueue().isEmpty()) {
//					request = handler.getQueue().take();
////					log.write("ServerInput received ConditionMonitoring: ", request.toString());
//					set_data(request);
//				
//				}
//
//			} catch (InterruptedException e) {
//				
//				e.printStackTrace();
//			}
//
//		}
	}
}
