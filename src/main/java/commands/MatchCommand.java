package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import quizbowl.QuizbowlHandler;
import quizbowl.Team;

import java.util.ArrayList;
import java.util.List;

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
		ArrayList<Team> teamList = new ArrayList<>();
		for (String temp : teams)
		{
			List<Role> tempR = event.getGuild().getRolesByName(temp, true);
			if (tempR.size() == 0)
			{
				teamList.add(new Team(temp, null));
			}
			else
			{
				teamList.add(new Team(tempR.get(0), null));
			}
		}
		QuizbowlHandler.startTeamMatch(event, teamList);
	}
}
