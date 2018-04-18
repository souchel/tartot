package team23.tartot.core;

import java.io.Serializable;
import java.util.Random;

import java.util.ArrayList;
import java.util.List;

public class Deck implements Serializable {
	private Team team;
	protected ArrayList<Card> cardList = new ArrayList<>();
	
	public Deck() {
		
	}
	
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
	//bug if many times the the same card in deck but it's not suppose to happen
	public void removeCard(Card cardToRemove) {
		for (Card card : cardList){
			if (card.isEqual(cardToRemove)) {
				cardList.remove(card);
			}
		}
	}
	
	//add the cards from a deck to this deck
	public void mergeDeck(Deck mergeDeck) {
		for (Card card : mergeDeck.getCardList()){
			cardList.add(card);
		}
	}
	
	public ArrayList<Card> getCardList() {
		return cardList;
	}
	
	public void setTeam(Team teamSet) {
		team = teamSet;
	}
	
	public Team getTeam() {
		return team;
	}
	public void setDeck(ArrayList<Card> cards) {
		cardList = cards;
	}
	
	public void shuffle()
	{
		Random rndGenerator = new Random();
		for (int i = 1 ; i <= 100 ; i++)
		{
			int num1 = rndGenerator.nextInt(78);
			Card card = cardList.remove(0);
			cardList.add(num1, card);

		}
		

	}
	
	//remove the card number index - 1 from cardList
	public Card removeCardByIndex(int index)
	{
		return cardList.remove(index);
	}
	public String toString()
	//methode purement pour simplifier les tests
	{
		String stringDeck = "";
		for (Card card : cardList)
			stringDeck = stringDeck + card.getSuit() + card.getValue()+" " ;
		return stringDeck;
	}
	
	public void empty()
	{
		cardList = new ArrayList<Card>();
	}
}
