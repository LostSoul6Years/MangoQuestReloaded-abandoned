package me.Cutiemango.MangoQuest.data;

public class IncompatibleQuestObjectProgress extends QuestObjectProgress{

	private int objCount = 0;
	
	public int getObjCount() {
		return objCount;
	}

	public void setObjCount(int objCount) {
		this.objCount = objCount;
	}
	
	private int progress;
	private double progressD = -99999;
	
	public IncompatibleQuestObjectProgress(int objCount) {
		super();
		this.objCount = objCount;
		
	}
	public IncompatibleQuestObjectProgress(int objCount,int progress) {
		super();
		this.objCount = objCount;
		super.setProgress(progress);
	}
	public IncompatibleQuestObjectProgress(int objCount,double progress) {
		super();
		this.objCount = objCount;
		super.setProgressD(progress);
	}
	
	public double getProgressD() {
		return super.getProgressD();
	}

	private long lastInvokedMilli = -1; //default is -1 meaning that it has never been invoked yet

	public long getLastInvokedMilli() {
		return super.getLastInvokedMilli();
	}



	public int getProgress() {
		return super.getProgress();
	}

	

}
