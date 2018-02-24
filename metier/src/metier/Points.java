package metier;

import java.util.ArrayList;

public class Points {
	private Player[] players;
	private double[] points;
	private ArrayList<Player> callers ;
	private ArrayList<Player> called ;
	private ArrayList<Bid> bids ; 
	private ArrayList<Card> cardsCalled;
	
	
	public Points(Player[] players)
	{
		this.players = players ;
		points = new double[players.length];
		for (int i = 0 ; i < points.length ; i++)
		{
			points[i]=0;
		}
	}
	public void updatePointsAndBid(Player attack, Bid bid, int attackOudlerNumber, ArrayList<Announces>[] annonces, int pointsAttack)
	{
		double[] pointsGame = new double[players.length];
		double pointsNeeded = Game.oudlerNumberIntoPointsNeeded(attackOudlerNumber) ; 
		pointsGame = updatePointsMisery(pointsGame, annonces);
		if (Game.theAttackWins(pointsNeeded, pointsAttack))
		{
			for (int index = 0 ; index < players.length ; index++)
			{
				if (players[index].getUsername()== attack.getUsername())
				{
					pointsGame[index] += 75 * bid.getMultiplicant() + (pointsAttack - pointsNeeded)*bid.getMultiplicant() * 3 ;
				}
				else
				{
					pointsGame[index] += -25 * bid.getMultiplicant() - (pointsAttack - pointsNeeded)*bid.getMultiplicant() ;
				}
			}
			for (int i = 0 ; i < annonces.length ; i++)
			{
				if (annonces[i].size()>0)
				{
					for (Announces a2 : annonces[i])
						switch (a2) 
						{
					      case SLAM : break;
					      case SIMPLE_HANDFUL : break ;
					      case DOUBLE_HANDFUL : break;
					      case TRIPLE_HANDFUL : break;
					      case PETIT_AU_BOUT : break;
						  case MISERY: break;
						  default: break;
					    }
				}
			}
		}
	}
	public double[] updatePointsHandful(double[] pointsGame, int playerIndex)
	{
		return pointsGame ;
	}
	public double[] updatePointsMisery( double[] pointsGame, ArrayList<Announces>[] annonces)//Attention deux joueurs ne peuvent pas avoir le mm nom d utilisateur
	{
		ArrayList<Integer> miserers = new ArrayList<Integer>();
		ArrayList<Integer> nonMiserers = new ArrayList<Integer>();
		for (int index = 0 ; index < players.length ; index ++)
		{
			if (annonces.length > 0)
			{
				for (Announces annonce : annonces[index])
				{
					if (annonce == Announces.MISERY)
					{
						miserers.add(index);
					}
					else
						nonMiserers.add(index);
				}
			}
			else
				nonMiserers.add(index);
		}
		if (miserers.size() > 0)
		{
			for (int indexNonMiserer : nonMiserers)
			{
				pointsGame[indexNonMiserer] += -10 * miserers.size();
			}
			for (int indexMiserer : miserers)
			{
				pointsGame[indexMiserer] += 10 *(players.length - miserers.size());
			}
		}
		return pointsGame ;
	}
	public void addPass()
	{
		
	}
	public ArrayList<Player> getCallers()
	{
		return callers ;
	}
	public ArrayList<Player> getCalled()
	{
		return called ;
	}
	public ArrayList<Bid> getBids()
	{
		return bids ;
	}
	public ArrayList<Card> getCardsCalled()
	{
		return cardsCalled ;
	}
}
