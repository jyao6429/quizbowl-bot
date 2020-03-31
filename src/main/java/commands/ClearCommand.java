package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ClearCommand extends Command
{
	public ClearCommand()
	{
		this.name = "clear";
		this.help = "clears the buzz queue";
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.clearBuzzQueue(event);
	}
}
