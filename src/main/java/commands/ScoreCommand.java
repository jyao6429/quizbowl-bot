package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ScoreCommand extends Command
{
	public ScoreCommand()
	{
		this.name = "score";
		this.help = "print the current scores";
		this.aliases = new String[]{"leaderboard"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.printScores(event);
	}
}
