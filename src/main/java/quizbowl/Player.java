package quizbowl;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public class Player implements Comparable<Player>
{
	private Member member;
	private int score, tens, powers, negs, bonuses;
	private Scoreboard scoreboard;

	public Player(Member m, Scoreboard s)
	{
		member = m;
		scoreboard = s;
	}
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
	public int getScore()
	{
		return score;
	}
	public Member getMember()
	{
		return member;
	}
	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}
	public int compareTo(@NotNull Player o)
	{
		return o.getScore() - this.getScore();
	}
	public String toString()
	{
		String toReturn = String.format("%s - **%d** pts (15: **%d** | 10: **%d** | -5: **%d**", member.getAsMention(), score, powers, tens, negs);
		if (scoreboard.getSession().getNumOfBonuses() > 0)
		{
			toReturn += " | B: **" + bonuses + "**";
		}
		toReturn += ")";
		return toReturn;
	}
}
