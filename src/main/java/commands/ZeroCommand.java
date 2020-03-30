package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ZeroCommand extends Command
{
	public ZeroCommand()
	{
		this.name = "0";
		this.help = "register a no penalty";
		this.aliases = new String[]{"zero"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.registerScore(event, 0);
	}
}
