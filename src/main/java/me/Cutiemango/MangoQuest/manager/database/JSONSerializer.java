package me.Cutiemango.MangoQuest.manager.database;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.Cutiemango.MangoQuest.data.IncompatibleQuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;

public class JSONSerializer {

	public static String jsonSerializeIncompatible(List<IncompatibleQuestObjectProgress> lst) {
		JSONObject json = new JSONObject();
		for (int i = 0; i < lst.size(); i++) {
			if(lst.get(i).getObject() instanceof DecimalObject){			
				json.put(Integer.toString(i), Double.toString(lst.get(i).getProgressD()));				
			}else{
				json.put(Integer.toString(i), Integer.toString(lst.get(i).getProgress()));
			}
			json.put(i+"lastinvokedmilli",lst.get(i).getLastInvokedMilli());
		}

		return json.toJSONString();
	}
	public static String jsonSerialize(List<QuestObjectProgress> lst) {
		JSONObject json = new JSONObject();
		for (int i = 0; i < lst.size(); i++) {
			if(lst.get(i).getObject() instanceof DecimalObject){			
				json.put(Integer.toString(i), Double.toString(lst.get(i).getProgressD()));				
			}else{
				json.put(Integer.toString(i), Integer.toString(lst.get(i).getProgress()));
			}
			json.put(i+"lastinvokedmilli",lst.get(i).getLastInvokedMilli());
		}

		return json.toJSONString();
	}

	
	public static List<QuestObjectProgress> jsonDeserialize(List<SimpleQuestObject> objs, String obj) {
		List<QuestObjectProgress> prog = new ArrayList<>();
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(obj);
			for (int i = 0; i < json.keySet().size()/2; i++) {
				QuestObjectProgress progress = new QuestObjectProgress(objs.get(i),0);
				try {
					progress.setProgress(Integer.parseInt(json.get(Integer.toString(i)).toString()));
				}catch(NumberFormatException e) {
					progress.setProgressD(Double.parseDouble(json.get(Integer.toString(i)).toString()));
				}
				
				if(json.containsKey(i+"lastinvokedmilli")) {
					progress.setLastInvokedMilli(Long.parseLong(json.get(i+"lastinvokedmilli").toString()));
				}
				progress.checkIfFinished();
				prog.add(progress);				
			}
		} catch (ParseException e) {
			QuestChatManager.logCmd(Level.WARNING, "An error occured whilest decoding json.");
		}
		return prog;
	}
	public static List<IncompatibleQuestObjectProgress> jsonDeserializeIncompatible(String obj){
		List<IncompatibleQuestObjectProgress> incompatProg = new ArrayList<>();
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(obj);
			for (int i = 0; i < json.keySet().size()/2; i++) {
				IncompatibleQuestObjectProgress progress = new IncompatibleQuestObjectProgress(i);
				try {
					progress.setProgress(Integer.parseInt(json.get(Integer.toString(i)).toString()));
				}catch(NumberFormatException e) {
					progress.setProgressD(Double.parseDouble(json.get(Integer.toString(i)).toString()));
				}
				if(json.containsKey(i+"lastinvokedmilli")) {
					progress.setLastInvokedMilli(Long.parseLong(json.get(i+"lastinvokedmilli").toString()));
				}				
				//progress.checkIfFinished();
				incompatProg.add(progress);				
			}
		} catch (ParseException e) {
			QuestChatManager.logCmd(Level.WARNING, "An error occured whilest decoding sql obj progress for incompatible quest format.");
		}
		return incompatProg;
	}
}
