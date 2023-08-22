package me.Cutiemango.MangoQuest.data;

import org.bukkit.entity.Player;

import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.conversation.ConversationManager;
import me.Cutiemango.MangoQuest.conversation.ConversationProgress;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTalkToNPC;

public class QuestObjectProgress
{
	private boolean isFinished = false;
	private final SimpleQuestObject obj;
	private ConversationProgress cp;
	private int progress;
	private double progressD = -99999;
	public double getProgressD() {
		return progressD;
	}

	public void setProgressD(double progressD) {
		if(progressD < 0) {
			progressD = 0;
		}
		this.progressD = progressD;
	}

	private long lastInvokedMilli = -1; //default is -1 meaning that it has never been invoked yet

	public long getLastInvokedMilli() {
		return lastInvokedMilli;
	}

	public void setLastInvokedMilli(long lastInvokedMilli) {
		this.lastInvokedMilli = lastInvokedMilli;
	}

	public QuestObjectProgress(SimpleQuestObject object, int amount) {
		obj = object;
		progress = amount;
	}
	public QuestObjectProgress(SimpleQuestObject object, double amount) {
		obj = object;
		progressD = amount;
	}

	public QuestObjectProgress() {
		this.obj = null;
		// TODO Auto-generated constructor stub
	}

	public void checkIfFinished() {
		if (obj instanceof QuestObjectTalkToNPC) {
			if (progress == 1 || (cp != null && QuestUtil.getData(cp.getOwner()).hasFinished(cp.getConversation())))
				isFinished = true;
			return;
		}
		if (obj instanceof NumerableObject) {
			if (((NumerableObject) obj).getAmount() <= progress)
				isFinished = true;
			return;
		}
		if(obj instanceof DecimalObject) {
			if(((DecimalObject)obj).getAmount() <= progressD)
				isFinished=true;
			return;
		}
		if (obj != null && progress == 1) {
			isFinished = true;
		}
	}

	public void newConversation(Player p) {
		ConversationManager.startConversation(p, obj.getConversation());
		cp = ConversationManager.getConvProgress(p);
	}

	public void openConversation(Player p) {
		if (obj.hasConversation()) {	
			if (cp == null)
				newConversation(p);
			else if (cp.isFinished()) {
				finish();	
				QuestUtil.getData(p).talkToNPC(cp.getConversation().getNPC());
			}
			else
				ConversationManager.openConversation(p, cp);
		}
	}

	public SimpleQuestObject getObject() {
		return obj;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void finish() {
		isFinished = true;
	}

	public void setProgress(int p) {
		progress = p;
	}

}
