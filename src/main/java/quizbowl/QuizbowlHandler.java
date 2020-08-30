package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class QuizbowlHandler
{
	private static HashMap<TextChannel, Match> matches = new HashMap<>();
	private static ArrayList<String> categories;

	public static void setCategories(String[] cats)
	{
		categories = new ArrayList<>(Arrays.asList(cats));
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
