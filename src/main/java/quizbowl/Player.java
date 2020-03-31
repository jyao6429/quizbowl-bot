package quizbowl;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public class Player implements Comparable<Player>
{
	private Member member;
	private int score, tens, powers, negs;
	private Scoreboard scoreboard;

	public Player(Member m, Scoreboard s)
	{
		member = m;
		scoreboard = s;
	}
	public void add(int toAdd)
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
				tens++;
				break;
			case -10:
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
		return member.getAsMention() + " - **" + score + "** pts " + "(15: **" + powers + "** | 10: **" + tens + "** | -5: **" + negs + "**)";
	}
}
