package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ReadCommand extends Command
{
	public ReadCommand()
	{
		this.name = "read";
		this.help = "start reading as the reader";
		this.guildOnly = true;
	}
	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.startSession(event);
	}
}
