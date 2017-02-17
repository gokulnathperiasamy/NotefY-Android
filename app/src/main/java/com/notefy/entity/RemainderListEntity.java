package com.notefy.entity;

import java.util.List;

public class RemainderListEntity {
	
	private String contactID;
	private String contactName;
	private List<String> remainderMessage;
	private String numberOfMessages;
	
	public String getContactID() {
		return contactID;
	}
	
	public void setContactID(String contactID) {
		this.contactID = contactID;
	}
	
	public String getContactName() {
		return contactName;
	}
	
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	public List<String> getRemainderMessage() {
		return remainderMessage;
	}
	
	public void setRemainderMessage(List<String> remainderMessage) {
		this.remainderMessage = remainderMessage;
	}
	
	public String getNumberOfMessages() {
		return numberOfMessages;
	}
	
	public void setNumberOfMessages(String numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

}
