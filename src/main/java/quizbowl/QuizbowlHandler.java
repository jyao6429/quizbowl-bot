package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;

public class QuizbowlHandler
{
	private static HashMap<TextChannel, Session> sessions = new HashMap<>();

	public static void startSession(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).startSession(event);
		}
		catch (Exception ex)
		{
			sessions.put(event.getTextChannel(), new Session(event));
		}
	}
	public static void stopSession(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).stopSession(event);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static Session getSession(CommandEvent event)
	{
		try
		{
			return sessions.get(event.getTextChannel());
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
		return null;
	}
	public static void registerScore(CommandEvent event, int toAdd)
	{
		try
		{
			sessions.get(event.getTextChannel()).registerScore(event, toAdd);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void registerBuzz(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).registerBuzz(event);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void undoScore(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).undoScore(event);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void continueTU(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).continueTU(event);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void changeReader(CommandEvent event, Member newReader)
	{
		try
		{
			sessions.get(event.getTextChannel()).changeReader(event, newReader);
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}
	public static void printScores(CommandEvent event)
	{
		try
		{
			sessions.get(event.getTextChannel()).printScores();
		}
		catch (Exception ex)
		{
			event.replyError("There is no active session in this text channel");
		}
	}

}
