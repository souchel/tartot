package team23.tartot.core;

import java.io.Serializable;

public class iCard implements Serializable {
    private Card card;
    private Player player;

    public iCard(Card c, Player p){
        card = c;
        player = p;
    }

    public void setCard(Card c){
        card = c;
    }
    public void setPlayer(Player p){
        player = p;
    }
    public Card getCard(){
        return card;
    }
    public Player getPlayer(){
        return player;
    }
}
