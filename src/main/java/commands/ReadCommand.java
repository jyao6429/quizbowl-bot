package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ReadCommand extends Command
{
	public ReadCommand()
	{
		this.name = "read";
		this.help = "start reading";
		this.guildOnly = true;
	}
	@Override protected void execute(CommandEvent event)
	{

	}
}
