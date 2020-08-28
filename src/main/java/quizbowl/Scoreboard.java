package quizbowl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Scoreboard
{
	private HashMap<Member, Player> players;
	private ArrayList<Player> playerList;
	private Session session;

	public Scoreboard(Session s)
	{
		players = new HashMap<>();
		playerList = new ArrayList<>();
		session = s;
	}

	public void addScore(Member member, int toAdd)
	{
		addScore(member, toAdd, false);
	}
	public void addScore(Member member, int toAdd, boolean isBonus)
	{
		getPlayer(member).add(toAdd, isBonus);
	}
	public int getScore(Member member)
	{
		return getPlayer(member).getScore();
	}
	public Player getPlayer(Member member)
	{
		Player player;
		if (players.containsKey(member))
		{
			player = players.get(member);
		}
		else
		{
			player = new Player(member, this);
			playerList.add(player);
			players.put(member, player);
		}
		return player;
	}
	public Session getSession()
	{
		return session;
	}
	public void printScoreboard()
	{
		Collections.sort(playerList);

		StringBuilder scores = new StringBuilder();
		for (int i = 0; i < playerList.size(); i++)
		{
			Player currentPlayer = playerList.get(i);
			scores.append(i + 1).append(". ").append(currentPlayer).append("\n");
		}
		MessageEmbed embed = new EmbedBuilder()
				.setTitle("Scoreboard")
			.setDescription("#" + session.getChannel().getName() + "\nToss Ups: " + session.getTossup())
			.setTimestamp(OffsetDateTime.now())
			.setThumbnail("https://raw.githubusercontent.com/jyao6429/quizbowl-bot/master/images/qbat%20icon%20square.png")
			.addField("Scores", scores.toString(), false)
			.build();

		session.getChannel().sendMessage(embed).queue();
	}
}
