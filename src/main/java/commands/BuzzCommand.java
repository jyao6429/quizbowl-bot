package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class BuzzCommand extends Command
{
	public BuzzCommand()
	{
		this.name = "buzz";
		this.help = "adds you to the buzz queue";
		//noinspection SpellCheckingInspection
		this.aliases = new String[]{"b", "bz", "buzzz"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.registerBuzz(event);
	}
}
