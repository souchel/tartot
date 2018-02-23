package metier;

public class Player {
	private String username;
	private Team team;
	private Deck deck;
	
	public Player(String username)
	{
		this.username = username;
		//quand les players sont crees ils n ont pas encore d equipe d ou ils sont dans l equipe none
		team = Team.NONE;
		deck = new Deck();
	}
	public String getUsername()
	{
		return username ;
	}
	public Team getTeam()
	{
		return team;
	}
	public Deck getDeck()
	{
		return deck;
	}
}
