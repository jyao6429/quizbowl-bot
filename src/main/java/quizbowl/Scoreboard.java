package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.list.TreeList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Scoreboard
{
	HashMap<Member, Player> players;
	ArrayList<Player> playerList;
	Session session;

	public Scoreboard(Session s)
	{
		players = new HashMap<>();
		playerList = new ArrayList<>();
		session = s;
	}

	public void addScore(Member member, int toAdd)
	{
		getPlayer(member).add(toAdd);
	}
	public int getScore(Member member)
	{
		return getPlayer(member).getScore();
	}
	public Player getPlayer(Member member)
	{
		Player player;
		try
		{
			player = players.get(member);
		}
		catch (Exception ex)
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

		String scores = "";
		for (int i = 0; i < playerList.size(); i++)
		{
			Player currentPlayer = playerList.get(i);
			scores += (i + 1) + ". " + currentPlayer + "\n";
		}
		MessageEmbed embed = new EmbedBuilder()
				.setTitle("Scoreboard")
			.setDescription("#" + session.getChannel())
			.setTimestamp(OffsetDateTime.now())
			.setThumbnail("https://cdn.discordapp.com/embed/avatars/0.png")
			.addField("Scores", scores, false)
			.build();

		session.getChannel().sendMessage(embed).queue();
	}
}
