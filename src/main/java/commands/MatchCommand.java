package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import quizbowl.QuizbowlHandler;
import quizbowl.Team;

import java.util.ArrayList;

public class MatchCommand extends Command
{
	public MatchCommand()
	{
		this.name = "match";
		this.help = "start a match as the moderator with 2-8 teams and 3 bonuses";
		this.arguments = "<@team1>,<@team2>,<@team3>,...";
		this.guildOnly = true;
	}
	@Override protected void execute(CommandEvent event)
	{
		String[] teams = event.getArgs().split(",");
		if (teams.length < 2)
		{
			event.replyError("Please specify at least 2 teams");
			return;
		}
		else if (teams.length > 8)
		{
			event.replyError("Please specify fewer than 8 teams");
			return;
		}
		ArrayList<Team> teamList = new ArrayList<>();
		for (String temp : teams)
		{
			temp = temp.trim();
			String numOnly = temp.replaceAll("[^\\d]", "");
			Role tempR = null;
			if (!numOnly.isEmpty())
				tempR = event.getGuild().getRoleById(numOnly);
			if (tempR == null)
			{
				teamList.add(new Team(temp, null));
			}
			else
			{
				teamList.add(new Team(tempR, null));
			}
		}
		QuizbowlHandler.startTeamMatch(event, teamList);
	}
}
