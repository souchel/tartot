package metier;
import java.util.Random;


public class Game {
	//the announce
	private Bid bid;
	// ???
	private boolean done;
	//suivis des points de chaque joueur
	int points;
	int oudlersNumber ;
	Deck deck;
	Player[] players;
	OnGoingFold onGoingFold ; 
	Player dealer ;
	int indexDealer = 0 ;
	Deck chien;
	
	public Game(String[] usernames)
	{
		deck = new Deck();
		onGoingFold = new OnGoingFold();
		players = new Player[usernames.length];
		for (int i = 0 ; i < usernames.length; i++)
		{
			players[i] = new Player(usernames[i]);
		}
		dealer = players[indexDealer];
		chien = new Deck();
	}
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
	public boolean alreadyInArray(int[] array, int number)
	//return true if number is already in the array of int called array and false if not
	{
		for (int i = 0 ; i < array.length ; i ++)
		{
			if (array[i]==number)
				return true ;
		}
		return false;
	}
	public void distribute()
	{
		//on cherche a savoir a quel moment on va mettre des cartes dans le chien
		//On cree une liste de moments qui ne peuvent pas exister
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
		//On distribue les cartes 3 par 3 aux 4 joueurs
		for (int i = 1; i <= 24 ; i++)
		{
			//on donne 3 cartes au joueur i modulo 4
			for (int j = 1 ; j <= 3 ; j++)
				players[(i/4)-1].getDeck().addCard(deck.removeCardByIndex(0));
			//si c est l un des tours ou on est cense mettre dans le chien, on met une carte dans le chien
			if (alreadyInArray(indexes, i))
				chien.addCard(deck.removeCardByIndex(0));
		}
	}
	public OnGoingFold getOnGoingFold()
	{
		return onGoingFold ;
	}
}