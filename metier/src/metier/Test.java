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
		GameManager gameManager ;
		String[] usernames = {"Hugo", "Guillaume", "Paul", "Thomas"};
		gameManager = new GameManager(usernames);
		gameManager.initializeGame();
		System.out.println("deck une fois melange :" + gameManager.getDeck());
		System.out.println("taille du paquet : "+ gameManager.getDeck().getCardList().size());
		System.out.println("On coupe");
		gameManager.cutTheDeck(6);
		System.out.println("le paquet : "+ gameManager.getDeck());		
		System.out.println("On distribue les cartes");	
		int[] indexes = {1,2,3,4,5,6};
		gameManager.distribute(indexes) ;
		System.out.println("Chaque personne devrait avoir 18 cartes : Hugo\t"+gameManager.getPlayers()[0].getHand().getCardList().size());
		System.out.println("jeu de Hugo : "+gameManager.getPlayers()[0].getHand() );
		System.out.println("jeu de Guillaume : "+gameManager.getPlayers()[1].getHand() );
		System.out.println("jeu de Paul : "+gameManager.getPlayers()[2].getHand() );
		System.out.println("jeu de Thomas : "+gameManager.getPlayers()[3].getHand() );
		System.out.println("chien : "+gameManager.chien );
		System.out.println("il ne devrait plus y avoir de cartes dans le paquet : "+ gameManager.getDeck());
		
		System.out.println("chaque joueur devrait avoir 0 points");
		System.out.println(gameManager.getStats());
		System.out.println("Hugo gagne de 0 points");
		ArrayList<ArrayList<Announces>> annonces = new ArrayList<ArrayList<Announces>>();
		for (int index = 0 ; index < 4 ; index ++)
			annonces.add(new ArrayList<Announces>());
		Announces a = Announces.SLAM;
		a.setOwner(gameManager.getPlayers()[0]);
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		//annonces.get(0).add(Announces.SLAM);

		annonces.get(1).add(Announces.MISERY);
		gameManager.getStats().updatePointsAndBid(gameManager.getPlayers()[0], Bid.SMALL, 3, annonces, 36);
		System.out.println(gameManager.getStats());
		
		a.setOwner(gameManager.getPlayers()[0]);
		System.out.println(a.getOwner().getUsername() + " a déclaré une misère");
		
	}
}
