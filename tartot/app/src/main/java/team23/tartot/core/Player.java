package team23.tartot.core;

import java.util.List;
import java.util.ArrayList;

public class Player {
	private String username;
	private Team team;
	private Deck hand;
	private Deck deck;
	private int position;
	private String mParticipantId;

	public Player(String username, int position, String participantId)
	{
		this.username = username;
		this.mParticipantId = participantId;
		//quand les players sont crees ils n ont pas encore d equipe d ou ils sont dans l equipe none
		team = Team.NONE;
		deck = new Deck();
		hand = new Deck();
		this.position = position ;
		//TODO
		//hand?
		//position?
	}

	public Player(String username)
	{
		this.username = username;
		//quand les players sont crees ils n ont pas encore d equipe d ou ils sont dans l equipe none
		team = Team.NONE;
		deck = new Deck();
		hand = new Deck();
		//TODO
		//hand?
		//position?
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	public String getUsername()
	{
		return username ;
	}
	public Team getTeam()
	{
		return team;
	}
	public Deck getHand()
	{
		return hand;
	}
	public Deck getDeck()
	{
		return deck;
	}
	public void setHand(ArrayList<Card> cards) {
		hand.setDeck(cards);
	}
	public int getPosition() {
		return position;
	}





	//methods to check existing card in deck (or count trump at the end)
	public boolean haveSuit(Suit suit) {
		for (Card card : hand.getCardList()) {
			if (card.getSuit() == suit) {
				return true;
			}
		} return false;
	}
	public boolean haveTrump() {
		for (Card card : hand.getCardList()) {
			if (card.getSuit() == Suit.TRUMP) {
				return true;
			}
		} return false;
	}
	public boolean haveHigherTrump(int value) {
		for (Card card : hand.getCardList()) {
			if (card.getSuit() == Suit.TRUMP) {
				if (card.getValue() > value && card.getValue() != 22) {
					return true;
				}
			}
		} return false;
	}
	public boolean haveHead() {
		for (Card card : hand.getCardList()) {
			if (card.getSuit() != Suit.TRUMP && card.getValue() > 10) {
				return true;
			} else if (card.getSuit() == Suit.TRUMP && card.getValue() == 22){
				return true;
			}
		} return false;
	}
	public int countTrump() {
		int total = 0;
		for (Card card : hand.getCardList()) {
			if (card.getSuit() == Suit.TRUMP) {
				total += 1;
			}
		} return total;
	}

	public boolean isEqual(Player otherPlayer) {
		if (username == otherPlayer.getUsername() && position == otherPlayer.getPosition()) {
			return true;
		} else {
			return false;
		}
	}
}
