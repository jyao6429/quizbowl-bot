package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import quizbowl.QuizbowlHandler;

public class UndoCommand extends Command
{
	public UndoCommand()
	{
		this.name = "undo";
		this.help = "undo the last scoring command";
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		QuizbowlHandler.undoScore(event);
	}
}
