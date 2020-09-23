package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class StopCommand extends Command
{
	public StopCommand()
	{
		this.name = "stop";
		this.help = "stop reading, with the option to forcefully stop";
		this.arguments = "<force>";
		this.aliases = new String[]{"end"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		boolean force = event.getArgs().toLowerCase().contains("force") || event.getArgs().toLowerCase().equals("f");
		QuizbowlHandler.stopMatch(event, force);
	}
}
