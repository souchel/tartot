package team23.tartot.core;

import android.content.Context;
import android.content.res.Resources;

import team23.tartot.R;

public enum Announces {
	SIMPLE_HANDFUL,
	DOUBLE_HANDFUL,
	TRIPLE_HANDFUL,
	MISERY,
	//we suppose defense won't slam for now...
	SLAM,
	PETIT_AU_BOUT;
	
	private Player owner ;
	private Team team;
	
	public Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Player owner)
	{
		this.owner = owner ;
	}

	public Team getTeam(){ return team; }

	public void setTeam(Team t) { team = t; }

	public String toString(Context context) {
		Resources res = context.getResources();
		switch(this) {
			case SIMPLE_HANDFUL: return res.getString(R.string.simple_handful);
			case DOUBLE_HANDFUL: return res.getString(R.string.double_handful);
			case TRIPLE_HANDFUL: return res.getString(R.string.triple_handful);
			case MISERY: return res.getString(R.string.misery);
			case SLAM: return res.getString(R.string.slam);
			case PETIT_AU_BOUT:return res.getString(R.string.petit_au_bout);
			default: throw new IllegalArgumentException();
		}
	}
}
