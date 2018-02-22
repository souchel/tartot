package metier;

public class Player {
	private String username;
	private Team team;
	
	public Player(String username)
	{
		this.username = username;
		//quand les players sont crees ils n ont pas encore d equipe d ou ils sont dans l equipe none
		team = Team.NONE;
	}
	public String getUsername()
	{
		return username ;
	}
	public Team getTeam()
	{
		return team;
	}
}
