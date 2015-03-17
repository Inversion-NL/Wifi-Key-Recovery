package nl.inversion.wifiKeyRecovery.containers;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class SavedData {
	final String TAG =  this.getClass().getName();
	
	private List<NetInfo> tWifiPasswords = new ArrayList<NetInfo>();
	
	private String dateTime = "";
    private String searchQuery = "";
    private boolean searchBarOpen = false;
    private MenuItem mSearchActionMenuItem;
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

    public String getSearchQuery() {
        return searchQuery;
    }

    public boolean getIsSearchBarOpen() {
        return searchBarOpen;
    }

    public MenuItem getSearchActionMenuItem() {
        return mSearchActionMenuItem;
    }

    public void setIsSearchBarOpen(boolean searchBarOpen) {
        this.searchBarOpen = searchBarOpen;
    }

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setSearchActionMenuItem (MenuItem menuItem) {
        this.mSearchActionMenuItem = menuItem;
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
