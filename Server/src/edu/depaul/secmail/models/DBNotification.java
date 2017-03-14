package edu.depaul.secmail.models;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.Notification;
import edu.depaul.secmail.NotificationType;

public class DBNotification {
	
	private int notificationID;
	private User sender;
	private User recipient;
	private String subject;
	private int emailID;
	private Date sendDate;
	
	public DBNotification(User sender, User recipient, String subject, int emailID, Date sendDate) {
		this.sender = sender;
		this.recipient = recipient;
		this.subject = subject;
		this.emailID = emailID;
		this.sendDate = sendDate;
	}

	public User getSender() {
		return sender;
	}

	public User getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public int getEmailID() {
		return emailID;
	}

	public Date getSendDate() {
		return sendDate;
	}

	@Override
	public String toString() {
		return "Notification [sender=" + sender + ", recipient=" + recipient + ", subject=" + subject + ", emailID="
				+ emailID + ", sendDate=" + sendDate + "]";
	}
	
	public Notification toNotificatonStruct(){
		return new Notification(recipient.toUserStruct(), sender.toUserStruct(), NotificationType.NEW_EMAIL, Integer.toString(emailID), subject, sendDate);
	}
	
	public void dbWrite() {
		/// Only write to the database if the notification isn't already stored in it 
		if (notificationID == 0){
			
			String sql = "INSERT INTO notification VALUES (0,  \"" + sender.getID() + "\", \"" + recipient.getID() + "\", \"" + emailID + "\" , \"" + new java.sql.Date(sendDate.getTime()) + "\")";
			System.out.println(sql);
			java.sql.Connection conn = null;
			PreparedStatement stmt = null;
			
			try{
				// Open a Connection
				System.out.println("Connecting to database...");
				conn = DBCon.getRemoteConnection();
				
				// Execute query 
				System.out.println("Creating statement...");
				stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.executeUpdate();
				ResultSet rs = stmt.getGeneratedKeys();
				
				// set tagID from newly inserted row
				if (rs.next()){
					System.out.println("Insert Notification Success");
					notificationID = rs.getInt(1);
				}
				
				// Clean up connection
				stmt.close();
				conn.close();
			} catch (SQLException se){
				// handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e){
				// handle errors for Class.forName
				e.printStackTrace();
			} finally {
				// close resources
				try{
					if (stmt != null) stmt.close();
				} catch (SQLException se2){}
				try {
					if (conn != null) conn.close();
				} catch (SQLException se){
					se.printStackTrace();
				}
			}
		}
		
	}
	
	

}
