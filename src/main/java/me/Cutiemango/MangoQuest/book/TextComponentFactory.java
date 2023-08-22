package me.Cutiemango.MangoQuest.book;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.RequirementManager;
import me.Cutiemango.MangoQuest.manager.TimeHandler;
import me.Cutiemango.MangoQuest.model.Quest;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class TextComponentFactory
{
	// Note:
	// The command argument here needs "/".
	public static TextComponent regClickCmdEvent(TextComponent t, String command) {
		if (!command.startsWith("/"))
			command = command + "/";
		BaseComponent text = t.duplicate();
		text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		return (TextComponent) text;
	}

	public static TextComponent regHoverEvent(TextComponent t, String s) {
		t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent(QuestChatManager.translateColor(s)) }));
		return t;
	}

	public static TextComponent formatSanitize(TextComponent t) {
		DebugHandler.log(5, "original");
		DebugHandler.log(5, ComponentSerializer.toString(t));
		if (t.getText() == null || t.getText().length() == 0)
			return t;
		else if (t.getText().contains("§") || t.getText().contains("&")||QuestChatManager.hex.matcher(t.getText()).find()) {
			TextComponent result = new TextComponent();
			BaseComponent[] comps = TextComponent.fromLegacyText(QuestChatManager.translateColor(t.getText()));
			for (BaseComponent comp : comps) {
				DebugHandler.log(5, ComponentSerializer.toString(comp));
				// cancel out parent's formatting
				if (comp.isBoldRaw() == null)
					comp.setBold(false);
				if (comp.isItalicRaw() == null)
					comp.setItalic(false);
				if (comp.isUnderlinedRaw() == null)
					comp.setUnderlined(false);
				if (comp.isStrikethroughRaw() == null)
					comp.setStrikethrough(false);
			}
		      result.copyFormatting(comps[0], ComponentBuilder.FormatRetention.FORMATTING, false);
		      result.copyFormatting(t, ComponentBuilder.FormatRetention.EVENTS, true);
			result.setText(comps[0].toPlainText());
			for (int i = 1; i < comps.length; i++)
				
				result.addExtra(comps[i]);
			
			DebugHandler.log(5, "result:");
			DebugHandler.log(5, ComponentSerializer.toString(result));
			return result;
		}
		return t;
	}

	public static TextComponent convertItemHoverEvent(ItemStack it, boolean isFinished) {
		DebugHandler.log(5, "converted Items (call) ");
		return Main.getInstance().handler.textFactoryConvertItem(it, isFinished);
	}

	public static TextComponent convertLocHoverEvent(Player p,String name, Location loc, boolean isFinished) {
		return Main.getInstance().handler.textFactoryConvertLocation(p,name, loc, isFinished);
	}

	public static TextComponent convertViewQuest(Player p,Quest q) {
		if (q == null)
			return new TextComponent(I18n.locMsg(p,"QuestEditor.NotSet"));
		TextComponent t = new TextComponent(I18n.locMsg(p,"QuestGUI.ColorFormat.NormalQuest") + q.getQuestName());
		t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				TextComponent.fromLegacyText(I18n.locMsg(p,"QuestJourney.ClickToView", q.getQuestName()))));
		
		t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mq quest view " + q.getInternalID()));
		return t;
	}

	public static TextComponent convertRequirement(QuestPlayerData qd, Quest q) {
		TextComponent text = new TextComponent(QuestChatManager.unavailableQuestFormat(q.getQuestName()));
		if (q.usePermission() && !qd.getPlayer().hasPermission("MangoQuest.takeQuest." + q.getInternalID()))
			return regHoverEvent(text, I18n.locMsg(qd.getPlayer(),"Requirements.NotMeet.Permission"));

		if (qd.hasFinished(q)) {
			long lastFinishTime = qd.getFinishData(q).getLastFinish();
			long delay = -1L;
			switch (q.getRedoSetting()) {
				case ONCE_ONLY:
					return regHoverEvent(text, I18n.locMsg(qd.getPlayer(),"CommandInfo.NotRedoable"));
				case COOLDOWN:
					delay = qd.getDelay(lastFinishTime, q.getRedoDelay());
					break;
				case DAILY:
					delay = TimeHandler.getDailyCooldown(lastFinishTime, q.getResetHour());
					break;
				case WEEKLY:
					delay = TimeHandler.getWeeklyCooldown(lastFinishTime, q.getResetDay(), q.getResetHour());
					break;
			}
			if (delay > 0)
				return regHoverEvent(text, I18n.locMsg(qd.getPlayer(),"QuestJourney.WaitFor", TimeHandler.convertTime(delay)));

			if (!qd.hasTakenReward(q))
				return regHoverEvent(text, I18n.locMsg(qd.getPlayer(),"QuestReward.RewardNotTaken"));
		}
		if (q.hasRequirement()) {
			Optional<String> msg = RequirementManager.meetRequirementWith(qd.getPlayer(), q.getRequirements(), true);
			return msg.map(s -> regHoverEvent(text, s)).orElseGet(() -> convertViewQuest(qd.getPlayer(),q));
		}
		return text;
	}

}
