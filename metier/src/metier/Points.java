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
	public void updatePointsAndBid(Player attack, Bid bid, int attackOudlerNumber, ArrayList<ArrayList<Announces>> annonces, int pointsAttack)
	{
		double[] pointsGame = new double[players.length];
		double pointsNeeded = GameManager.oudlerNumberIntoPointsNeeded(attackOudlerNumber) ; 
		updatePointsMisery(pointsGame, annonces);
		
		int multiplier ;
		if (GameManager.theAttackWins(pointsNeeded, pointsAttack))
		{
			multiplier = 1 ;
		}
		else
		{
			multiplier = -1;
		}
		for (int index = 0 ; index < players.length ; index++)
		{
			if (players[index].getUsername()== attack.getUsername())
			{
				pointsGame[index] += multiplier * 75 * bid.getMultiplicant() + (pointsAttack - pointsNeeded)*bid.getMultiplicant() * 3 ;
			}
			else
			{
				pointsGame[index] += -1 * multiplier * 25 * bid.getMultiplicant() - (pointsAttack - pointsNeeded)*bid.getMultiplicant() ;
			}
		}
		for (int i = 0 ; i < annonces.size() ; i++)
		{
			if (annonces.get(i).size()>0)
			{
				for (Announces a2 : annonces.get(i))
				{
					switch (a2) 
					{
				      case SLAM : updatePointsSlam(pointsGame, a2); break;
				      case SIMPLE_HANDFUL : updatePointsHandful(pointsGame, i, 20, GameManager.theAttackWins(pointsNeeded, pointsAttack), attack) ; break;
				      case DOUBLE_HANDFUL : updatePointsHandful(pointsGame, i, 30, GameManager.theAttackWins(pointsNeeded, pointsAttack), attack); break;
				      case TRIPLE_HANDFUL : updatePointsHandful(pointsGame, i, 40, GameManager.theAttackWins(pointsNeeded, pointsAttack), attack); break;
				      case PETIT_AU_BOUT : updatePointsPetitAuBout(pointsGame , a2, bid, attack);
					  case MISERY: break;
					  default: break;
				    }
				}
			}
		}
			
	
		updatePoints(pointsGame);
	}
	public void updatePointsSlam(double[] pointsGame, Announces announce)
	{
		if (announce.getOwner().getTeam() == Team.DEFENSE)
		{
			for(int i = 0 ; i < players.length ; i++)
			{
				if (players[i].getTeam() == Team.ATTACK)
					pointsGame[i]+= -200*3 ;
				else
					pointsGame[i]+= 200 ;
			}
		}
		else
		{
			for(int i = 0 ; i < players.length ; i++)
			{
				if (players[i].getTeam() == Team.ATTACK)
				{
					pointsGame[i]+= 600 ;
					System.out.println("l attaque reçoit 600 "+pointsGame[i]);

				}
				else
				{
					pointsGame[i]+= -200 ;
					System.out.println("la défense perd 200 "+pointsGame[i]);

				}
			}
		}
			
	}
	public void updatePointsPetitAuBout(double[] pointsGame, Announces annonce, Bid bid, Player attack)
	{
		if (annonce.getOwner().getTeam() == Team.ATTACK) {
			for(int i = 0 ; i < players.length ; i++)
			{
				if (players[i].getTeam() == Team.ATTACK)
					pointsGame[i]+= bid.getMultiplicant()*10*3 ;
				else
					pointsGame[i]+= -bid.getMultiplicant()*10 ;
			}
		}
		else
		{
			for(int i = 0 ; i < players.length ; i++)
			{
				if (players[i].getTeam() == Team.ATTACK)
					pointsGame[i]+= - bid.getMultiplicant()*10*3 ;
				else
					pointsGame[i]+= bid.getMultiplicant()*10 ;
			}
		}
	}
	public void updatePointsHandful(double[] pointsGame, int playerIndex, double gain, boolean attackWon, Player attack)
	{
		int multiplier ;
		if (attackWon )
		{
			multiplier = 1 ;			
		}
		else
		{
			multiplier = -1;
		}
		for (int index = 0 ; index < pointsGame.length ; index ++)
		{
			if (playerIndex == index)
			{
				pointsGame[index] += gain * 3 * multiplier ;
			}
			else
			{
				pointsGame[index] += -gain * multiplier;
			}
		}
	}
	public void updatePointsMisery( double[] pointsGame, ArrayList<ArrayList<Announces>> annonces)//Attention deux joueurs ne peuvent pas avoir le mm nom d utilisateur
	{
		
		ArrayList<Integer> miserers = new ArrayList<Integer>();
		ArrayList<Integer> nonMiserers = new ArrayList<Integer>();
		for (int index = 0 ; index < players.length ; index ++)
		{
			
			for (Announces annonce : annonces.get(index))
			{
				
				if (annonce == Announces.MISERY)
				{
					miserers.add(index);
				}
				else
					nonMiserers.add(index);
			}
			if (annonces.get(index).size()==0)
			{
				nonMiserers.add(index);
			}
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
	public void updatePoints(double[] gamePoints)
	{
		for (int index = 0 ; index < points.length ; index ++)
		{
			points[index]+= gamePoints[index];
		}
	}
	public String toString()
	{
		String stats = "";
		for (int index = 0 ; index < players.length ; index++)
		{
			stats += players[index].getUsername()+ " a " + points[index]+"\t";
		}
		return stats ;
	}
}
