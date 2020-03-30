package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class PowerCommand extends Command
{
	public PowerCommand()
	{
		this.name = "15";
		this.help = "register a power";
		this.aliases = new String[]{"power"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.registerScore(event, 15);
	}
}
