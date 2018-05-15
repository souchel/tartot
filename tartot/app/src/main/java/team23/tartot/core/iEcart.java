package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iEcart implements Serializable {
    private ArrayList<Card> ecart;

    public iEcart(ArrayList<Card> e){
        ecart = e;
    }

    public void setEcart(ArrayList<Card> e){
        ecart = e;
    }
    public ArrayList<Card> getEcart(){
        return ecart;
    }
}
