package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iAnnounces implements Serializable {
    private ArrayList<Announces> announces;
    private String player;

    public iAnnounces(ArrayList<Announces> a, String username){
        announces = a;
        player = username;
    }

    public void setAnnounces(ArrayList<Announces> a){
        announces = a;
    }
    public void setPlayer(String username){
        player = username;
    }
    public ArrayList<Announces> getAnnounces(){
        return announces;
    }
    public String getPlayer(){
        return player;
    }
}
