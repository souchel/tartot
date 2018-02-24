package metier;

import java.util.ArrayList;

public class Points {
	private Player[] players;
	private double[] points;
	private ArrayList<Player> callers ;
	private ArrayList<Player> called ;
	private ArrayList<Bid> bids ; 
	private ArrayList<Card> cardCalled;
	
	
	public Points(Player[] players)
	{
		this.players = players ;
		points = new double[players.length];
		for (int i = 0 ; i < points.length ; i++)
		{
			points[i]=0;
		}
	}
	public void updatePointsAndBid(ArrayList<Player> Attack, Bid bid, int attackOudlerNumber, Announces[] annonces, int pointsAttack, Card cardCalled)
	{
		double[] pointsGame = new double[players.length];
		double pointsNeeded = Game.oudlerNumberIntoPointsNeeded(attackOudlerNumber) ; 
		if (Game.theAttackWins(pointsNeeded, pointsAttack))
		{
			
		}
	}
}
