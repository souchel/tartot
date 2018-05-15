package team23.tartot.core;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class iBids implements Serializable{
    private ArrayList<Bid> bids = new ArrayList<>();

    public iBids(ArrayList<Bid> b){
        bids = b;
    }

    public void setBids(ArrayList<Bid> b){
        bids = b;
    }
    public ArrayList<Bid> getBids(){
        return bids;
    }
}
