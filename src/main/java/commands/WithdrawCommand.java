package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class WithdrawCommand extends Command
{
	public WithdrawCommand()
	{
		this.name = "withdraw";
		this.help = "removes your buzz from the queue";
		this.aliases = new String[]{"wd"};
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.withdrawBuzz(event);
	}
}
