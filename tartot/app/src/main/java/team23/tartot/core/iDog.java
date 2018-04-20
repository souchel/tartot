package team23.tartot.core;

import java.io.Serializable;
import java.util.ArrayList;

public class iDog implements Serializable {
    private ArrayList<Card> dog;

    public iDog(ArrayList<Card> d){
        dog = d;
    }

    public void setDog(ArrayList<Card> d){
        dog = d;
    }
    public ArrayList<Card> getDog(){
        return dog;
    }
}
