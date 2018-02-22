package metier;
import java.io.*;

public class TestP {
	public static void main(String[] args) throws IOException {
		System.out.println("cr�ation d'un deck");
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
		
		System.out.println("ajout d'une carte d�j� existante");
		deck.addCard(new Card(Suit.TRUMP, 22));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		System.out.println("suppression d'une carte pas en double");
		deck.removeCard(new Card(Suit.TRUMP, 1));
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		//removeCard crash if carte twice in the deck
//		System.out.println("suppression d'une carte en double");
//		deck.removeCard(new Card(Suit.TRUMP, 22));
//		System.out.println("points dans deck: "+deck.countPoints());
//		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
		
		Deck deck2 = new Deck();
		System.out.println("ajout d'une carte banale � deck2");
		deck2.addCard(new Card(Suit.DIAMOND, 13));
		System.out.println("points dans deck: "+deck2.countPoints());
		System.out.println("bouts dans deck: "+deck2.countOudlers());
		deck.mergeDeck(deck2);
		System.out.println("merge des deck");
		System.out.println("points dans deck: "+deck.countPoints());
		System.out.println("bouts dans deck: "+deck.countOudlers()+"\n");
	}
}
