package team23.tartot.core;
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
		Player laure = new Player("Laure", 0);
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
		
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		for (int i = 1 ; i < 4 ; i++)
		{
			gameManager.getPlayers()[i].setTeam(Team.DEFENSE);
		}
		System.out.println("Hugo fait une petite avec 3 bouts qu'il gagne de 0 points, aucune annonce");
		ArrayList<Announces> annonces = new ArrayList<Announces>();
		gameManager.getStats().updatePointsAndBid(Bid.SMALL, 3, annonces, 36);
		System.out.println("Hugo devrait avoir 75 pts et les autres -25 : "+gameManager.getStats());
		
		
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		for (int i = 1 ; i < 4 ; i++)
		{
			gameManager.getPlayers()[i].setTeam(Team.DEFENSE);
		}
		System.out.println("Hugo fait une garde avec 3 bouts qu'il gagne de 0 points, aucune annonce");
		ArrayList<Announces> annonces1 = new ArrayList<Announces>();
		gameManager.getStats().updatePointsAndBid(Bid.GUARD, 3, annonces1, 36);
		System.out.println("Hugo devrait avoir gagn� 150 pts et les autres -50 : "+gameManager.getStats());
		
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		for (int i = 1 ; i < 4 ; i++)
		{
			gameManager.getPlayers()[i].setTeam(Team.DEFENSE);
		}
		System.out.println("Hugo fait une petite avec 3 bouts qu'il gagne de 0 points, Paul annonce mis�re");
		ArrayList<Announces> annonces11 = new ArrayList<Announces>();
		Announces a = Announces.MISERY ;
		a.setOwner(gameManager.getPlayers()[2]);
		annonces11.add(a);
		gameManager.getStats().updatePointsAndBid(Bid.SMALL, 3, annonces11, 36);
		System.out.println("Hugo devrait avoir gagn� 65 pts, Paul 5 et les autres -35 : "+gameManager.getStats());
	
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		for (int i = 1 ; i < 4 ; i++)
		{
			gameManager.getPlayers()[i].setTeam(Team.DEFENSE);
		}
		System.out.println("Hugo fait une petite avec 3 bouts qu'il perd d'un points, pas d'annonce");
		ArrayList<Announces> annonces111 = new ArrayList<Announces>();
		gameManager.getStats().updatePointsAndBid( Bid.SMALL, 3, annonces111, 35);
		System.out.println("Hugo devrait avoir gagn� -75 pts et les autres 25 : "+gameManager.getStats());
		
		gameManager.getPlayers()[0].setTeam(Team.ATTACK);
		for (int i = 1 ; i < 4 ; i++)
		{
			gameManager.getPlayers()[i].setTeam(Team.DEFENSE);
		}
		System.out.println("Hugo fait une garde avec 2 bouts avec une poign�e de 10 atouts, il mene le petit au bout et realise 49 pts, pas d'autre annonce");
		ArrayList<Announces> annonces1111 = new ArrayList<Announces>();
		Announces a1 = Announces.SIMPLE_HANDFUL;
		a1.setOwner(gameManager.getPlayers()[0]);
		Announces a2 = Announces.PETIT_AU_BOUT;
		a2.setOwner(gameManager.getPlayers()[0]);
		annonces1111.add(a1);
		annonces1111.add(a2);
		gameManager.getStats().updatePointsAndBid( Bid.GUARD, 2, annonces1111, 49);
		System.out.println("Hugo devrait avoir gagn� 318 pts et les autres -106 : "+gameManager.getStats());
		
	}
}
