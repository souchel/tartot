package metier;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestManager {
	public static void main(String[] args) throws IOException {
		String[] usernames = {"Hugo", "Gui", "Paul", "blu","Laure"};
		Game game = new Game(usernames);
		GameManager gameManager = new GameManager(game);
		Player laure = new Player("Laure");
		Player paul = new Player("Paul");
		Player hugo = new Player("Hugo");
		Player gui = new Player("Gui");
		Player blu = new Player("blu");
		Card excuse = new Card(Suit.TRUMP, 22);
		Card atout = new Card(Suit.TRUMP, 10);
		Card normal = new Card(Suit.DIAMOND, 10);
		Card tete = new Card(Suit.HEART, 12);
		laure.getHand().addCard(excuse);
		paul.getHand().addCard(normal);
		hugo.getHand().addCard(atout);
		gui.getHand().addCard(tete);
		blu.getHand().addCard(atout);
		blu.getHand().addCard(tete);
		//test haveTrump
		System.out.println("test haveTrump");
		System.out.println("a un atout (excuse): "+laure.haveTrump()+"unique : "+laure.countTrump());
		System.out.println("pas d'atout : "+paul.haveTrump());
		System.out.println("a un atout : "+hugo.haveTrump());
		System.out.println("a un atout : "+blu.haveTrump()+"\n");
		//test haveSuit
		System.out.println("test haveSuit");
		System.out.println("n'a pas de carreau : "+hugo.haveSuit(Suit.DIAMOND));
		System.out.println("a un atout : "+hugo.haveSuit(Suit.TRUMP));
		System.out.println("a un atout : "+laure.haveSuit(Suit.TRUMP));
		System.out.println("a un carreau : "+paul.haveSuit(Suit.DIAMOND)+"\n");
		//test haveHead
		System.out.println("test haveHead");
		System.out.println("a une tete : "+gui.haveHead());
		System.out.println("a une tete : "+laure.haveHead());
		System.out.println("a pas de tete : "+paul.haveHead());
		System.out.println("a une tete : "+blu.haveHead()+"\n");
		//test havehighertrump
		System.out.println("test haveHigherTrump");
		System.out.println("a plus haut : "+blu.haveHigherTrump(2));
		System.out.println("a moins haut : "+blu.haveHigherTrump(12));
		System.out.println("a pas du tout de toute façon : "+paul.haveHigherTrump(3));
		System.out.println("a que l'excuse : "+laure.haveHigherTrump(2)+"\n");
		//test counttrump
		System.out.println("test countTrump");
		blu.getHand().addCard(new Card(Suit.TRUMP, 10));
		hugo.getHand().addCard(atout);
		System.out.println("pas d'atout"+paul.countTrump());
		System.out.println("2 atouts :"+blu.countTrump());
		System.out.println("2 atouts :"+hugo.countTrump()+"\n");
		//testCheckAnnouncesBegining
		System.out.println("test checkAnnouncesBegining");
		System.out.println("pas de misère mais misère annoncée :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.MISERY), blu));
		System.out.println("double misère et misère annoncée :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.MISERY), paul));
		System.out.println("simple misère et misère annoncée :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.MISERY), gui));
		System.out.println("excuse mais misère annoncée :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.MISERY), laure));
		blu.getHand().addCard(new Card(Suit.TRUMP, 11));
		blu.getHand().addCard(new Card(Suit.TRUMP, 12));
		blu.getHand().addCard(new Card(Suit.TRUMP, 13));
		blu.getHand().addCard(new Card(Suit.TRUMP, 14));
		blu.getHand().addCard(new Card(Suit.TRUMP, 15));
		System.out.println("simple annoncée mais non aquise :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.SIMPLE_HANDFUL), blu));
		blu.getHand().addCard(new Card(Suit.TRUMP, 16));
		System.out.println("simple annoncée et aquise :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.SIMPLE_HANDFUL), blu));
		System.out.println("double annoncée mais non aquise :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.DOUBLE_HANDFUL), blu));
		blu.getHand().addCard(new Card(Suit.TRUMP, 17));
		blu.getHand().addCard(new Card(Suit.TRUMP, 18));
		System.out.println("double annoncée et aquise :"+gameManager.checkAnnouncesBegining(Arrays.asList(Announces.DOUBLE_HANDFUL), blu));

	}
}