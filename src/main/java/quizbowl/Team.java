package quizbowl;

import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;

public class Team implements Comparable<Team>
{
	private String name;
	private Role role;
	private boolean isRole;
	private Match match;
	private ArrayList<Player> players;

	public Team(String n, Match m)
	{
		name = n;
		role = null;
		isRole = false;
		match = m;
		players = new ArrayList<>();
	}
	public Team(Role r, Match m)
	{
		role = r;
		name = role.getAsMention();
		isRole = true;
		match = m;
		players = new ArrayList<>();
	}
	public int addPlayer(Player p)
	{
		if (!players.contains(p))
		{
			players.add(p);
			return 0;
		}
		return -1;
	}
	public void removePlayer(Player p)
	{
		players.remove(p);
	}
	public int getScore()
	{
		int sum = 0;
		for (Player temp : players)
		{
			sum += temp.getScore();
		}
		return sum;
	}
	public int compareTo(Team o)
	{
		return o.getScore() - this.getScore();
	}
	public String toString()
	{
		int score, tens, powers, negs, bonuses;
		score = tens = powers = negs = bonuses = 0;
		for (Player temp : players)
		{
			score += temp.getScore();
			tens += temp.getTens();
			powers += temp.getPowers();
			negs += temp.getNegs();
			bonuses += temp.getBonuses();
		}
		String toReturn = String.format("%s - **%d** pts (15: **%d** | 10: **%d** | -5: **%d** | B: **%d**)", name, score, powers, tens, negs, bonuses);
		return toReturn;
	}
	public int getNumOfPlayers()
	{
		return players.size();
	}
	public ArrayList<Player> getPlayers()
	{
		return players;
	}
	public Match getMatch()
	{
		return match;
	}
	public String getName()
	{
		return name;
	}
	public Role getRole()
	{
		return role;
	}
	public boolean getIsRole()
	{
		return isRole;
	}
	public void setMatch(Match match)
	{
		this.match = match;
	}
}
