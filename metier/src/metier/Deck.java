package metier;

import java.util.ArrayList;
import java.util.List;

public class Deck {
	private Team team = null;
	private List<Card> cardList = new ArrayList<>();
	
	//TODO
	public Deck() {
		
	}
	
	//TODO
	public double countPoints() {
		double points = 0;
		for (Card card : cardList) {
			points += card.pointValue();
		}
		return points;
	}
	
	public int countOudlers() {
		int nbOudler = 0;
		for (Card card : cardList) {
			if (card.getSuit() == Suit.TRUMP) {
				int cardValue = card.getValue();
				if (cardValue == 1 || cardValue == 21 || cardValue == 22) {
					nbOudler += 1;
				}
			}
		}
		return nbOudler;
	}
	
	public void addCard(Card cardToAdd) {
		cardList.add(cardToAdd);
	}
	
	//if card not in the list, nothing happen
	//can remove many times the same card but it's not suppose to be possible
	public void removeCard(Card cardToRemove) {
		for (Card card : cardList){
			if (card == cardToRemove) {
				cardList.remove(card);
			}
		}
	}
	
	//TODO
	public void mergeDeck(Deck firstDeck, Deck secondDeck) {
		
	}
}
