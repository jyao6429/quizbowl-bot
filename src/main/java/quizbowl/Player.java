package quizbowl;

import net.dv8tion.jda.api.entities.Member;

public class Player implements Comparable<Player>
{
	private final Member member;
	private int score, tens, powers, negs, bonuses;
	private final Match match;
	private Team team;

	public Player(Member m, Match s, Team t)
	{
		team = t;
		member = m;
		match = s;
	}
	public Player(Member m, Match s)
	{
		this(m, s, null);
	}
	public void add(Match.Points points)
	{
		add(points, false);
	}
	public void add(Match.Points points, boolean subtract)
	{
		int toAdd = subtract ? -1 : 1;
		switch (points)
		{
			case TEN:
				tens += toAdd;
				toAdd *= 10;
				break;
			case POWER:
				powers += toAdd;
				toAdd *= 15;
				break;
			case NEG:
				negs += toAdd;
				toAdd *= -5;
				break;
			case BONUS:
				bonuses += toAdd;
				toAdd *= 10;
				break;
			case ZERO:
				toAdd = 0;
				break;
		}
		score += toAdd;
	}
	/*
	public void add(int toAdd)
	{
		add(toAdd, false);
	}
	public void add(int toAdd, boolean isBonus)
	{
		switch (toAdd)
		{
			case 15:
				powers++;
				break;
			case -15:
				powers--;
				break;
			case 10:
				if (isBonus)
					bonuses++;
				else
					tens++;
				break;
			case -10:
				if (isBonus)
					bonuses--;
				else
					tens--;
				break;
			case -5:
				negs++;
				break;
			case 5:
				negs--;
				break;
		}
		score += toAdd;
	}
	 */
	public int getScore()
	{
		return score;
	}
	public int getTens()
	{
		return tens;
	}
	public int getPowers()
	{
		return powers;
	}
	public int getNegs()
	{
		return negs;
	}
	public int getBonuses()
	{
		return bonuses;
	}
	public Member getMember()
	{
		return member;
	}
	public Match getMatch()
	{
		return match;
	}
	public Team getTeam()
	{
		return team;
	}
	public void setTeam(Team team)
	{
		this.team = team;
	}
	public int compareTo(Player o)
	{
		return o.getScore() - this.getScore();
	}
	public String toString()
	{
		String toReturn = String.format("%s - **%d** pts (15: **%d** | 10: **%d** | -5: **%d**", member.getAsMention(), match.isTeam() ? score - 10 * bonuses : score, powers, tens, negs);
		if (match.getNumOfBonuses() > 0 && !match.isTeam())
		{
			toReturn += " | B: **" + bonuses + "**";
		}
		toReturn += ")";
		return toReturn;
	}
	public String getCSV()
	{
		return String.format("%s,%d,%d,%d,%d", member.getEffectiveName(), powers, tens, negs, score - 10 * bonuses);
	}
	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Player))
			return false;
		Player player = (Player) o;
		return member.equals(player.member);
	}
}
