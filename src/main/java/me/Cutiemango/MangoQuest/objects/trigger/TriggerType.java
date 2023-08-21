package me.Cutiemango.MangoQuest.objects.trigger;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;

public enum TriggerType
{
	//unfinished danger (used in command so no trans)
	TRIGGER_ON_TAKE("TriggerType.OnTake", false),
	TRIGGER_ON_QUIT("TriggerType.OnQuit", false),
	TRIGGER_ON_FINISH("TriggerType.OnFinish", false),
	TRIGGER_STAGE_START("TriggerType.StageStart", true),
	TRIGGER_STAGE_FINISH("TriggerType.StageFinish", true);

	private String name;
	private boolean hasStage;

	TriggerType(String s, boolean b) {
		name = s;
		hasStage = b;
	}

	public String toCustomString(Player p) {
		return I18n.locMsg(p, name);
	}

	public boolean hasStage() {
		return hasStage;
	}

	public String toCustomString(Player p,int i) {
		return I18n.locMsg(p, name.replace("N", Integer.toString(i)));
	}
}