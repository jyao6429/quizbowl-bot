package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ReadCommand extends Command
{
	public ReadCommand()
	{
		this.name = "read";
		this.help = "start reading as the reader, with a customizable number of bonuses";
		this.arguments = "<numOfBonuses>";
		this.guildOnly = true;
	}
	@Override protected void execute(CommandEvent event)
	{
		try
		{
			int numOfBonuses = Integer.parseInt(event.getArgs());
			if (numOfBonuses < 0)
			{
				event.replyError("Please specify a positive number of bonuses");
				return;
			}
			if (numOfBonuses == 1)
			{
				event.replySuccess("Starting session with 1 bonus");
			}
			else
			{
				event.replySuccess("Starting session with " + numOfBonuses + " bonus");
			}
			QuizbowlHandler.startMatch(event, numOfBonuses);
		}
		catch (NumberFormatException ex)
		{
			event.replySuccess("Starting session with 0 bonuses");
			QuizbowlHandler.startMatch(event);
		}
	}
}
