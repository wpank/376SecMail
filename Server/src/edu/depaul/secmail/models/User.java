package edu.depaul.secmail.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.SecMailServer;
import edu.depaul.secmail.UserStruct;

public class User implements DBModel {

	private int userID;
	private String userAddress;
	private String userPassword;
	private String userSalt;
	
	// Constructor for instantiating User object from the DB, where it already has a userID
	public User(int userID, String userAddress, String userPassword){
		this.userID = userID;
		this.userAddress = userAddress;
		this.userPassword =userPassword;
		try {
			setSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	// Constructor for creating a user object to write to the DB
	public User(String userAddress, String userPassword){
		this.userAddress = userAddress;
		this.userPassword =userPassword;
		try {
			setSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		hashPassword();
	}

	public String getUserAddress() {
		return userAddress;
	}

	public String getUserSalt() {
		return userSalt;
	}

	@Override
	public int getID() {
		return userID;
	}

	@Override
	public String toString() {
		return "User [userID=" + userID + ", userAddress=" + userAddress + ", userPassword=" + userPassword
				+ ", userSalt=" + userSalt + "]";
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
		
		// Only write to the database if the user isn't already stored in it 
		if (userID == 0){
			
			String sql = "INSERT INTO user VALUES (0,  \""+userAddress + "\", \"" + userPassword + "\", \"" + userSalt + "\")";
			
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
				
				// set userID from newly inserted row
				if (rs.next()){
					System.out.println("Insert User Success");
					userID = rs.getInt(1);
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
	
	// Generates a salt and saves it 
	private void setSalt() throws NoSuchAlgorithmException{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		userSalt = salt.toString();
	}
	
	// hashes the password
	private void hashPassword(){
		String generatedPassword = null;
		try{
			MessageDigest md =MessageDigest.getInstance("MD5");
			md.update(userSalt.getBytes());
			byte[] passwordBytes = md.digest(userPassword.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i =0; i < passwordBytes.length; i++){
				sb.append(Integer.toString((passwordBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
			userPassword = generatedPassword;
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}

	
	// returns a userStruct object
	public UserStruct toUserStruct(){
		return new UserStruct(userAddress + "@" + SecMailServer.getGlobalConfig().getDomain());
	}
}