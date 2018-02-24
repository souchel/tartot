package metier;
import java.io.*;
import java.util.ArrayList;

public class Test {
	public static void main(String[] args) throws IOException {
		System.out.println("Une carte est creee");
		Card card = new Card(Suit.TRUMP, 22);
		System.out.println("valeur : "+card.getValue()+" soit "+card.valueToString()+ " Couleur : "+card.getSuit());
		System.out.println("cette carte a pour valeur : "+ card.pointValue());
		System.out.println("");
		System.out.println("Un joueur est cree");
		Player laure = new Player("Laure");
		System.out.println("son username est : "+laure.getUsername()+" et son equipe est : "+laure.getTeam());
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("On instancie maintenant une partie entre Hugo, Guillaume, Paul et Thomas");
		Game game ;
		String[] usernames = {"Hugo", "Guillaume", "Paul", "Thomas"};
		game = new Game(usernames);
		game.initializeGame();
		System.out.println("deck une fois melange :" + game.getDeck());
		System.out.println("taille du paquet : "+ game.getDeck().getCardList().size());
		System.out.println("On coupe");
		game.cutTheDeck(6);
		System.out.println("le paquet : "+ game.getDeck());		
		System.out.println("On distribue les cartes");	
		int[] indexes = {1,2,3,4,5,6};
		game.distribute(indexes) ;
		System.out.println("Chaque personne devrait avoir 18 cartes : Hugo\t"+game.players[0].getHand().getCardList().size());
		System.out.println("jeu de Hugo : "+game.players[0].getHand() );
		System.out.println("jeu de Guillaume : "+game.players[1].getHand() );
		System.out.println("jeu de Paul : "+game.players[2].getHand() );
		System.out.println("jeu de Thomas : "+game.players[3].getHand() );
		System.out.println("chien : "+game.chien );
		System.out.println("il ne devrait plus y avoir de cartes dans le paquet : "+ game.getDeck());
		
		System.out.println("chaque joueur devrait avoir 0 points");
		System.out.println(game.getStats());
		System.out.println("Hugo gagne de 0 points");
		ArrayList<ArrayList<Announces>> annonces = new ArrayList<ArrayList<Announces>>();
		for (int index = 0 ; index < 4 ; index ++)
			annonces.add(new ArrayList<Announces>());
		annonces.get(0).add(Announces.SIMPLE_HANDFUL);
		game.getStats().updatePointsAndBid(game.getPlayers()[0], Bid.GUARD, 3, annonces, 36);
		System.out.println(game.getStats());
		
	}
}
