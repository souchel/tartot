package metier;
import java.io.*;

public class Test {
	public static void main(String[] args) throws IOException {
		System.out.println("Une carte est creee");
		Card card = new Card(Suit.DIAMOND, 14);
		System.out.println("valeur : "+card.getValue()+" Couleur : "+card.getSuit());
		System.out.println("cette carte a pour valeur : "+ card.pointValue());
		System.out.println("");
		System.out.println("Un joueur est cree");
		Player laure = new Player("Laure");
		System.out.println("son username est : "+laure.getUsername()+" et son equipe est : "+laure.getTeam());
	}
}
