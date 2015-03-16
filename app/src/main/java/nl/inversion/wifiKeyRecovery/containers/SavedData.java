package nl.inversion.wifiKeyRecovery.containers;

import java.util.ArrayList;
import java.util.List;

public class SavedData {
	final String TAG =  this.getClass().getName();
	
	private List<NetInfo> tWifiPasswords = new ArrayList<NetInfo>();
	
	private String dateTime = "";
	private boolean areWeRooted = false;
	private int textSize;
	
	public void setTextSize(int size) {
		textSize = size;
	}
	
	public int getTextSize() {
		return textSize;
	}
	
	public String getDateTime() {
		return dateTime;
	}


	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}


	public List<NetInfo> getWifiPasswordList() {
		return tWifiPasswords;
	}

	public boolean getAreWeRooted() {
		return areWeRooted;
	}


	public void setAreWeRooted(boolean areWeRooted) {
		this.areWeRooted = areWeRooted;
	}
	
	
	public void setWiFiPasswordList(List<NetInfo> l){
		tWifiPasswords = l;
	}	
}
