package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iDeck implements Serializable {
    private ArrayList<Card> deck;
    private String player;

    public iDeck(ArrayList<Card> d, String username){
        deck = d;
        player = username;
    }

    public void setDeck(ArrayList<Card> d){
        deck = d;
    }
    public void setPlayer(String username){
        player = username;
    }
    public ArrayList<Card> getDeck(){
        return deck;
    }
    public String getPlayer(){
        return player;
    }
}
