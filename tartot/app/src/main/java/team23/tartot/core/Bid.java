package team23.tartot.core;

public enum Bid {
	SMALL(1),
	GUARD(2),
	GUARD_WITHOUT(4),
	GUARD_AGAINST(6);
	
	int multiplicant ;
	//default -1, for unknown
	private int playerPosition = -1;
	
	public void setPlayerPosition(int playerToSet) {
		playerPosition = playerToSet;
	}
	public int getPlayerPosition() {
		return playerPosition;
	}
 	private Bid(int i)
	{
		multiplicant = i;
	}
	public int getMultiplicant()
	{
		return multiplicant;
	}
}
