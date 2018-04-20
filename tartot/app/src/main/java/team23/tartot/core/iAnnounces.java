package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iAnnounces implements Serializable {
    private ArrayList<Announces> announces;
    private Player player;

    public iAnnounces(ArrayList<Announces> a, Player p){
        announces = a;
        player = p;
    }

    public void setAnnounces(ArrayList<Announces> a){
        announces = a;
    }
    public void setPlayer(Player p){
        player = p;
    }
    public ArrayList<Announces> getAnnounces(){
        return announces;
    }
    public Player getPlayer(){
        return player;
    }
}
