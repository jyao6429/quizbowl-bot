package quizbowl;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Session
{
	enum QBState
	{
		STOPPED,
		READING,
		BUZZED
	}

	private TextChannel channel;
	private Scoreboard scoreboard;
	private Member reader, buzzer;
	private int tossup, prevScoreChange;
	private QBState state;

	public Session(CommandEvent event)
	{
		channel = event.getTextChannel();
		startSession(event);
	}
	public void registerBuzz(CommandEvent event)
	{
		if (state == QBState.BUZZED)
			return;

		buzzer = event.getMember();
		state = QBState.BUZZED;

		event.reply(event.getMember().getAsMention() + " buzzed");
	}
	public void registerScore(CommandEvent event, int toAdd)
	{
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.BUZZED)
		{
			event.replyWarning("Nobody has buzzed");
			return;
		}

		scoreboard.addScore(buzzer, toAdd);
		prevScoreChange = toAdd;
		state = QBState.READING;

		if (toAdd > 0)
		{
			tossup++;
			channel.sendMessage("Toss Up: " + tossup).queue();
		}
	}
	public void undoScore(CommandEvent event)
	{
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING || tossup == 1)
		{
			event.replyWarning("Nothing to undo");
			return;
		}

		scoreboard.addScore(buzzer, -prevScoreChange);
		if (prevScoreChange > 0)
			tossup--;
		prevScoreChange = 0;
		state = QBState.BUZZED;
	}
	public void continueTU(CommandEvent event)
	{
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't continue, has someone buzzed?");
			return;
		}

		tossup++;
		channel.sendMessage("Toss Up: " + tossup).queue();
	}
	public void changeReader(CommandEvent event, Member newReader)
	{
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't change readers, has someone buzzed?");
			return;
		}
		reader = newReader;
	}
	public void startSession(CommandEvent event)
	{
		if (state != QBState.STOPPED)
		{
			event.replyWarning("There is already an ongoing session");
			return;
		}
		scoreboard = new Scoreboard(this);
		reader = event.getMember();
		tossup = 1;
		state = QBState.READING;
		channel.sendMessage("Toss Up: " + tossup).queue();
	}
	public void stopSession(CommandEvent event)
	{
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't stop, has someone buzzed?");
			return;
		}

		printScores();
		scoreboard = null;
		reader = null;
		buzzer = null;
		tossup = 1;
		prevScoreChange = 0;
		state = QBState.STOPPED;
		event.reply("Session stopped");
	}
	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}
	public void printScores()
	{
		scoreboard.printScoreboard();
	}
	public TextChannel getChannel()
	{
		return channel;
	}
	public Member getReader()
	{
		return reader;
	}
	public QBState getState()
	{
		return state;
	}
	public int getTossup()
	{
		return tossup;
	}
}
