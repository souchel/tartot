package metier;

public class GameManager {
	private Game game;
	private Player
	
	public GameManager(Game onGoingGame) {
		this.game = onGoingGame;
	}
	
	private void nextDealer() {
		
	}
	
	private Player getPlayerAtPosition(int i) {
		
	}
	
	//return if the card can be played, unless false
	public boolean checkCard(Card card, Player player) {
		Card winingCard = game.getOnGoingFold().getWiningCard();
		Suit askedSuit = game.getOnGoingFold().getSuit();
		Suit cardSuit = card.getSuit();
		int cardValue = card.getValue();
		Suit winingSuit = winingCard.getSuit();
		int winingValue = winingCard.getValue();
		//if excuse played ok
		if (cardSuit == Suit.TRUMP && cardValue == 22) {
			return true;
		}
		//if no suit asked ok
		if (askedSuit == null) {
			return true;
		//in the case askedsuit isn't trump
		} else if (askedSuit != Suit.TRUMP) {
			//if both card have same suit ok
			if (askedSuit == cardSuit) {
				return true;
			//if not but player have asked suit, NOT ok
			} else if (player.haveSuit(askedSuit)) {
				return false;
			//if player don't have asked suit but play trump
			} else if (cardSuit == Suit.TRUMP) {
				//if value higher than wining card, ok
				if (cardValue > winingValue) {
					return true;
				//if wining card isn't trump, ok
				} else if (winingSuit != Suit.TRUMP) {
					return true;
				//if wining card is trump and higher value 
				} else
					//if player don't have higher value, ok
					if (!player.haveHigherTrump(winingValue)) {
						return true;
					//if player have higher value
					} else return false;
			//if player don't have asked suit and isn't playing trump while he have one, Not ok
			} else if (player.haveSuit(Suit.TRUMP)) {
				return false;
			//if player don't have neither asked nor trump suit, ok
			} else return true;
		//in the case asked suit is trump
		//if trump card is played with a higher value ok
		} else if (cardSuit == Suit.TRUMP && cardValue > winingValue) {
			return true;
		//if player don't have trump, ok
		} else if (!player.haveTrump()) {
			return true;
		// have trump but not higher, ok
		} else if (!player.haveHigherTrump(winingValue)) {
			return true;
		//have trump higher but don't play it
		} else if (cardValue < winingValue) {
			return false;
		//have trump card and play it
		} else return true;

	}
}
