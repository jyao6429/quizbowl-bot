package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

public class Match
{
	enum Points
	{
		TEN,
		POWER,
		NEG,
		BONUS,
		ZERO
	}
	enum MatchState
	{
		STOPPED,
		STARTING,
		READING,
		BUZZED,
		BONUS,
		BOUNCE
	}
	// Channel variables
	private final TextChannel channel;
	private Member reader;
	private final String BEE = "\uD83D\uDC1D";

	// Player and Team variables
	private HashMap<Member, Player> players;
	private ArrayList<Player> playerList;
	private ArrayList<Team> teamList;
	private boolean isTeam, isBounce;

	// State variables
	private MatchState state;
	private int tossup, bonus, numOfBonuses;
	private ArrayList<Buzz> buzzQueue;
	private Stack<Buzz> lockedOut;
	private Stack<MatchEvent> currentMatchEvents;
	private Stack<MatchEvent> previousMatchEvents;

	// Constructor
	public Match(CommandEvent event, boolean team, int bonusNum, boolean bounce)
	{
		channel = event.getTextChannel();
		state = MatchState.STOPPED;
		initializeMatch(event, team, bonusNum, bounce);
	}
	public void initializeMatch(CommandEvent event, boolean team, int bonusNum, boolean bounce)
	{
		if (state != MatchState.STOPPED)
		{
			event.replyWarning("There is currently an ongoing match!");
			return;
		}
		state = MatchState.STARTING;
		reader = event.getMember();
		tossup = 0;
		bonus = 0;
		numOfBonuses = bonusNum;
		isTeam = team;
		isBounce = bounce;
		players = new HashMap<>();
		playerList = new ArrayList<>();
		teamList = new ArrayList<>();
	}
	public void stopMatch(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session to stop");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != MatchState.READING && state != MatchState.STARTING)
		{
			event.replyWarning("Can't stop, someone has buzzed!");
			return;
		}
		printScoreboard();
		reader = null;
		players = null;
		playerList = null;
		teamList = null;
		lockedOut = null;
		buzzQueue = null;
		currentMatchEvents = null;
		previousMatchEvents = null;
		tossup = 0;
		bonus = 0;
		numOfBonuses = 0;
		isTeam = false;
		isBounce = false;
		state = MatchState.STOPPED;
		event.replySuccess("Session stopped");
	}
	public void goToNextTU()
	{
		tossup++;
		bonus = 0;
		state = MatchState.READING;
		buzzQueue = new ArrayList<>();
		lockedOut = new Stack<>();
		previousMatchEvents = currentMatchEvents;
		currentMatchEvents = new Stack<>();
		channel.sendMessage("Toss Up: " + tossup).queue();
	}
	public void goToNextBonus()
	{
		bonus++;
		if (bonus > numOfBonuses)
		{
			goToNextTU();
			return;
		}
		state = MatchState.BONUS;
		channel.sendMessage("Bonus: " + tossup + "-" + bonus).queue();
	}
	public void registerBuzz(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (state == MatchState.BONUS || state == MatchState.BOUNCE)
		{
			event.replyError("There is currently a bonus");
			return;
		}
		Buzz newBuzz = new Buzz(event, getPlayer(event.getMember()), tossup, buzzQueue.isEmpty());
		if (newBuzz.player == null)
		{
			event.replyError(event.getMember().getAsMention() + " You are not part of a team!");
			return;
		}
		if (lockedOut.contains(newBuzz) || buzzQueue.contains(newBuzz))
		{
			String message = (isTeam) ? " Your team has already buzzed!" : " You have already buzzed!";
			event.replyWarning(event.getMember().getAsMention() + message);
			return;
		}
		buzzQueue.add(newBuzz);
		state = MatchState.BUZZED;
		event.reactSuccess();
		if (newBuzz.isFirst)
			event.getMessage().addReaction(BEE).queue();
	}
	public void registerScore(CommandEvent event, int toAdd)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state == MatchState.READING)
		{
			event.replyWarning("Nothing to score");
			return;
		}

		// Branch between possible match states
		MatchEvent currentEvent;
		if (state == MatchState.BUZZED)
		{
			// Handle the buzzer
			Buzz currentBuzz = buzzQueue.remove(0);
			currentBuzz.event.getMessage().clearReactions(BEE).queue();

			// Handle event and points change
			currentEvent = new MatchEvent(event, tossup, currentBuzz.player, getPoints(toAdd, false), false, currentBuzz);
			currentBuzz.player.add(currentEvent.scoreChange);
			currentMatchEvents.push(currentEvent);
			event.reactSuccess();

			// Handle end
			if (buzzQueue.size() == 0)
			{
				state = MatchState.READING;
			}
			else
			{
				buzzQueue.get(0).event.getMessage().addReaction(BEE).queue();
			}
			if (toAdd > 0)
			{
				goToNextBonus();
			}
		}
		else if (state == MatchState.BONUS)
		{
			if (toAdd != 10 && toAdd != 0)
			{
				event.replyWarning("Bonuses can only be 10 or 0 pts");
				return;
			}
			// Handle event and points change
			currentEvent = new MatchEvent(event, tossup, currentMatchEvents.peek().player, getPoints(toAdd, true), true);
			currentMatchEvents.peek().player.add(currentEvent.scoreChange);
			currentMatchEvents.push(currentEvent);
			event.reactSuccess();

			// end
			goToNextBonus();
		}
	}
	public void undoScore(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}

		MatchEvent toUndo;

		if (currentMatchEvents.empty() && tossup == 1)
		{
			event.replyWarning("Nothing to undo");
			return;
		}
		if (currentMatchEvents.empty())
		{
			if (previousMatchEvents == null)
			{
				event.replyError("Can't undo past the previous tossup");
				return;
			}

			currentMatchEvents = previousMatchEvents;
			previousMatchEvents = null;
			tossup--;
			if (currentMatchEvents.isEmpty())
			{
				bonus = 0;
				return;
			}
			toUndo = currentMatchEvents.pop();
			if (toUndo.isBonus)
				bonus = numOfBonuses;
			else
				bonus = 0;
		}
		else
		{
			toUndo = currentMatchEvents.pop();
			if (bonus > 0)
				bonus--;
		}
		toUndo.player.add(toUndo.scoreChange, true);

		if (toUndo.isBonus)
		{
			state = MatchState.BONUS;
		}
		else
		{
			buzzQueue.add(0, toUndo.buzz);
			state = MatchState.BUZZED;
			if (!lockedOut.isEmpty() && lockedOut.peek().equals(toUndo.buzz))
				lockedOut.pop();
		}
		event.reactSuccess();
	}
	public void withdrawBuzz(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (state != MatchState.BUZZED)
		{
			event.replyWarning("Nothing to withdraw");
			return;
		}

		int index = buzzQueue.indexOf(new Buzz(event, getPlayer(event.getMember()), tossup, false));
		if (index < 0)
		{
			event.reactError();
		}
		else
		{
			Buzz toRemove = buzzQueue.get(index);
			if (toRemove.isFirst)
			{
				event.replyWarning("You are the first buzzer, you cannot withdraw!");
				return;
			}
			else
			{
				if (index == 0)
				{
					toRemove.event.getMessage().clearReactions(BEE).queue();
					if (buzzQueue.size() > 1)
						buzzQueue.get(1).event.getMessage().addReaction(BEE).queue();
				}
				buzzQueue.remove(index);
				event.reactSuccess();
			}
		}
		if (buzzQueue.isEmpty())
		{
			state = MatchState.READING;
		}
	}
	public void clearBuzzQueue(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != MatchState.BUZZED)
		{
			event.replyWarning("Nothing to clear");
			return;
		}

		buzzQueue.clear();
		state = MatchState.READING;
		event.replySuccess("Cleared the buzz queue");
	}
	public void continueTU(CommandEvent event)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != MatchState.READING)
		{
			event.replyWarning("Can't continue, someone has buzzed!");
			return;
		}
		goToNextTU();
		event.reactSuccess();
	}
	public void changeReader(CommandEvent event, Member newReader)
	{
		if (state == MatchState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != MatchState.READING)
		{
			event.replyWarning("Can't change readers, someone has buzzed!");
			return;
		}
		reader = newReader;
		event.reactSuccess();
	}

	public void addPlayer(Player p)
	{
		if (!playerList.contains(p))
		{
			playerList.add(p);
			players.put(p.getMember(), p);
		}
	}
	public Player getPlayer(Member member)
	{
		Player player = null;
		if (players.containsKey(member))
		{
			player = players.get(member);
		}
		else if (!isTeam)
		{
			player = new Player(member, this);
			playerList.add(player);
			players.put(member, player);
		}
		return player;
	}
	public void addPlayer(Member member, Team team)
	{
		Player player = new Player(member, this, team);
		if (!players.containsKey(member))
		{
			players.put(member, player);
			playerList.add(player);
		}
	}
	public void addTeam(Team team)
	{
		if (!teamList.contains(team))
		{
			teamList.add(team);
		}
	}
	public void setTeamList(ArrayList<Team> teams)
	{
		if (teams != null && !teams.isEmpty())
		{
			teamList = teams;
		}
	}
	public void setPlayers(HashMap<Member, Player> players1)
	{
		players = players1;
		playerList = new ArrayList<>(players.values());
	}
	public static Points getPoints(int points, boolean isBonus)
	{
		switch (points)
		{
			case 10:
				if (isBonus)
					return Points.BONUS;
				else
					return Points.TEN;
			case 15:
				return Points.POWER;
			case -5:
				return Points.NEG;
			case 0:
				return Points.ZERO;
		}
		return Points.ZERO;
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

		MessageEmbed embed;
		if (isTeam)
		{
			Collections.sort(teamList);
			StringBuilder teamScores = new StringBuilder();
			for (int i = 0; i < teamList.size(); i++)
			{
				Team currentTeam = teamList.get(i);
				teamScores.append(i + 1).append(". ").append(currentTeam).append("\n");
			}
			embed = new EmbedBuilder()
					.setTitle("Scoreboard")
					.setColor(Color.WHITE)
					.setDescription("#" + channel.getName() + "\nToss Ups: " + tossup)
					.setTimestamp(OffsetDateTime.now())
					.setThumbnail("https://raw.githubusercontent.com/jyao6429/quizbowl-bot/master/images/qbat%20icon%20square.png")
					.addField("Team Scores", teamScores.toString(), false)
					.addField("Individual Scores", scores.toString(), false)
					.build();
		}
		else
		{
			embed = new EmbedBuilder()
					.setTitle("Scoreboard")
					.setColor(Color.WHITE)
					.setDescription("#" + channel.getName() + "\nToss Ups: " + tossup)
					.setTimestamp(OffsetDateTime.now())
					.setThumbnail("https://raw.githubusercontent.com/jyao6429/quizbowl-bot/master/images/qbat%20icon%20square.png")
					.addField("Scores", scores.toString(), false)
					.build();
		}
		channel.sendMessage(embed).queue();
	}

	public boolean isTeam()
	{
		return isTeam;
	}
	public int getNumOfBonuses()
	{
		return numOfBonuses;
	}
	public TextChannel getChannel()
	{
		return channel;
	}
	public MatchState getState()
	{
		return state;
	}
}
class Buzz
{
	public final CommandEvent event;
	public final Player player;
	public final int tossup;
	public final boolean isFirst;

	public Buzz(CommandEvent e, Player p, int tu, boolean f)
	{
		event = e;
		player = p;
		tossup = tu;
		isFirst = f;
	}
	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Buzz buzz = (Buzz) o;

		if (tossup != buzz.tossup)
			return false;
		if (player.getTeam() != null && buzz.player.getTeam() != null)
			return player.getTeam().equals(buzz.player.getTeam());
		return player.equals(buzz.player);
	}
}
class MatchEvent
{
	public final CommandEvent event;
	public final Buzz buzz;
	public final Player player;
	public final boolean isBonus;
	public final Match.Points scoreChange;
	public final int tossup;

	public MatchEvent(CommandEvent e, int tu, Player p, Match.Points s, boolean b)
	{
		this(e, tu, p, s, b, null);
	}
	public MatchEvent(CommandEvent e, int tu, Player p, Match.Points s, boolean bo, Buzz bu)
	{
		event = e;
		tossup = tu;
		player = p;
		isBonus = bo;
		scoreChange = s;
		buzz = bu;
	}
}
