package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class NegCommand extends Command
{
	public NegCommand()
	{
		this.name = "-5";
		this.help = "register a neg";
		this.aliases = new String[]{"neg", "neg 5"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.registerScore(event, -5);
	}
}
