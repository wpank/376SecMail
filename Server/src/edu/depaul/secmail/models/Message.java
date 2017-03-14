package edu.depaul.secmail.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.EmailStruct;

public class Message implements DBModel {

	private int messageID;
	private User sender;
	private ArrayList<User> recipients = new ArrayList<User>();
	private String subject;
	private String content;
	// Message Attachment
	private Date messageDate;
	private ArrayList<Tag> tags;
	private boolean hasRead;
	
	
	public Message(int messageID, User sender, User recipient, String subject, String content, Date messageDate){
		this.messageID = messageID;
		this.sender = sender;
		recipients.add(recipient);
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
		tags = new ArrayList<Tag>();
		hasRead = false;
	}
	
	public Message(User sender, User recipient, String subject, String content, Date messageDate){
		this.sender = sender;
		recipients.add(recipient);
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
	}

	public User getSender() {
		return sender;
	}

	public ArrayList<User> getRecipient() {
		return recipients;
	}
	
	public void addRecipient(User recipient){
		recipients.add(recipient);
	}

	@Override
	public String toString() {
		return "Message [messageID=" + messageID + ", sender=" + sender + ", recipient=" + recipients + ", subject="
				+ subject + ", content=" + content + ", messageDate=" + messageDate + ", tags=" + tags + ", hasRead="
				+ hasRead + "]";
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public ArrayList<Tag> getTags() {
		return tags;
	}
	
	public void addTag(Tag tag){
		tags.add(tag);
	}

	@Override
	public int getID() {
		return messageID;
	}

	@Override
	public void encrypt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void decrypt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dbWrite() {
		// the last parameter is an attachment BLOB field in the database
		String messageSqlQuery = "INSERT INTO message VALUES (0, \"" + subject + "\", + \""+content + "\", null)";
		System.out.println(messageSqlQuery);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = DBCon.getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.prepareStatement(messageSqlQuery, Statement.RETURN_GENERATED_KEYS);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			
			// Extract data from result set
			if (rs.next()){
				System.out.println("Insert message success");
				
				messageID = rs.getInt(1);
				
				// Write into notifications table for each message recipient
				for (User recipient : recipients){
					String messageRecipientSqlQuery = "INSERT INTO notification VALUES ("+ sender.getID() + ", " + recipient.getID() + ", " + rs.getInt(1) + ", null)";
					stmt = conn.prepareStatement(messageRecipientSqlQuery);
					int rsMR= stmt.executeUpdate();
					if (rsMR != 0){
						System.out.println("Insert message_recipient success");
					} else {
						System.out.println("Insert message_recipient fail");
					}
				}
				
			} else {
				System.out.println("Insert message fail");
			}
			
			// clean up connection
			stmt.close();
			conn.close();
		} catch(SQLException se){
			// handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e){
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try{
				if (stmt != null)
					stmt.close();
				} catch(SQLException se2){
				}
				try{
					if (conn != null) conn.close();
				} catch (SQLException se){
					se.printStackTrace();
				}
		}
	}
	
	
	public EmailStruct toEmailStruct(){
		EmailStruct emailStruct = new EmailStruct();
		for (User recipient : recipients){
			emailStruct.addRecipient(recipient.getUserAddress());
		}
		emailStruct.setBody(content);
		emailStruct.setSubject(subject);
		emailStruct.setID(messageID);
		return emailStruct;
	}

}
