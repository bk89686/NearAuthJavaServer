package com.humansarehuman.blue2factor.authentication.tracking;
import java.util.*;
import java.util.stream.Collectors;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;

public class DeviceConnectionTracking {
	
	class ConnectedAndType {
		private boolean connected;
		private ConnectionType connectionType;
		public ConnectedAndType(boolean connected, ConnectionType connectionType) {
			super();
			this.connected = connected;
			this.connectionType = connectionType;
		}
		public boolean isConnected() {
			return connected;
		}
		public void setConnected(boolean connected) {
			this.connected = connected;
		}
		public ConnectionType getConnectionType() {
			return connectionType;
		}
		public void setConnectionType(ConnectionType connectionType) {
			this.connectionType = connectionType;
		}
		
		
	}


	public ArrayList<ConnectionLogDbObj> getGlobalConnectionTimeline(ArrayList<ArrayList<ConnectionLogDbObj>> allConnLogs) {
		DataAccess dataAccess = new DataAccess();
		HashMap<String, ConnectedAndType> connectionMap = new HashMap<>();
	    // 1. Flatten all lists into one master list
	    List<ConnectionLogDbObj> allEvents = allConnLogs.stream()
	            .flatMap(List::stream)
	            .collect(Collectors.toList());

	    // 2. Sort by timestamp
	    allEvents.sort(Comparator.comparing(ConnectionLogDbObj::getEventTimestamp));

	    ArrayList<ConnectionLogDbObj> globalTimeline = new ArrayList<>();

	    // 3. Process the timeline
	    ConnectedAndType currConn;
	    ConnectedAndType previousConn;
	    for (ConnectionLogDbObj event : allEvents) {
	    	currConn = new ConnectedAndType(event.isConnected(), event.getConnectionType());
	    	if (event.getConnectionId() != null) {
		    	if (!connectionMap.containsKey(event.getConnectionId())) {
		    		dataAccess.addLogSynchronous("creating new object for" + event.getConnectionId(), 
		    				LogConstants.TRACE);
		    		connectionMap.put(event.getConnectionId(), currConn);
		    	} else {
		    		previousConn = connectionMap.get(event.getConnectionId());
		    		if (previousConn.connected != currConn.connected) {
		    			if (currConn.connected) {
		    				if (allOthersAreDisconnected(connectionMap, event.getConnectionId())) {
		    					globalTimeline.add(createGlobalEvent(event));
		    				} else {
		    					dataAccess.addLogSynchronous(event.getConnectionId() + 
					            		" was already connected with another device", LogConstants.TRACE);
		    				}
		    			} else {
		    				if (allOthersAreDisconnected(connectionMap, event.getConnectionId())) {
		    					globalTimeline.add(createGlobalEvent(event));
		    				} else {
		    					dataAccess.addLogSynchronous("still connected with another device", 
		    							LogConstants.TRACE);
		    				}
		    			}
		    			connectionMap.put(event.getConnectionId(), currConn);
		    		} else {
		    			if (previousConn.getConnectionType() != null || currConn.getConnectionType() != null) {
			    			if (previousConn.getConnectionType() == null || currConn.getConnectionType() == null ||	
			    					!previousConn.getConnectionType().equals(currConn.getConnectionType())) {
				    			dataAccess.addLogSynchronous(event.getConnectionId() + 
					            		" changed connectionType to " + currConn.getConnectionType().toString(), 
					            		LogConstants.TRACE);
				    			globalTimeline.add(createGlobalEvent(event));
				    			connectionMap.put(event.getConnectionId(), currConn);
				    		}
			    		}
			    	}
		    	}
	    	} else {
	    		dataAccess.addLog("fix me", LogConstants.ERROR);
	    	}
	    }
	    return globalTimeline;
	}
	
	private boolean allOthersAreDisconnected(HashMap<String, ConnectedAndType> connMap, String currConnId) {
		boolean allDisconn = true;
		for (Map.Entry<String, ConnectedAndType> entry : connMap.entrySet()) {
		    if (!entry.getKey().equals(currConnId)) {
		    	if (entry.getValue().connected) {
		    		allDisconn = false;
		    		break;
		    	}
		    }
		}
		return allDisconn;
	}

	// Helper to create the new "Global" object
	private ConnectionLogDbObj createGlobalEvent(ConnectionLogDbObj original) {
		new DataAccess().addLog("new central event: connected: " + original.isConnected() + " at " + original.getEventTimestamp() + 
				" based on change in connection: " + original.getConnectionId(), LogConstants.TRACE);
	    return new ConnectionLogDbObj(
	    	original.getConnectionId(),
	    	original.getDeviceId(),
	        original.isConnected(), 
	        original.getEventTimestamp(),
	        "GLOBAL_SUMMARY",
	        "GLOBAL_ID",
	        ConnectionType.PROX
	    );
	}
}
