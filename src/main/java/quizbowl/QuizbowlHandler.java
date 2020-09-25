package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldCanBeLocal") public class QuizbowlHandler
{
	private static final HashMap<TextChannel, Match> matches = new HashMap<>();
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") private static ArrayList<String> categories = new ArrayList<>();
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
			match.setState(Match.MatchState.SELECTING);
		}
		else
		{
			match = new Match(event, true, 3, false, false);
			matches.put(event.getTextChannel(), match);
		}
		for (Team temp : teamList)
		{
			temp.setMatch(match);
		}
		match.setTeamList(teamList);
		match.setReader(event.getMember());

		TeamSelectionMenu.Builder builder = new TeamSelectionMenu.Builder()
				.allowTextInput(false)
				.useNumbers()
				.useCancelButton(false)
				.setEventWaiter(waiter)
				.setTimeout(15, TimeUnit.MINUTES)
				.setDescription("#" + event.getChannel().getName() + "\nPlease react to the number corresponding to your team")
				.setColor(Color.WHITE);
		for (Team temp : teamList)
		{
			builder = builder.addChoice(temp.getName());
		}
		builder.addChoice("Remove from match");
		builder.addChoice("Start match");
		HashMap<Member, Player> players = new HashMap<>();
		builder.setSelection((msg,msgReactionAddEvent) ->
		{
			int i = TeamSelectionMenu.getNumber(msgReactionAddEvent.getReactionEmote().getName());
			Member m = msgReactionAddEvent.getMember();
			if (i == teamList.size() + 1)
			{
				if (players.containsKey(m))
				{
					Player currentPlayer = players.get(m);
					currentPlayer.getTeam().removePlayer(currentPlayer);
					players.remove(m);
					assert m != null;
					event.replySuccess("Removed " + m.getAsMention() + " from the match");
				}
				else
				{
					assert m != null;
					event.replyWarning(m.getAsMention() + " You are not part of a team!");
				}
				return;
			}
			else if (i == teamList.size() + 2)
			{
				assert m != null;
				if (!m.equals(event.getMember()))
				{
					event.replyWarning(m.getAsMention() + " You are not the moderator!");
					return;
				}
				msg.delete().queue();
				match.initializeMatch(event, true, 3, false);
				event.replySuccess("Starting match");
				match.setTeamList(teamList);
				match.setPlayers(players);
				match.goToNextTU();
				return;
			}
			Team currentTeam = teamList.get(i - 1);
			if (currentTeam.getIsRole())
			{
				assert m != null;
				if (!m.getRoles().contains(currentTeam.getRole()))
				{
					event.replyWarning(m.getAsMention() + " You do not have the role for this team!");
					return;
				}
			}
			if (players.containsKey(m))
			{
				Player currentPlayer = players.get(m);
				if (currentPlayer.getTeam().equals(currentTeam))
				{
					assert m != null;
					event.replyWarning(m.getAsMention() + " You are already on that team!");
					return;
				}
				currentPlayer.getTeam().removePlayer(currentPlayer);
				currentTeam.addPlayer(currentPlayer);
				currentPlayer.setTeam(currentTeam);
				assert m != null;
				event.replySuccess("Switched " + m.getAsMention() + " to team " + currentTeam.getName());
			}
			else
			{
				Player currentPlayer = new Player(m, match, currentTeam);
				currentTeam.addPlayer(currentPlayer);
				players.put(m, currentPlayer);
				assert m != null;
				event.replySuccess("Added " + m.getAsMention() + " to team " + currentTeam.getName());
			}
		})
		.setCancel(msg -> match.setState(Match.MatchState.STOPPED))
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
			if (matches.get(event.getTextChannel()).getState() != Match.MatchState.STOPPED)
			{
				event.replyWarning("There is currently an ongoing match!");
				return;
			}
			if (numOfBonuses == 1)
			{
				event.replySuccess("Starting session with 1 bonus");
			}
			else
			{
				event.replySuccess("Starting session with " + numOfBonuses + " bonuses");
			}
			matches.get(event.getTextChannel()).initializeMatch(event, false, numOfBonuses, false);
			matches.get(event.getTextChannel()).goToNextTU();
		}
		else
		{
			if (numOfBonuses == 1)
			{
				event.replySuccess("Starting session with 1 bonus");
			}
			else
			{
				event.replySuccess("Starting session with " + numOfBonuses + " bonuses");
			}
			matches.put(event.getTextChannel(), new Match(event, false, numOfBonuses, false));
			matches.get(event.getTextChannel()).goToNextTU();
		}
	}
	public static void stopMatch(CommandEvent event, boolean force)
	{
		if (matches.containsKey(event.getTextChannel()))
		{
			matches.get(event.getTextChannel()).stopMatch(event, force);
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
