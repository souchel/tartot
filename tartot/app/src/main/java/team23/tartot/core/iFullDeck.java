package team23.tartot.core;

import java.io.Serializable;

public class iFullDeck implements Serializable {
    private Deck deck;
    private Player player;

    public iFullDeck(Deck d, Player p){
        deck = d;
        player = p;
    }

    public void setDeck(Deck d){
        deck = d;
    }
    public void setPlayer(Player p){
        player = p;
    }
    public Deck getDeck(){
        return deck;
    }
    public Player getPlayer(){
        return player;
    }

}
