package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class StopCommand extends Command
{
	public StopCommand()
	{
		this.name = "stop";
		this.help = "stop reading";
		this.aliases = new String[]{"end"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.stopSession(event);
	}
}
