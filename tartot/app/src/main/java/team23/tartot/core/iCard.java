package team23.tartot.core;

import java.io.Serializable;

public class iCard implements Serializable {
    private Card card;
    private String player;

    public iCard(Card c, String username){
        card = c;
        player = username;
    }

    public void setCard(Card c){
        card = c;
    }
    public void setPlayer(String username){
        player = username;
    }
    public Card getCard(){
        return card;
    }
    public String getPlayer(){
        return player;
    }
}
