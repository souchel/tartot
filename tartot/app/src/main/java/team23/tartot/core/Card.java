package team23.tartot.core;

public class Card {
	private Suit suit;
	// la valeur de la carte 11 = valet etc si la carte n est pas un atout
	//le 22  d atout correspond a l excuse
	private int value;
	
	public Card(Suit s, int v)
	{
		//Attention rien n empeche de creer des cartes impossibles 
		suit = s;
		value = v;
	}
	public double pointValue()
	{
		//Attention si la carte n existe pas ex 15 de trefle la valeur est debile
		if (value > 10 && suit != Suit.TRUMP)
		{
			//Si c est une tete 
			return value - 10 + 0.5;
		}
		else if (suit == Suit.TRUMP && (value == 21 || value == 22 || value == 1))
		{
			//Si c est un bout
			return 4.5;
		}
		//les petites cartes
		return 0.5;
	}
	public int getValue()
	{
		return value;
	}
	public Suit getSuit()
	{
		return suit;
	}
	public String valueToString()
	{
		if ((suit == Suit.TRUMP && value != 22) ||(value <= 10))
		{
			return Integer.toString(value);
		}
		else
		{
			switch (value) 
			{
		      case 11: return "V"; 
		      case 12: return "C";
		      case 13: return "D";
		      case 14: return "R"; 
		      case 22: return "*"; 
		      default : return "Invalid";
		    }
		}		
	}
	
	public boolean isEqual(Card cardToCompare) {
		if (cardToCompare.getSuit() == suit && cardToCompare.getValue() == value) {
			return true;
		} else return false;
	}

	public byte[] toBytes(){

	}
}
