package metier;

import java.util.List;
import java.util.Random;

//multiplicateurs 1, 2, 4, 6

//TODO implements iCoreToNetwork, iNetworkToCore
public class GameManager {
	private Player[] players;
	private Points stats ;
	int points;
	int oudlersNumber ;
	private Deck deck;
	OnGoingFold onGoingFold ; 
	int indexDealer ;
	Deck chien;
	Bid bid;
	
	public GameManager(String[] usernames) {
		deck = new Deck();
		onGoingFold = new OnGoingFold();
		players = new Player[usernames.length];
		for (int i = 0 ; i < usernames.length; i++)
		{
			players[i] = new Player(usernames[i]);
		}
		indexDealer = 0 ;
		chien = new Deck();
		stats = new Points(players);
		bid = null;
	}
	
	
	//initialize
	public void initializeGame()
	{
		initializeDeck();
		deck.shuffle();
	}
	public void initializeDeck()
	{
		for (Suit suit : Suit.values())
		{
			int bound ;
			if (suit ==  Suit.TRUMP)
			{
				bound = 22;
			}
			else
			{
				bound = 14;
			}
			for (int i = 1; i<=bound; i++)
			{
				deck.addCard(new Card(suit, i));
			}
		}
	}
	
	
	
	
	//distribution phase
	public void distribute(int[] indexes)
	{	
		//On distribue les cartes 3 par 3 aux 4 joueurs
		for (int i = 1; i <= 24 ; i++)
		{
			//on donne 3 cartes au joueur i modulo 4
			for (int j = 1 ; j <= 3 ; j++)
			{
				Card card = deck.removeCardByIndex(0);
				players[(i + indexDealer )%4].getHand().addCard(card);
				
			}
			//si c est l un des tours ou on est cense mettre dans le chien, on met une carte dans le chien
			if (alreadyInArray(indexes, i - 1))
			{
				chien.addCard(deck.removeCardByIndex(0));
			}
		}
	}
	public void cutTheDeck(int position)
	{
		/*
		Random rndGenerator = new Random();
		int position = rndGenerator.nextInt(73)+3;
		*/
		
		
		for (int i = 1; i<=position; i++)
		{
			Card card = deck.removeCardByIndex(0);
			deck.addCard(card);
		}
	}
	public void setNextDealer(){
		if (indexDealer < 3) {
			indexDealer += 1;
		} else indexDealer = 0;
	}
	public int[] getPositionDistribution() {
		//on cherche a savoir a quel moment on va mettre des cartes dans le chien
		//On cree une liste de moments qui ne peuvent pas exister
				
		//Partie qui sert à rien
		int[] indexes = {25,25,25,25,25,25};
		Random rndGenerator = new Random();
		//on pioche 6 entiers entre 0 et 22 sachant qu'on distribue 3 fois 24 cartes mais qu on ne 
		//peut pas mettre la derniere carte dans le chien
		for (int j = 0 ; j < 6 ; j ++)
		{
			int index = rndGenerator.nextInt(23);
			//On pioche jusqu a avoir un index different de ceux deja dans la liste
			while (alreadyInArray(indexes, index))
				index = rndGenerator.nextInt(23);				
			indexes[j] = index ;
		}
		return indexes;
	}
	
	
	
	
	
	
	//count points section
	public int bidIntoMultiplier(Bid bid)
	{
		return bid.getMultiplicant();
	}
	public static double oudlerNumberIntoPointsNeeded(int oudlerNumber)
	{
		switch (oudlerNumber) 
		{
	      case 1: return 51; 
	      case 2: return 41;
	      case 3: return 36;
	      case 0: return 56; 
	      default : return 0;
	    }
	}
	public static boolean theAttackWins(double pointsNeeded, double pointsWon)
	{
		return pointsNeeded <= pointsWon ;
	}
	public double pointsCounter(Deck deckAttack)
	{
		return deckAttack.countPoints() ;
	}
	public Points getStats()
	{
		return stats ;
	}
	
	
	
	
	
	
	
	//TODO reste a faire j ai juste renvoye un truc au pif pour que ca puisse compiler
	//inutil?
//	private Player[] getPlayerAtPosition(int i) {
//		
//		return players;
//	}
	
	
	
	
	
	
	//check rules section
	
