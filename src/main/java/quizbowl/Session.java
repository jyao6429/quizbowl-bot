package quizbowl;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Stack;

public class Session
{
	enum QBState
	{
		STOPPED,
		READING,
		BUZZED,
		BONUS
	}

	private TextChannel channel;
	private Scoreboard scoreboard;
	private Member reader;
	private int tossup, numOfBonuses, bonus;
	private QBState state;
	private Stack<BuzzEvent> lockedOut;
	private ArrayList<BuzzEvent> buzzQueue;
	private BuzzEvent prevTUBuzz;

	public Session(CommandEvent event, int bonusNum)
	{
		channel = event.getTextChannel();
		state = QBState.STOPPED;
		startSession(event, bonusNum);
	}
	private void goToNextTU()
	{
		tossup++;
		bonus = 0;
		state = QBState.READING;
		lockedOut = new Stack<>();
		buzzQueue = new ArrayList<>();
		channel.sendMessage("Toss Up: " + tossup).queue();
	}
	private void goToNextBonus()
	{
		bonus++;
		if (bonus > numOfBonuses)
		{
			goToNextTU();
			return;
		}
		state = QBState.BONUS;
		channel.sendMessage("Bonus: " + tossup + "-" + bonus).queue();
	}
	public void registerBuzz(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (state == QBState.BONUS)
		{
			event.replyError("There is currently a bonus");
			return;
		}
		BuzzEvent newBuzz = new BuzzEvent(event);
		if (lockedOut.contains(newBuzz) || buzzQueue.contains(newBuzz))
		{
			event.replyWarning(event.getMember().getAsMention() + " You have already buzzed!");
			return;
		}
		newBuzz.isFirst = buzzQueue.isEmpty();
		buzzQueue.add(newBuzz);
		event.reactSuccess();
		state = QBState.BUZZED;
	}
	public void registerScore(CommandEvent event, int toAdd)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.BUZZED && state != QBState.BONUS)
		{
			event.replyWarning("Nobody has buzzed");
			return;
		}
		BuzzEvent currentBuzz = new BuzzEvent(event);
		if (state == QBState.BUZZED)
		{
			currentBuzz = buzzQueue.remove(0);
			scoreboard.addScore(currentBuzz.member, toAdd);
		}
		else if (state == QBState.BONUS && (toAdd == 10 || toAdd == 0))
		{
			currentBuzz.isBonus = true;
			currentBuzz.member = lockedOut.peek().member;
			scoreboard.addScore(currentBuzz.member, toAdd, true);
		}
		else
		{
			event.replyWarning("Bonuses can only be 10 or 0 pts");
			return;
		}
		currentBuzz.scoreChange = toAdd;
		lockedOut.push(currentBuzz);

		event.reactSuccess();

		if ((toAdd > 0 && state == QBState.BUZZED) || state == QBState.BONUS)
		{
			prevTUBuzz = currentBuzz;
			goToNextBonus();
		}
		if (buzzQueue.size() == 0 && state != QBState.BONUS)
		{
			state = QBState.READING;
		}
	}
	public void undoScore(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}

		BuzzEvent toUndo;

		if (lockedOut.empty() && tossup == 1)
		{
			event.replyWarning("Nothing to undo");
			return;
		}
		if (lockedOut.empty())
		{
			toUndo = prevTUBuzz;
			tossup--;
			bonus = numOfBonuses;
		}
		else
		{
			toUndo = lockedOut.pop();
			if (bonus > 0)
				bonus--;
		}
		scoreboard.addScore(toUndo.member, -toUndo.scoreChange, toUndo.isBonus);

		if (toUndo.isBonus)
		{
			if (lockedOut.isEmpty())
			{
				lockedOut.push(toUndo);
				toUndo.scoreChange = 0;
			}
			state = QBState.BONUS;
		}
		else
		{
			buzzQueue.add(0, toUndo);
			state = QBState.BUZZED;
		}
		event.reactSuccess();
	}
	public void withdrawBuzz(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (state != QBState.BUZZED)
		{
			event.replyWarning("Nothing to withdraw");
			return;
		}

		int index = buzzQueue.indexOf(new BuzzEvent(event));
		if (index < 0)
		{
			event.reactError();
		}
		else
		{
			BuzzEvent toRemove = buzzQueue.get(index);
			if (toRemove.isFirst)
			{
				event.replyWarning("You are the first buzzer, you cannot withdraw!");
				return;
			}
			else
			{
				buzzQueue.remove(index);
				event.reactSuccess();
			}
		}
		if (buzzQueue.isEmpty())
		{
			state = QBState.READING;
		}
	}
	public void clearBuzzQueue(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.BUZZED)
		{
			event.replyWarning("Nothing to clear");
			return;
		}

		buzzQueue.clear();
		state = QBState.READING;
		event.replySuccess("Cleared the buzz queue");
	}
	public void continueTU(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't continue, someone has buzzed!");
			return;
		}
		goToNextTU();
		event.reactSuccess();
	}
	public void changeReader(CommandEvent event, Member newReader)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session in this text channel");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't change readers, someone has buzzed!");
			return;
		}
		reader = newReader;
		event.reactSuccess();
	}
	public void startSession(CommandEvent event, int bonusNum)
	{
		if (state != QBState.STOPPED)
		{
			event.replyError("There is already an ongoing session");
			return;
		}
		scoreboard = new Scoreboard(this);
		reader = event.getMember();
		tossup = 0;
		bonus = 0;
		numOfBonuses = bonusNum;
		state = QBState.READING;
		goToNextTU();
	}
	public void stopSession(CommandEvent event)
	{
		if (state == QBState.STOPPED)
		{
			event.replyError("There is no active session to stop");
			return;
		}
		if (!event.getMember().equals(reader))
		{
			event.replyWarning("You are not the reader");
			return;
		}
		if (state != QBState.READING)
		{
			event.replyWarning("Can't stop, someone has buzzed!");
			return;
		}

		printScores();
		scoreboard = null;
		reader = null;
		prevTUBuzz = null;
		lockedOut = null;
		buzzQueue = null;
		tossup = 0;
		bonus = 0;
		numOfBonuses = 0;
		state = QBState.STOPPED;
		event.replySuccess("Session stopped");
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
	public int getNumOfBonuses()
	{
		return numOfBonuses;
	}
}
class BuzzEvent
{
	public CommandEvent event;
	public Member member;
	public int scoreChange;
	public boolean isFirst, isBonus;

	public BuzzEvent(CommandEvent event)
	{
		this.event = event;
		member = event.getMember();
	}
	public boolean equals(Object o)
	{
		BuzzEvent other = (BuzzEvent) o;
		return this.member.equals(other.member);
	}
}
