package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class TenCommand extends Command
{
	public TenCommand()
	{
		this.name = "10";
		this.help = "register a regular tossup";
		this.aliases = new String[]{"ten"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.registerScore(event, 10);
	}
}
