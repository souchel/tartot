package team23.tartot.core;

import java.io.Serializable;

public class iFullDeck implements Serializable {
    private Deck deck;
    private String player;

    public iFullDeck(Deck d, String username){
        deck = d;
        player = username;
    }

    public void setDeck(Deck d){
        deck = d;
    }
    public void setPlayer(String username){
        player = username;
    }
    public Deck getDeck(){
        return deck;
    }
    public String getPlayer(){
        return player;
    }

}
