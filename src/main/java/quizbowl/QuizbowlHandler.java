package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class QuizbowlHandler
{
	private static HashMap<TextChannel, Session> sessions = new HashMap<>();
	private static ArrayList<String> categories;

	public static void setCategories(String[] cats)
	{
		categories = new ArrayList<>(Arrays.asList(cats));
	}
	public static void startSession(CommandEvent event)
	{
		if (!categories.contains(event.getTextChannel().getName()))
		{
			event.replyWarning("Please use this bot in dedicated quiz bowl channels!");
			return;
		}
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).startSession(event);
		}
		else
		{
			sessions.put(event.getTextChannel(), new Session(event));
		}
	}
	public static void stopSession(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).stopSession(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static Session getSession(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			return sessions.get(event.getTextChannel());
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
		return null;
	}
	public static void registerScore(CommandEvent event, int toAdd)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).registerScore(event, toAdd);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void registerBuzz(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).registerBuzz(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void withdrawBuzz(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).withdrawBuzz(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void clearBuzzQueue(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).clearBuzzQueue(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void undoScore(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).undoScore(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void continueTU(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).continueTU(event);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void changeReader(CommandEvent event, Member newReader)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).changeReader(event, newReader);
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void printScores(CommandEvent event)
	{
		if (sessions.containsKey(event.getTextChannel()))
		{
			sessions.get(event.getTextChannel()).printScores();
		}
		else
		{
			event.replyError("There is no active session in this text channel");
		}
	}

}
