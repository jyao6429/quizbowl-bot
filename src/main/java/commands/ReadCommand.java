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
			QuizbowlHandler.startMatch(event, numOfBonuses);
		}
		catch (NumberFormatException ex)
		{
			QuizbowlHandler.startMatch(event);
		}
	}
}
