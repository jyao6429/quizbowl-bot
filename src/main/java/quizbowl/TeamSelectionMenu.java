package quizbowl;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("SpellCheckingInspection") public class TeamSelectionMenu extends Menu
{
	public final static String[] NUMBERS = new String[] { "1\u20E3", "2\u20E3", "3\u20E3", "4\u20E3", "5\u20E3", "6\u20E3", "7\u20E3", "8\u20E3", "9\u20E3", "\uD83D\uDD1F" };
	public final static String[] LETTERS = new String[] { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF" };
	public final static String CANCEL = "\u274C";
	private final Color color;
	private final String text;
	private final String description;
	private final List<String> choices;
	private final BiConsumer<Message, MessageReactionAddEvent> action;
	private final Consumer<Message> cancel;
	private final boolean useLetters;
	private final boolean useCancel;

	TeamSelectionMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit, Color color, String text, String description, List<String> choices, BiConsumer<Message, MessageReactionAddEvent> action,
			Consumer<Message> cancel, boolean useLetters, boolean useCancel)
	{
		super(waiter, users, roles, timeout, unit);
		this.color = color;
		this.text = text;
		this.description = description;
		this.choices = choices;
		this.action = action;
		this.cancel = cancel;
		this.useLetters = useLetters;
		this.useCancel = useCancel;
	}
	// Gets the number emoji by the name.
	// This is kinda the opposite of the getEmoji method
	// except it's implementation is to provide the number
	// to the selection consumer when a choice is made.
	public static int getNumber(String emoji)
	{
		String[] array = NUMBERS;
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(emoji))
				return i + 1;
		return -1;
	}
	/**
	 * Shows the TeamSelectionMenu as a new {@link net.dv8tion.jda.api.entities.Message Message}
	 * in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
	 *
	 * @param channel The MessageChannel to send the new Message to
	 * @throws java.lang.IllegalArgumentException If <b>all</b> of the following are violated simultaneously:
	 *                                            <ul>
	 *                                                <li>Being sent to a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
	 *                                                <li>This TeamSelectionMenu does not allow typed input.</li>
	 *                                                <li>The bot doesn't have {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION
	 *                                                Permission.MESSAGE_ADD_REACTION} in the channel this menu is being sent to.</li>
	 *                                            </ul>
	 */
	@Override public void display(MessageChannel channel)
	{
		// This check is basically for whether or not the menu can even display.
		// Is from text channel
		// Does not allow typed input
		// Does not have permission to add reactions
		if (channel.getType() == ChannelType.TEXT && !((TextChannel) channel).getGuild().getSelfMember().hasPermission((TextChannel) channel, Permission.MESSAGE_ADD_REACTION))
			throw new PermissionException("Must be able to add reactions if not allowing typed input!");
		initialize(channel.sendMessage(getMessage()));
	}
	/**
	 * Displays this TeamSelectionMenu by editing the provided
	 * {@link net.dv8tion.jda.api.entities.Message Message}.
	 *
	 * @param message The Message to display the Menu in
	 * @throws java.lang.IllegalArgumentException If <b>all</b> of the following are violated simultaneously:
	 *                                            <ul>
	 *                                                <li>Being sent to a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
	 *                                                <li>This TeamSelectionMenu does not allow typed input.</li>
	 *                                                <li>The bot doesn't have {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION
	 *                                                Permission.MESSAGE_ADD_REACTION} in the channel this menu is being sent to.</li>
	 *                                            </ul>
	 */
	@Override public void display(Message message)
	{
		// This check is basically for whether or not the menu can even display.
		// Is from text channel
		// Does not allow typed input
		// Does not have permission to add reactions
		if (message.getChannelType() == ChannelType.TEXT && !message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_ADD_REACTION))
			throw new PermissionException("Must be able to add reactions if not allowing typed input!");
		initialize(message.editMessage(getMessage()));
	}
	// Initializes the TeamSelectionMenu using a Message RestAction
	// This is either through editing a previously existing Message
	// OR through sending a new one to a TextChannel.
	private void initialize(RestAction<Message> ra)
	{
		ra.queue(m -> {
			try
			{
				// From 0 until the number of choices.
				// The last run of this loop will be used to queue
				// the last reaction and possibly a cancel emoji
				// if useCancel was set true before this TeamSelectionMenu
				// was built.
				for (int i = 0; i < choices.size(); i++)
				{
					// If this is not the last run of this loop
					if (i < choices.size() - 1)
						m.addReaction(getEmoji(i)).queue();
						// If this is the last run of this loop
					else
					{
						RestAction<Void> re = m.addReaction(getEmoji(i));
						// If we're using the cancel function we want
						// to add a "step" so we queue the last emoji being
						// added and then make the RestAction to start waiting
						// on the cancel reaction being added.
						if (useCancel)
						{
							re.queue(); // queue the last emoji
							re = m.addReaction(CANCEL);
						}
						// queue the last emoji or the cancel button
						re.queue(v -> {
							// Depending on whether we are allowing text input,
							// we call a different method.
							waitReactionOnly(m);
						});
					}
				}
			}
			catch (PermissionException ex)
			{
				// If there is a permission exception mid process, we'll still
				// attempt to make due with what we have.
				waitReactionOnly(m);
			}
		});
	}
	// Waits only for reaction input
	private void waitReactionOnly(Message m)
	{
		Logger logger = LoggerFactory.getLogger(TeamSelectionMenu.class);

		// This one is only for reactions
		waiter.waitForEvent(MessageReactionAddEvent.class, e -> isValidReaction(m, e), e -> {
			if (e.getReaction().getReactionEmote().getName().equals(CANCEL))
				cancel.accept(m);
			else
			{
				// The int provided in the success consumer is not indexed from 0 to number of choices - 1,
				// but from 1 to number of choices. So the first choice will correspond to 1, the second
				// choice to 2, etc.
				action.accept(m, e);
				e.getReaction().removeReaction(Objects.requireNonNull(e.getUser())).queue(msg -> {
					logger.info("ID: {} Successfully removed reaction from {}", m.getIdLong(), e.getUser().getAsTag());
					waitReactionOnly(m);
				}, throwable -> logger.info("ID: {} Failed to remove reaction from {}", m.getIdLong(), e.getUser().getAsTag()));
			}
		}, timeout, unit, () -> {
			cancel.accept(m);
			m.delete().queue(msg -> logger.info("ID: {} Successfully deleted timed out menu", m.getIdLong()), throwable -> logger.warn("ID: {} Failed to delete timed out menu", m.getIdLong()));
		});
	}
	// This is where the displayed message for the TeamSelectionMenu is built.
	private Message getMessage()
	{
		MessageBuilder mbuilder = new MessageBuilder();
		if (text != null)
			mbuilder.append(text);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < choices.size(); i++)
			sb.append("\n").append(getEmoji(i)).append(" ").append(choices.get(i));
		mbuilder.setEmbed(new EmbedBuilder().setTitle("Team Selection Menu").setColor(color).setDescription(description == null ? "" : description).addField("Choices", sb.toString(), false).setTimestamp(OffsetDateTime.now())
				.setThumbnail("https://raw.githubusercontent.com/jyao6429/quizbowl-bot/master/images/qbat%20icon%20square.png").build());
		return mbuilder.build();
	}
	private boolean isValidReaction(Message m, MessageReactionAddEvent e)
	{
		// The message is not the same message as the menu
		if (!e.getMessageId().equals(m.getId()))
			return false;
		// The user is not valid
		if (!isValidUser(e.getUser(), e.isFromGuild() ? e.getGuild() : null))
			return false;
		// The reaction is the cancel reaction
		if (e.getReaction().getReactionEmote().getName().equals(CANCEL))
			return true;

		int num = getNumber(e.getReaction().getReactionEmote().getName());
		return !(num < 0 || num > choices.size());
	}
	private String getEmoji(int number)
	{
		return useLetters ? LETTERS[number] : NUMBERS[number];
	}
	@SuppressWarnings("UnusedReturnValue") public static class Builder extends Menu.Builder<Builder, TeamSelectionMenu>
	{
		private final List<String> choices = new LinkedList<>();
		private Color color;
		private String text;
		private String description;
		private BiConsumer<Message, MessageReactionAddEvent> selection;
		private Consumer<Message> cancel = (m) -> {
		};
		private boolean useLetters = false;
		private boolean addCancel = false;

		@Override public TeamSelectionMenu build()
		{
			Checks.check(waiter != null, "Must set an EventWaiter");
			Checks.check(!choices.isEmpty(), "Must have at least one choice");
			Checks.check(choices.size() <= 10, "Must have no more than ten choices");
			Checks.check(selection != null, "Must provide an selection consumer");
			Checks.check(text != null || description != null, "Either text or description must be set");
			return new TeamSelectionMenu(waiter, users, roles, timeout, unit, color, text, description, choices, selection, cancel, useLetters, addCancel);
		}

		/**
		 * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
		 *
		 * @param color The Color of the MessageEmbed
		 * @return This builder
		 */
		public Builder setColor(Color color)
		{
			this.color = color;
			return this;
		}

		public Builder useLetters()
		{
			this.useLetters = true;
			return this;
		}

		public Builder useNumbers()
		{
			this.useLetters = false;
			return this;
		}

		/**
		 * If {@code true}, {@link net.dv8tion.jda.api.entities.User User}s can type the number or
		 * letter of the input to make their selection, in addition to the reaction option.
		 *
		 * @param allow {@code true} if raw text input is allowed, {@code false} if it is not
		 * @return This builder
		 */
		public Builder allowTextInput(boolean allow)
		{
			return this;
		}

		/**
		 * If {@code true}, adds a cancel button that performs the timeout action when selected.
		 *
		 * @param use {@code true} if the cancel button should be shown, {@code false} if it should not
		 * @return This builder
		 */
		public Builder useCancelButton(boolean use)
		{
			this.addCancel = use;
			return this;
		}
		public Builder setText(String text)
		{
			this.text = text;
			return this;
		}

		/**
		 * Sets the description to be placed in an {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
		 * <br>If this is {@code null}, no MessageEmbed will be displayed
		 *
		 * @param description The content of the MessageEmbed's description
		 * @return This builder
		 */
		public Builder setDescription(String description)
		{
			this.description = description;
			return this;
		}

		/**
		 * Sets the {@link java.util.function.BiConsumer BiConsumer} action to perform upon selecting a option.
		 *
		 * @param selection The BiConsumer action to perform upon selecting a button
		 * @return This builder
		 */
		public Builder setSelection(BiConsumer<Message, MessageReactionAddEvent> selection)
		{
			this.selection = selection;
			return this;
		}

		public Builder setCancel(Consumer<Message> cancel)
		{
			this.cancel = cancel;
			return this;
		}

		/**
		 * Adds a single String choice.
		 *
		 * @param choice The String choice to add
		 * @return This builder
		 */
		public Builder addChoice(String choice)
		{
			Checks.check(choices.size() < 10, "Cannot set more than 10 choices");

			this.choices.add(choice);
			return this;
		}

		/**
		 * Adds the String choices.
		 * <br>These correspond to the button in order of addition.
		 *
		 * @param choices The String choices to add
		 * @return This builder
		 */
		public Builder addChoices(String... choices)
		{
			for (String choice : choices)
				addChoice(choice);
			return this;
		}

		/**
		 * Sets the String choices.
		 * <br>These correspond to the button in the order they are set.
		 *
		 * @param choices The String choices to set
		 * @return This builder
		 */
		public Builder setChoices(String... choices)
		{
			clearChoices();
			return addChoices(choices);
		}

		/**
		 * Clears all previously set choices.
		 *
		 * @return This builder
		 */
		public Builder clearChoices()
		{
			this.choices.clear();
			return this;
		}
	}
}
