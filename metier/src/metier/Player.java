package metier;

public class Player {
	private String username;
	private Team team;
	private Deck deck;
	
	public Player(String username)
	{
		this.username = username;
		//quand les players sont crees ils n ont pas encore d equipe d ou ils sont dans l equipe none
		team = Team.NONE;
		deck = new Deck();
	}
	public String getUsername()
	{
		return username ;
	}
	public Team getTeam()
	{
		return team;
	}
	
	
	
	
	
	//methods to check existing card in deck
	public boolean haveSuit(Suit suit) {
		for (Card card : deck.getCardList()) {
			if (card.getSuit() == suit) {
				return true;
			}
		} return false;
	}
	public boolean haveTrump() {
		for (Card card : deck.getCardList()) {
			if (card.getSuit() == Suit.TRUMP) {
				return true;
			}
		} return false;
	}
	public boolean haveHigherTrump(int value) {
		for (Card card : deck.getCardList()) {
			if (card.getSuit() == Suit.TRUMP) {
				if (card.getValue() > value) {
					return true;
				}
			}
		} return false;
	}
}
