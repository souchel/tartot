package metier;

public enum Suit {
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
