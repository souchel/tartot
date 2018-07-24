package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iDeck implements Serializable {
    private ArrayList<Card> deck;
    private String player;
    private String mParticipantId;

    public iDeck(ArrayList<Card> d, String username, String id){
        deck = d;
        player = username;
        mParticipantId = id;

    }

    public void setDeck(ArrayList<Card> d){
        deck = d;
    }
    public void setPlayer(String username){
        player = username;
    }
    public void setId(String id){
        mParticipantId = id;
    }
    public ArrayList<Card> getDeck(){
        return deck;
    }
    public String getPlayer(){
        return player;
    }
    public String getmParticipantId() {
        return mParticipantId;
    }
}
