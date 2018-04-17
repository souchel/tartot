package team23.tartot.core;

import java.io.Serializable;

public enum Suit implements Serializable{
	TRUMP("t"),
	SPADE("s"), 
	HEART("h"), 
	CLUB("c"), 
	DIAMOND("d"); 	
	
	private String s;
	
	private Suit(String s)
	{
		this.s = s;
	}
	
	public String toString()
	{
		return s;
	}

}
