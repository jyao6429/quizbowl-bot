package bot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import commands.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import quizbowl.QuizbowlHandler;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bot
{
	public final static Permission[] RECOMMENDED_PERMS = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
			Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE};
	public final static String SUCCESS_EMOJI = "\u2705";
	public final static String WARNING_EMOJI = "\u26A0";
	public final static String ERROR_EMOJI = "\u274C";

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
		QuizbowlHandler.setCategories(categories);

		// Build the client
		EventWaiter waiter = new EventWaiter();
		QuizbowlHandler.setWaiter(waiter);

		CommandClientBuilder client = new CommandClientBuilder();
		client.useDefaultGame();
		client.setOwnerId(ownerID);
		client.setEmojis(SUCCESS_EMOJI, WARNING_EMOJI, ERROR_EMOJI);
		client.setPrefix(prefix);

		// Add commands
		client.addCommands(
				new AboutCommand(Color.WHITE, "a quizbowl scorekeeping bot",
					new String[]{"Scorekeeping!","Team matches!","Version 2.0.4", "GitHub: https://github.com/jyao6429/quizbowl-bot"},
						RECOMMENDED_PERMS),
				new ReadCommand(),
				new MatchCommand(),
				new BuzzCommand(),
				new PowerCommand(),
				new TenCommand(),
				new ZeroCommand(),
				new NegCommand(),
				new ScoreCommand(),
				new ContinueCommand(),
				new StopCommand(),
				new UndoCommand(),
				new WithdrawCommand(),
				new ClearCommand(),
				new ChangeReaderCommand(),
				new PingCommand());

		JDABuilder.createDefault(botToken)
				// set the game for when the bot is loading
				  .setStatus(OnlineStatus.DO_NOT_DISTURB)
				  .setActivity(Activity.playing("loading..."))
				  .addEventListeners(waiter, client.build())
				  .build();
	}
}
