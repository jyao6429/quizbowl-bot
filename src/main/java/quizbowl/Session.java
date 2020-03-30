package quizbowl;

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

	TextChannel channel;
	Scoreboard scoreboard;
	Member reader, buzzer;
	int tossup, prevScoreChange;
	QBState state;

	public Session(TextChannel t, Member r)
	{
		channel = t;
		startSession(r);
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
		if (event.getMember().equals(reader) && state == QBState.BUZZED)
		{
			scoreboard.addScore(buzzer, toAdd);
			prevScoreChange = toAdd;
			state = QBState.READING;

			if (toAdd > 0)
			{
				tossup++;
				channel.sendMessage("Toss Up: " + tossup).queue();
			}
		}
	}
	public void undoScore(CommandEvent event)
	{
		if (event.getMember().equals(reader) && state == QBState.READING)
		{
			scoreboard.addScore(buzzer, -prevScoreChange);
			if (prevScoreChange > 0)
				tossup--;
			prevScoreChange = 0;
			state = QBState.BUZZED;
		}
	}
	public void continueTU(CommandEvent event)
	{
		if (event.getMember().equals(reader) && state == QBState.READING)
		{
			tossup++;
			channel.sendMessage("Toss Up: " + tossup).queue();
		}
	}
	public void changeReader(CommandEvent event, Member newReader)
	{
		if (event.getMember().equals(reader) && state == QBState.READING)
		{
			reader = newReader;
		}
	}
	public void startSession(Member r)
	{
		scoreboard = new Scoreboard(this);
		reader = r;
		tossup = 1;
		state = QBState.READING;
		channel.sendMessage("Toss Up: " + tossup).queue();
	}
	public void stopSession()
	{
		scoreboard = null;
		reader = null;
		buzzer = null;
		tossup = 1;
		prevScoreChange = 0;
		state = QBState.STOPPED;
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
}
