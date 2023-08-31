package me.Cutiemango.MangoQuest.questobject;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class CustomQuestObject extends SimpleQuestObject
{
	public CustomQuestObject(){}

	@Override
	public final String getConfigString()
	{
		return "CUSTOM_OBJECT";
	}

	@Override
	public String getObjectName()
	{
		return I18n.locMsg(null,"QuestObjectName.CustomObject");
	}

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished)
	{
		return new TextComponent(I18n.locMsg(p,"QuestObject.CustomObject"));
	}

	@Override
	public String toDisplayText(Player p)
	{
		return I18n.locMsg(p,"QuestObject.CustomObject");
	}

	public abstract String getProgressText(QuestObjectProgress qop);
	
	public void save(QuestIO config, String objpath)
	{
		config.set(objpath + "ObjectClass", this.getClass().getName());
	}
}
