package metier;



// this class doesn't check which player is adding a card!!!
public class OnGoingFold extends Deck{
	private Suit askedSuit;
	private Card winingCard;
	private String winingPlayer;
	
	public OnGoingFold() {
	}
	
	//should get suit on the first card but if it doesn't it'll take suit from the following card
	public void addCard(Card addedCard, String playerName) {
		if (askedSuit == null && addedCard.isEqual(new Card(Suit.TRUMP, 22)) == false) {
			askedSuit = addedCard.getSuit();
		}
		super.addCard(addedCard);
		if (this.isBetterCard(addedCard) == true){
			winingCard = addedCard;
			winingPlayer = playerName;
		}
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
	
	public String getWiningPlayer() {
		return winingPlayer;
	}
	
	public Card getWiningCard() {
		return winingCard;
	}
}
