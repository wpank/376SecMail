package edu.depaul.secmail.models;

public interface DBModel {

	// returns id within database
	public int getID();

	// wrapper for encrypting DBModel Object
	public void encrypt();

	// wrapper for decrypting DBModel Object
	public void decrypt();

	// calls insert statement to write object to DB
	public void dbWrite();


}
