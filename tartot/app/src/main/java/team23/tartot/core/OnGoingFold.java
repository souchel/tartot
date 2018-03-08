package team23.tartot.core;

import java.util.ArrayList;

// this class doesn't check which player is adding a card!!!
public class OnGoingFold extends Deck{
	private Suit askedSuit;
	private Card winingCard;
	//TODO change Player to his username
	private Player winingPlayer;
	private Player excused;
	
	public OnGoingFold() {
		cardList = new ArrayList<Card>(4) ;
		excused = null;
	}
	
	//should get suit on the first card but if it doesn't it'll take suit from the following card
	public void addCard(Card addedCard, Player player) {
		if (addedCard.isEqual(new Card(Suit.TRUMP, 22))){
			excused = player;
		}
		if (askedSuit == null && addedCard.isEqual(new Card(Suit.TRUMP, 22)) == false) {
			askedSuit = addedCard.getSuit();
		}
		super.addCard(addedCard);
		if (this.isBetterCard(addedCard) == true){
			winingCard = addedCard;
			winingPlayer = player;
		}
		cardList.set(player.getPosition(), addedCard);
		
	}
	

	//return false in an not planned case
	//return true if winingPlayer isn't set
	private boolean isBetterCard(Card addedCard) {
		if (winingCard == null) {
			if (addedCard.isEqual(new Card(Suit.TRUMP, 22)) == false) {
				return true;
			}else return false;
		}
		Suit addedCardSuit = addedCard.getSuit();
		int addedCardValue = addedCard.getValue();
		Suit winingCardSuit = winingCard.getSuit();
		int winingCardValue = winingCard.getValue();
		if (addedCardSuit == Suit.TRUMP && addedCardValue == 22) {
			return false;
		}
		//in case we start fold with the excuse (already seen irl)
		if (winingCardSuit == Suit.TRUMP && winingCardValue == 22) {
			return true;
		}
		if (addedCardSuit == Suit.TRUMP) {
			if (winingCardSuit != Suit.TRUMP) {
				return true;
			} else if (addedCardValue > winingCardValue) {
				return true;
			} else return false;
		} else if (addedCardSuit != winingCardSuit) {
			return false;
		} else if (addedCardSuit == winingCardSuit) {
			if (addedCardValue < winingCardValue) {
				return false;
			} else if (addedCardValue > winingCardValue) {
				return true;
			}
		}
		return false;
	}
	
	public Suit getSuit() {
		return askedSuit;
	}
	
	public Player getWiningPlayer() {
		return winingPlayer;
	}
	
	public Card getWiningCard() {
		return winingCard;
	}
	
	public ArrayList<Card> getCardList() {
		return cardList;
	}

	public Player getExcused() {
		return excused;
	}
	
	public void setExcused(Player player) {
		excused = player;
	}
}
