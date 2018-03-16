package team23.tartot.core;

import android.content.Context;
import android.content.res.Resources;

import team23.tartot.R;

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

	public String toString(Context context) {
		Resources res = context.getResources();
		switch(this) {
			case SMALL: return res.getString(R.string.small);
			case GUARD: return res.getString(R.string.guard);
			case GUARD_WITHOUT: return res.getString(R.string.guard_without);
			case GUARD_AGAINST: return res.getString(R.string.guard_against);
			default: throw new IllegalArgumentException();
		}
	}

}
