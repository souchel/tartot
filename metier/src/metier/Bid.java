package metier;

public enum Bid {
	SMALL(1),
	GUARD(2),
	GUARD_WITHOUT(4),
	GUARD_AGAINST(6);
	
	int multiplicant ;
	
	private Bid(int i)
	{
		multiplicant = i;
	}
	public int getMultiplicant()
	{
		return multiplicant;
	}
}