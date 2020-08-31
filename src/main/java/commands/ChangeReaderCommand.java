package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import quizbowl.QuizbowlHandler;

import java.util.List;

public class ChangeReaderCommand extends Command
{
	@SuppressWarnings("SpellCheckingInspection") public ChangeReaderCommand()
	{
		this.name = "change";
		this.help = "change the reader";
		this.aliases = new String[]{"switch", "setnewreader"};
		this.arguments = "<@mention>";
		this.guildOnly = true;
	}

	@Override protected void execute(CommandEvent event)
	{
		List<Member> mentioned = event.getMessage().getMentionedMembers();
		if (mentioned.size() < 1)
		{
			event.replyWarning("Please mention a new reader");
			return;
		}
		QuizbowlHandler.changeReader(event, mentioned.get(0));
	}
}
