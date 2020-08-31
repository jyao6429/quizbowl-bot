package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class QuizbowlHandler
{
	private static HashMap<TextChannel, Match> matches = new HashMap<>();
	private static ArrayList<String> categories;
	private static EventWaiter waiter;

	public static void setWaiter(EventWaiter w)
	{
		waiter = w;
	}
	public static void setCategories(String[] cats)
	{
		categories = new ArrayList<>(Arrays.asList(cats));
	}
	public static void startTeamMatch(CommandEvent event, ArrayList<Team> teamList)
	{
		Match match;
		if (matches.containsKey(event.getTextChannel()))
		{
			match = matches.get(event.getTextChannel());
			if (match.getState() != Match.MatchState.STOPPED)
			{
				event.replyWarning("There is currently an ongoing match!");
				return;
			}
			match.initializeMatch(event, true, 3, false);
		}
		else
		{
			match = new Match(event, true, 3, false);
			matches.put(event.getTextChannel(), match);
		}
		for (Team temp : teamList)
		{
			temp.setMatch(match);
		}
		match.setTeamList(teamList);

		TeamSelectionMenu.Builder builder = new TeamSelectionMenu.Builder()
				.allowTextInput(false)
				.useNumbers()
				.useCancelButton(false)
				.setEventWaiter(waiter)
				.setTimeout(5, TimeUnit.MINUTES)
				.setDescription("Please react to the number corresponding to your team");
		for (Team temp : teamList)
		{
			builder = builder.addChoice(temp.getName());
		}
		builder.addChoice("Remove from teams");
		builder.addChoice("Start Match");
		HashMap<Member, Player> players = new HashMap<>();
		builder.setSelection((msg,msgReactionAddEvent) ->
				{
					int i = TeamSelectionMenu.getNumber(msgReactionAddEvent.getReactionEmote().getName());
					Member m = msgReactionAddEvent.getMember();
					//msg.removeReaction(msgReactionAddEvent.getReactionEmote().getEmote(), m.getUser());
					if (i == teamList.size() + 1)
					{
						if (players.containsKey(m))
						{
							Player currentPlayer = players.get(m);
							currentPlayer.getTeam().removePlayer(currentPlayer);
							players.remove(m);
							match.getChannel().sendMessage("Removed " + currentPlayer.getMember().getAsMention() + " from the match").queue();
						}
						return;
					}
					else if (i == teamList.size() + 2)
					{
						match.setTeamList(teamList);
						match.setPlayers(players);
						match.goToNextTU();
						return;
					}
					Team currentTeam = teamList.get(i - 1);
					if (players.containsKey(m))
					{
						Player currentPlayer = players.get(m);
						currentPlayer.getTeam().removePlayer(currentPlayer);
						currentPlayer.setTeam(currentTeam);
						match.getChannel().sendMessage("Switched " + currentPlayer.getMember().getAsMention() + " to team " + currentTeam.getName()).queue();
					}
					else
					{
						Player currentPlayer = new Player(m, match, currentTeam);
						currentTeam.addPlayer(currentPlayer);
						players.put(m, currentPlayer);
						match.getChannel().sendMessage("Added " + currentPlayer.getMember().getAsMention() + " to team " + currentTeam.getName()).queue();
					}
				})
				.setCancel((msg) -> {})
				;
		builder.build().display(match.getChannel());

	}
	public static void startMatch(CommandEvent event)
	{
		startMatch(event,0);
	}
	public static void startMatch(CommandEvent event, int numOfBonuses)
	{
		/*
		if (!categories.contains(event.getTextChannel().getName()))
		{
			event.replyWarning("Please use this bot in dedicated quiz bowl channels!");
			return;
		}
		*/
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).initializeMatch(event, false, numOfBonuses, false);
			matches.get(event.getTextChannel()).goToNextTU();
		}
		else
		{
			matches.put(event.getTextChannel(), new Match(event, false, numOfBonuses, false));
			matches.get(event.getTextChannel()).goToNextTU();
		}
	}
	public static void stopMatch(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).stopMatch(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static Match getMatch(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			return matches.get(event.getTextChannel());
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
		return null;
	}
	public static void registerScore(CommandEvent event, int toAdd)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).registerScore(event, toAdd);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void registerBuzz(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).registerBuzz(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void withdrawBuzz(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).withdrawBuzz(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void clearBuzzQueue(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).clearBuzzQueue(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void undoScore(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).undoScore(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void continueTU(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).continueTU(event);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void changeReader(CommandEvent event, Member newReader)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).changeReader(event, newReader);
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}
	public static void printScores(CommandEvent event)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).printScoreboard();
		}
		else
		{
			event.replyError("There is no active match in this text channel");
		}
	}

}
