package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iDeck implements Serializable {
    private ArrayList<Card> deck;
    private Player player;

    public iDeck(ArrayList<Card> d, Player p){
        deck = d;
        player = p;
    }

    public void setDeck(ArrayList<Card> d){
        deck = d;
    }
    public void setPlayer(Player p){
        player = p;
    }
    public ArrayList<Card> getDeck(){
        return deck;
    }
    public Player getPlayer(){
        return player;
    }
}
