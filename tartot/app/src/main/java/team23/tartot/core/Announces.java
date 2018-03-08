package team23.tartot.core;

public enum Announces {
	SIMPLE_HANDFUL,
	DOUBLE_HANDFUL,
	TRIPLE_HANDFUL,
	MISERY,
	SLAM,
	PETIT_AU_BOUT;
	
	private Player owner ;
	
	public Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Player owner)
	{
		this.owner = owner ;
	}
}
