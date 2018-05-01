package team23.tartot.core;

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
}
