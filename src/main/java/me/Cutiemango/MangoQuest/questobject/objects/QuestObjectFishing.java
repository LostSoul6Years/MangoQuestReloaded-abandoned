package me.Cutiemango.MangoQuest.questobject.objects;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.editor.EditorListenerObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.interfaces.EditorObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestObjectFishing extends NumerableObject implements EditorObject
{
	public static final List<Material> FISHES = Arrays.asList(Main.getInstance().mcCompat.getCompatMaterial("RAW_FISH", "COD"),Main.getInstance().mcCompat.getCompatMaterial("RAW_FISH", "SALMON") ,Main.getInstance().mcCompat.getCompatMaterial("RAW_FISH", "PUFFERFISH"), Main.getInstance().mcCompat.getCompatMaterial("RAW_FISH", "TROPICAL_FISH"));

	public QuestObjectFishing() {
	}

	public QuestObjectFishing(int fish) {
		amount = fish;
	}

	@Override
public String toDisplayText(Player p) {
		return I18n.locMsg(p,"QuestObject.Fishing", Integer.toString(amount));
	}

	@Override
	public String getConfigString() {
		return "FISHING";
	}

	@Override
	public String getObjectName() {
		return I18n.locMsg(null,"QuestObjectName.Fishing");
	}

	@Override
	public TextComponent toTextComponent(Player p,boolean isFinished) {
		return super.toTextComponent(p,ChatColor.stripColor(I18n.locMsg(null,"QuestObject.Fishing")), isFinished, amount);
	}

	@Override
	public boolean load(QuestIO config, String path) {
		return super.load(config, path);
	}

	@Override
	public void save(QuestIO config, String path) {
		super.save(config, path);
	}

	@Override
	public boolean receiveCommandInput(Player sender, String type, String obj) {
		return super.receiveCommandInput(sender, type, obj);
	}

	@Override
	public EditorListenerObject createCommandOutput(Player sender, String command, String type) {
		return super.createCommandOutput(sender, command, type);
	}

}
