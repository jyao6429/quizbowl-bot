package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class ContinueCommand extends Command
{
	public ContinueCommand()
	{
		this.name = "next";
		this.help = "continue to the next tossup";
		this.aliases = new String[]{"continue"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.continueTU(event);
	}
}