	//return if the card can be played, unless false
	//TODO pas testé
	public boolean checkCard(Card card, Player player) {
		Card winingCard = onGoingFold.getWiningCard();
		Suit askedSuit = onGoingFold.getSuit();
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
	//default true
	public boolean checkAnnouncesBegining(List<Announces> announces, Player player) {
		boolean res = true;
		int nbPlayers = players.length;
		for (Announces announce : announces) {
			switch (announce) {
				case MISERY: 
					if (player.haveTrump() && player.haveHead()) {
						res = false;
					}
					break;
				case SIMPLE_HANDFUL:
					switch(nbPlayers) {
						case 3:
							if (player.countTrump() < 13) {
								res = false;
							}
						case 4:
							if (player.countTrump() < 10) {
								res = false;
							}
						case 5:
							if (player.countTrump() < 8) {
								res = false;
							}
					}
					break;
				case DOUBLE_HANDFUL:
					switch(nbPlayers) {
					case 3:
						if (player.countTrump() < 15) {
							res = false;
						}
					case 4:
						if (player.countTrump() < 13) {
							res = false;
						}
					case 5:
						if (player.countTrump() < 10) {
							res = false;
						}
				}
					break;
				case TRIPLE_HANDFUL:
					switch(nbPlayers) {
					case 3:
						if (player.countTrump() < 18) {
							res = false;
						}
					case 4:
						if (player.countTrump() < 15) {
							res = false;
						}
					case 5:
						if (player.countTrump() < 13) {
							res = false;
						}
				}
					break;
				case SLAM:
					break;
				case PETIT_AU_BOUT:
					break;
			}
		}
		return res;
	}
	//TODO
	public boolean checkAnnouncesEnd(List<Announces> announces, Player player) {
		boolean res = true;
		for (Announces announce : announces) {
			switch (announce) {
				case MISERY: 
					break;
				case SIMPLE_HANDFUL:
					break;
				case DOUBLE_HANDFUL:
					break;
				case TRIPLE_HANDFUL:
					break;
				case SLAM:
					break;
				case PETIT_AU_BOUT:
					//vérifie juste que le petit a bien été mis au bout, pas que le pli est gagné
					boolean petit = false;
					//TODO if (Card player.getDeck().getCardList().get(//TODO récupérer lindice)) {
						;
					//}
					break;
			}
		}
		return res;
	}
	//default (if bid null?) return false
	public boolean checkBid(Bid bid) {
		Bid onGoingBid = bid;
		switch (onGoingBid) {
		case SMALL :
			if (bid == Bid.SMALL) {
				return false;
			} break;
		case GUARD :
			if (bid == Bid.SMALL || bid == Bid.GUARD) {
				return false;
			} break;
		case GUARD_WITHOUT :
			if (bid == Bid.SMALL || bid == Bid.GUARD || bid == Bid.GUARD_WITHOUT) {
				return false;
			} break;
		case GUARD_AGAINST :
			return false;
		default :
			return false;
		}
		return true;
	}

	
	
	
	
	
	//utility TODO put them in a class to herit?
	private boolean alreadyInArray(int[] array, int number)
	//return true if number is already in the array of int called array and false if not
	{
		for (int i = 0 ; i < array.length ; i ++)
		{
			if (array[i]==number)
				return true ;
		}
		return false;
	}
	
	//getter setter
	public Player[] getPlayers()
	{
		return players;
	}
	public Deck getDeck()
	{
		return deck ;
	}
	
	
	

	
	//TODO implements mthods
//	@Override
//	public void onInvitationReceived() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void onPlayCard(team23.tartot.core.Player player, team23.tartot.core.Card card) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void onCardsDelt(team23.tartot.core.Card[] cards) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void onAnnounce(team23.tartot.core.Player player, Announce announce) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void createLobby() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void leaveLobby() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void inviteFriend(iPlayer player) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void startGame() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void leaveGame() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void onInGameDataReceive(Object data) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void onInLobbyDataReceive() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void dealCards(team23.tartot.core.Player destination, team23.tartot.core.Card[] cards) {
//		// TODO Auto-generated method stub	
//	}
//	@Override
//	public void playCard(team23.tartot.core.Card card) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void announce(Announce announce) {
//		// TODO Auto-generated method stub
//	}
}
