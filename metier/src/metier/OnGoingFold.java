package metier;

public class OnGoingFold extends Deck{
	private Suit askedSuit;
	private Card winingCard;
	private String winingPlayer;
	private String nextPlayer;
	
	public OnGoingFold(String startingPlayer) {
		nextPlayer = startingPlayer;
		winingPlayer = startingPlayer;
	}
	
	//we need the gameManager to set up the next player each time
	public void setNextPlayer(String newNextPlayer) {
		nextPlayer = newNextPlayer;
	}
	
	private void setSuit(Suit suit) {
		askedSuit = suit;
	}
	
	public void addCard(Card addedCard) {
		super.addCard(addedCard);
		if (this.isBetterCard(addedCard)==true){
			winingCard = addedCard;
		}
	}
	
	//TODO
	private boolean isBetterCard(Card addedCard) {
		
	}
}
