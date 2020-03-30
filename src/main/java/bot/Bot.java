package bot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import commands.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bot
{
	public final static Permission[] RECOMMENDED_PERMS = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
			Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES};

	public static void main(String[] args) throws LoginException
	{
		// Import config file
		List<String> list;
		try
		{
			list = Files.readAllLines(Paths.get("config.txt"));
		}
		catch (IOException ex)
		{
			System.out.println("Unable to read config.txt");
			ex.printStackTrace();
			return;
		}

		// Get all the config values
		String botToken = list.get(0);
		String ownerID = list.get(1);
		String prefix = list.get(2);
		String[] categories = list.get(3).split(",");

		// Build the client
		EventWaiter waiter = new EventWaiter();

		CommandClientBuilder client = new CommandClientBuilder();
		client.useDefaultGame();
		client.setOwnerId(ownerID);
		client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");
		client.setPrefix(prefix);

		// Add commands
		client.addCommands(
				new AboutCommand(Color.BLUE, "a quizbowl scorekeeping bot",
						new String[]{"Read stuff","Buzz","Score stuff"},
						RECOMMENDED_PERMS),
				new PingCommand(),
				new BuzzCommand(),
				new ChangeReaderCommand(),
				new ContinueCommand(),
				new NegCommand(),
				new PowerCommand(),
				new ReadCommand(),
				new StopCommand(),
				new TenCommand(),
				new UndoCommand(),
				new ZeroCommand());

		new JDABuilder(botToken)
				// set the game for when the bot is loading
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setActivity(Activity.playing("loading..."))
				.addEventListeners(waiter, client.build())
				.build();
	}
}
