package metier;
import java.io.*;

public class TestP {
	public static void main(String[] args) throws IOException {
		System.out.println("création d'un deck");
		Deck deck = new Deck();
		System.out.println("deck vide et team inconnu");
		System.out.println("team: "+deck.getTeam());
		System.out.println("points dans deck: "+deck.countPoints()+"\n");
		
		System.out.println("ajout d'une carte (excuse)");
		deck.addCard(new Card(Suit.TRUMP, 22));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		System.out.println("ajout d'une carte banale");
		deck.addCard(new Card(Suit.DIAMOND, 2));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		System.out.println("ajout d'une carte petit");
		deck.addCard(new Card(Suit.TRUMP, 1));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		System.out.println("ajout d'une carte déjà existante");
		deck.addCard(new Card(Suit.TRUMP, 22));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		System.out.println("suppression d'une carte pas en double");
		deck.removeCard(new Card(Suit.TRUMP, 1));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		//removeCard crash if card twice in the deck
//		System.out.println("suppression d'une carte en double");
//		deck.removeCard(new Card(Suit.TRUMP, 22));
//		System.out.println("points dans deck: "+deck.countPoints());
//		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		Deck deck2 = new Deck();
		System.out.println("ajout d'une carte banale à deck2");
		deck2.addCard(new Card(Suit.DIAMOND, 13));
		System.out.println("points dans deck: "+deck2.countPoints());
		System.out.println("bouts dans deck: "+deck2.countOudlers());
		deck.mergeDeck(deck2);
		System.out.println("merge des deck");
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		OnGoingFold fold = new OnGoingFold();
		System.out.println("suit pas instancié : "+fold.getSuit());
		
		System.out.println("ajout d'une carte :");
		fold.addCard(new Card(Suit.TRUMP, 22), "Paul");
		System.out.println("joueur gagnant non instancié :"+fold.getWiningPlayer());
		System.out.println("suit pas instancié : "+fold.getSuit());
		
		System.out.println("ajout d'une carte :");
		fold.addCard(new Card(Suit.DIAMOND, 7), "Laure");
		System.out.println("joueur gagnant Laure :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
		
		System.out.println("ajout d'une carte plus faible:");
		fold.addCard(new Card(Suit.DIAMOND, 4), "Hugo");
		System.out.println("joueur gagnant Laure :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
		
		System.out.println("ajout d'une mauvaise couleur mais plus forte :");
		fold.addCard(new Card(Suit.HEART, 10), "Gui");
		System.out.println("joueur gagnant Laure :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
		
		System.out.println("ajout d'une plus forte bonne couleur :");
		fold.addCard(new Card(Suit.DIAMOND, 12), "Thomas");
		System.out.println("joueur gagnant Thomas :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
		
		System.out.println("coupé :");
		fold.addCard(new Card(Suit.TRUMP, 7), "Paul");
		System.out.println("joueur gagnant Paul :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
		
		System.out.println("excuse :");
		fold.addCard(new Card(Suit.TRUMP, 22), "Laure");
		System.out.println("joueur gagnant Paul :"+fold.getWiningPlayer());
		System.out.println("suit diamond: "+fold.getSuit());
	}
}
