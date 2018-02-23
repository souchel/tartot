package metier;

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
	int numberOfPlayers;
	Player dealer ;
	int indexDealer = 0 ;
	
	public Game(String[] usernames)
	{
		numberOfPlayers = usernames.length;
		deck = new Deck();
		onGoingFold = new OnGoingFold();
		players = new Player[numberOfPlayers];
		for (int i = 0 ; i < numberOfPlayers; i++)
		{
			players[i] = new Player(usernames[i]);
		}
		dealer = players[indexDealer];
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
	
	public void distribute()
	{
		for (int i = 1; i <= 24 ; i++)
		{
			for (int j = 1 ; j <= 3 ; j++)
			{
				players[(i/numberOfPlayers)-1].getDeck().addCard(deck.removeCardByIndex(0));
			}
		}
	}
	public OnGoingFold getOnGoingFold()
	{
		return onGoingFold ;
	}
}
