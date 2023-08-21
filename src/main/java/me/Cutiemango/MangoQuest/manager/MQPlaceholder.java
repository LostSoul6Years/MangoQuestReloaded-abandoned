package me.Cutiemango.MangoQuest.manager;

import java.text.DecimalFormat;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.objects.QuestStage;
import me.Cutiemango.MangoQuest.questobject.DecimalObject;
import me.Cutiemango.MangoQuest.questobject.NumerableObject;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class MQPlaceholder extends PlaceholderExpansion{

	@Override
	public @NotNull String getAuthor() {
		return "JJCDeveloper";
	}

	@Override
	public @NotNull String getIdentifier	() {
		return "mq";
	}

	@Override
	public @NotNull String getVersion() {
		return "Reloaded";
	}
	@Override
	public @NotNull String getRequiredPlugin() {
		return "MangoQuestReloaded";
	}
	 @Override
	    public String onRequest(OfflinePlayer player, String args) {
		 	if(args.toLowerCase().startsWith("player_")) {
		 		//argsnames[0] must be "player"
		 		String[] argnames = args.split("_");
		 		switch(argnames[1]) {
		 		case "completedquests":{
		 			if(argnames[2].equalsIgnoreCase("amount")) {
		 				if(QuestStorage.playerData.get(player.getName())!=null) {
		 					return QuestStorage.playerData.get(player.getName()).getFinishQuests().size()+"";
		 				}else {
		 					return "0";
		 				}
		 			   
		 			}else if(argnames[2].equalsIgnoreCase("names")) {
		 				if(QuestStorage.playerData.get(player.getName())!=null) {
		 				StringBuilder finalString = new StringBuilder();
		 				
		 				for(QuestFinishData q:QuestStorage.playerData.get(player.getName()).getFinishQuests()) {
		 					finalString.append(QuestChatManager.translateColor(q.getQuest().getQuestName()));
		 					finalString.append("\n");
		 				}
		 				
		 				return finalString.toString();
		 				}else {
		 					return "";
		 				}
		 			}else {
		 				return "Invalid Arguments";
		 			}
		 			
		 		}
		 		case "currentquests":{
		 			if(argnames[2].equalsIgnoreCase("amount")) {
		 				if(QuestStorage.playerData.get(player.getName())!=null) {
		 					return QuestStorage.playerData.get(player.getName()).getProgresses().size()+"";
		 				}else {
		 					return "0";
		 				}
		 			}else if(argnames[2].equalsIgnoreCase("names")) {
		 				if(QuestStorage.playerData.get(player.getName())!=null) {
			 				StringBuilder finalString = new StringBuilder();
			 				
			 				for(QuestProgress q:QuestStorage.playerData.get(player.getName()).getProgresses()) {
			 					finalString.append(QuestChatManager.translateColor(q.getQuest().getQuestName()));
			 					finalString.append("\n");
			 				}
			 				
			 				return finalString.toString();
		 				}else {
		 					return "";
		 				}
		 			}else if(argnames[2].equalsIgnoreCase("totalprogress")){
		 				if(QuestStorage.playerData.get(player.getName())!=null) {
		 					int questPoints = 0;
		 					int gainedPoints = 0;
		 					for(QuestProgress qp:QuestStorage.playerData.get(player.getName()).getProgresses()) {
		 						int currentStage = qp.getCurrentStage();
		 						//previous stages
		 						for(int i = 0;i < currentStage;i++) {
		 							for(SimpleQuestObject sqo:qp.getQuest().getStage(i).getObjects()) {
		 							   if(sqo instanceof NumerableObject) {
		 								   gainedPoints += ((NumerableObject) sqo).getAmount();
		 								   questPoints += ((NumerableObject) sqo).getAmount();
		 							   }else {
		 								   questPoints+=1;
		 								   gainedPoints+=1;
		 							   }
		 							}
		 						}
		 						//current stage
		 							for(SimpleQuestObject sqo:qp.getQuest().getStage(currentStage).getObjects()) {
			 							   if(sqo instanceof NumerableObject) {
			 								   questPoints += ((NumerableObject) sqo).getAmount();
			 							   }else {
			 								   questPoints+=1;
			 							   }
			 							   for(QuestObjectProgress qop:qp.getCurrentObjects()) {
			 								   if(qop.getObject().equals(sqo)) {
			 									   if(qop.getObject() instanceof NumerableObject) {
			 											   gainedPoints+= ((NumerableObject)qop.getObject()).getAmount();
			 									   }else {
			 										   if(qop.isFinished()) {
			 											   gainedPoints+=1;
			 										   }
			 									   }
			 								   }
			 							   }
		 							}
		 						//future stages
		 						for(int i = currentStage+1;i < qp.getQuest().getStages().size()-1;i++) {
		 							QuestStage qs = qp.getQuest().getStage(i);
		 							for(SimpleQuestObject sqo:qs.getObjects()) {
			 							   if(sqo instanceof NumerableObject) {
			 								   questPoints += ((NumerableObject) sqo).getAmount();
			 							   }else {
			 								   questPoints +=1;
			 							   }
		 							}
		 						}
		 					}
		 					//if no quest
		 					if(questPoints == 0) {
		 						return "N/A%";
		 					}
		 					double percentage = gainedPoints/questPoints;
		 					DecimalFormat df = new DecimalFormat("#.##");
		 					return df.format(percentage)+"%";
		 				}
		 				
		 				
		 			}else {
	 					//player_currentquest_index_function_optarg
	 					int index;
	 					try {
	 						index = Integer.parseInt(argnames[2])-1;
	 						
	 					}catch(NumberFormatException e) {
	 						return "Invalid Arguments";
	 					}
	 					QuestProgress progress = ((QuestProgress)QuestStorage.playerData.get(player.getName()).getProgresses().toArray()[index]);
	 					if(progress == null) {
	 						return "";
	 					}
	 					if(argnames[3].equalsIgnoreCase("name")){
	 						
	 						if(progress.isIncompatibleMode()) {
	 							return progress.getIncompatQuest();
	 						}else {
	 							return progress.getQuest().getQuestName()+org.bukkit.ChatColor.RESET;
	 						}
	 						
	 					}else if(argnames[3].equalsIgnoreCase("stages")){
	 						if(progress.isIncompatibleMode()) {
	 							return "0";
	 						}else {
	 							return progress.getQuest().getStages().size()+"";
	 						}
	 					}else if(argnames[3].equalsIgnoreCase("stage")){
	 						return progress.getCurrentStage()+"";
	 					}else if(argnames[3].equalsIgnoreCase("objectives")){
	 						if(progress.isIncompatibleMode()) {
	 							return "";
	 						}else {
	 							int max = -1;
	 							String splitter = "\n";
	 							if(argnames.length >=5) {
	 								splitter = QuestChatManager.translateColor(argnames[4]);
	 							}
	 							if(argnames.length >=6) {
	 								try {
	 									max = Integer.parseInt(argnames[5]);
	 								}catch(Exception e) {
	 									
	 								}
	 							}
	 							StringBuilder objstr = new StringBuilder();
	 							StringBuilder objstr1 = objstr;
	 							final String splitterfinal = splitter;//bruh i hate java
	 							progress.getCurrentObjects().forEach((obj)->{
	 								if(obj.isFinished()) {
	 									objstr1.append(obj.getObject().toDisplayText(player.getPlayer())+I18n.locMsg(player, "CommandInfo.Finished")+splitterfinal);
	 								}else {
	 									if(obj.getObject() instanceof NumerableObject) {
	 										int amount = ((NumerableObject)obj.getObject()).getAmount();
	 										objstr1.append(obj.getObject().toDisplayText(player.getPlayer())+" "+I18n.locMsg(player, "CommandInfo.Progress",obj.getProgress()+"",amount+"")+splitterfinal);
	 									}else if(obj.getObject() instanceof DecimalObject) {
	 										double amount = ((DecimalObject)obj.getObject()).getAmount();
	 										objstr1.append(obj.getObject().toDisplayText(player.getPlayer())+" "+I18n.locMsg(player, "CommandInfo.Progress",obj.getProgress()+"",amount+"")+splitterfinal);
	 									}else {
	 										objstr1.append(obj.getObject().toDisplayText(player.getPlayer())+" "+I18n.locMsg(player, "CommandInfo.Progress","0","1")+splitterfinal);
	 										
	 									}
	 								}
	 								
	 							});
	 							
	 							for(int i = 0; i <= progress.getCurrentObjects().size()-max;i++) {
	 								if(objstr.lastIndexOf(splitterfinal) == -1) {
	 									break;
	 								}
	 								objstr = new StringBuilder(objstr.substring(0,objstr.lastIndexOf(splitterfinal)));
	 							}
	 							
	 							
	 							
	 							return objstr.toString();
	 						}
	 					}else {
	 						return "Invalid Arguments";
	 					}
	 				} 
		 		}
		 		}
		 	}else {
		 		return "Unknown Placeholder";
		 	}
	       return "Unknown Placeholder";
	    }
}
