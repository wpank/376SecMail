package edu.depaul.secmail;

import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpSession {


	protected static Map<String, Runnable> sessions = new HashMap<String, Runnable>(); //collection of all active sessions
	private static Map<String, Long> sessionLog = new HashMap<String, Long>();
	private static Object sessionLock = new Object(); //object used to lock access to both collections. Ensure only one thread at a time can modify both maps


	public static class sessionCleaner implements Runnable {
		//Executes a session clean every 60 seconds which will remove inactive sessions from collection
		public void run() {
			while (true) {
				try {
					Thread.sleep(60000);
					this.sessionClean();
				}
				catch (InterruptedException e) {} //TO DO -- figure out what to do here
			}
		}


		public void sessionClean() {
			List<String> oldKeys = new LinkedList<String>();

			Iterator<Entry<String, Long>> it = sessionLog.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Long> p = it.next();
				Long entryTime = p.getValue();
				String key = p.getKey();
				//add keys to be removed into temporary collection
				if ((new Date().getTime() - entryTime) > 600000) {
					oldKeys.add(key); //if more than 10 minutes since sessionID was last used, kill session
				}
			}
			for (String key : oldKeys) {
				HttpSession.remove(key);
			}
		}
	}



	public static MailServerConnection get(String id) {
		//get session variables for specified session id
		return (MailServerConnection)sessions.get(id);
	}


	public static String generateSessionID() {
		//generates unique session ID to be given to the client within a cookie
		SecureRandom r = new SecureRandom();
		return new BigInteger(130, r).toString(32);
	}


	public static String start(String username, Socket s, DHEncryptionIO io) {
		//generates new session id, adds to sessions collection, returns session id to client
		String id = generateSessionID();
		try {
			Thread newUserSession = new MailServerConnection(id, username, s, io);
			newUserSession.start();
			//ensure thread safe
			synchronized(sessionLock) {
				sessions.put(id, newUserSession);
				sessionLog.put(id, new Date().getTime());
			}

		}
		catch (Exception e) { return null; }
		return id;
	}


	public static void updateTime(String id) {
		if (sessionLog.containsKey(id)) sessionLog.put(id, new Date().getTime());
	}


	public static boolean isSet(String id) {
		//checks if a session is active for userid
		if (id == null) return false;
		if (sessions.containsKey(id)) return true;
		return false;
	}


	public static void remove(String id) {
		//make sure connections get closed
		((MailServerConnection)sessions.get(id)).close();
		//needs to be synchronized so multiple threads can execute without damaging the data
		synchronized(sessionLock) {
			//remove from session and session log
			sessions.remove(id);
			sessionLog.remove(id);
		}
	}
}
