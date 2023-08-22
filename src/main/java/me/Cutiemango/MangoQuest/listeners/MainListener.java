package me.Cutiemango.MangoQuest.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.editor.ConversationEditorManager;
import me.Cutiemango.MangoQuest.editor.EditorListenerHandler;
import me.Cutiemango.MangoQuest.editor.QuestEditorManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;

public class MainListener implements Listener
{
    public MainListener() {
    	DebugHandler.log(5, "Initiated Listener Registration!");
    }
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		PlayerListener.onPlayerJoin(e.getPlayer());
	}
	
	

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		PlayerListener.onPlayerQuit(e.getPlayer());
	}
	
	//@EventHandler(priority=EventPriority.LOWEST)
	//public void onAnvil(PrepareAnvilEvent e) {
		
    //}
	
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerLeaveBed(PlayerBedLeaveEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = (Player)e.getPlayer();
		QuestPlayerData pd = QuestUtil.getData(p);
		if(pd == null) {
			DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
			return;
		}
		pd.sleep();
	}
   
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTameMob(EntityTameEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if(!(e.getOwner() instanceof Player)) {
			return;
		}
		
		Player p = (Player) e.getOwner();
		QuestPlayerData pd = QuestUtil.getData(p);
		if(pd == null) {
			DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
			return;
		}
		pd.tameMob(e.getEntity());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerEnchant(EnchantItemEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getEnchanter();
		QuestPlayerData pd = QuestUtil.getData(p);
		if(pd == null) {
			DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
			return;
		}
		pd.enchantItem(e.getItem(),e.getEnchantsToAdd()); 
	}
	

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		PlayerListener.onEntityDeath(e.getEntity());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityRegen(EntityRegainHealthEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		
		if(e.getEntity() instanceof Player) {

			PlayerListener.onPlayerRegen((Player)e.getEntity(),e.getAmount());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerFillBucketEvent(PlayerBucketFillEvent e) {
		
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if(qd == null) {
			DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
			return;
		}
		
		//qd.fillBucket(e.getItemStack(),e.getBlock().getType());		
		qd.fillBucket(e.getItemStack(),e.getBlockClicked().getType());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onShearEntity(PlayerShearEntityEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		PlayerListener.onShearSheep(e.getPlayer(),e.getEntity());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(5, "[Listener] Player %s has no player data.", p.getName());
			return;
		}
		if (e.isCancelled()) {
			DebugHandler.log(5, "[Listener] Detected player %s broke a block, but the event is cancelled.", p.getName());
			return;
		}
		if (e.getBlock() != null && e.getBlock().getType() != null) {
			PlayerListener.onBreakBlock(p, e.getBlock().getType());
			EditorListenerHandler.onBlockBreak(p, e.getBlock(), e);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onConsumeItem(PlayerItemConsumeEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		if (e.getItem() != null && !e.isCancelled())
			PlayerListener.onConsumeItem(p, e.getItem());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + e.getPlayer().getName() + " has no player data.");
			return;
		}
		if (qd != null && e.getTo() != null) {
			PlayerListener.onMove(p, e.getFrom(),e.getTo());
			if(ConfigSettings.LEGACY_MC) {
				Material m = e.getPlayer().getLocation().getBlock().getType();
				if(m == Main.getInstance().mcCompat.getCompatMaterial("STATIONARY_WATER", "WATER")|| m == Material.WATER) {
					
				}
			}
		}
		
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInteractEntity(PlayerInteractEntityEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		
		if(Main.getInstance().mcCompat.getItemInMainHand(p)!=null) {
			qd.setLastClicked(Main.getInstance().mcCompat.getItemInMainHand(p).clone());
		}else if(Main.getInstance().mcCompat.getItemInOffHand(p)!=null) {
			qd.setLastClicked(Main.getInstance().mcCompat.getItemInMainHand(p).clone());
		}
		//qd.setLastClicked(e.getPlayer().getInventory().getItem(e.getHand()).clone());
		if (Main.getInstance().mcCompat.runStupidOffHandCheck(e))
			return;
		if (CitizensAPI.getNPCRegistry().isNPC(e.getRightClicked()))
			PlayerListener.onNPCRightClick(p, CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked()), e);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onNPCDamage(NPCDamageByEntityEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if (!(e.getDamager() instanceof Player))
			return;
		Player p = (Player) e.getDamager();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		if (QuestEditorManager.checkEditorMode(p, false) || ConversationEditorManager.checkEditorMode(p, false))
			e.setCancelled(true);
		return;
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		EditorListenerHandler.onNPCLeftClick(e.getClicker(), e.getNPC(), e);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClose(InventoryCloseEvent e) {
		
		if(Main.lockDown.get()) {
			return;
		}
		if (!(e.getPlayer() instanceof Player))
			return;
		
		DebugHandler.log(2,"isAsynchronous: "+e.isAsynchronous());
		EditorListenerHandler.onInventoryClose((Player) e.getPlayer(), e.getInventory(), e.getView());
		RewardGUIListener.onInventoryClose(e);
	}
	
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onCraft(CraftItemEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if(!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		if(e.getClickedInventory()==null) {
			return;
		}
		qd.craftItem((CraftingInventory)e.getClickedInventory());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if (!(e.getWhoClicked() instanceof Player))
			return;
		Player p = (Player) e.getWhoClicked();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		if (e.getClickedInventory() == null || e.getView().getTitle() == null)
			return;
		if(e.getClickedInventory() instanceof AnvilInventory) {
			//AnvilInventory inv = (AnvilInventory) e.getClickedInventory();
			qd.useAnvil(((AnvilInventory)e.getClickedInventory()),e.getCurrentItem());
			return;
		}
		RewardGUIListener.onInventoryClick(e);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Damageable))
			return;
		Player p = (Player) e.getDamager();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		EditorListenerHandler.onEntityDamage(p, e.getEntity(), e);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if(e.getEntity().getShooter() instanceof Player) {
			QuestPlayerData pd = QuestUtil.getData((Player) e.getEntity().getShooter());
			if (pd == null) {
				DebugHandler.log(4, "[Listener] Player " + ((Player)e.getEntity().getShooter()).getName() + " has no player data.");
				return;
			}
			if(!pd.isProjectileInAir(e.getEntity())) {
				return;
			}
			pd.launchProjectile(e.getEntity());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFish(PlayerFishEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if (e.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
			Item item = (Item) e.getCaught();
			PlayerListener.onFish(e.getPlayer(), item);
		}else if(e.getState().equals(Main.getInstance().mcCompat.getMidAirState())) {
			QuestPlayerData pd = QuestUtil.getData(e.getPlayer());
			if (pd == null) {
				DebugHandler.log(4, "[Listener] Player " + e.getPlayer().getName() + " has no player data.");
				return;
			}
			pd.launchProjectile(null);
			
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		DebugHandler.log(4, "[Listener] in chat function!");
		if(Main.lockDown.get()) {
			DebugHandler.log(4, "[Listener] Player is in lock down, all functions not work!");
			return;
		}
		Player p = e.getPlayer();
		if(!EditorListenerHandler.isListening(e.getPlayer())) {
			QuestPlayerData qd = QuestUtil.getData(p);
			if (qd == null) {
				DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
				return;
			}
			DebugHandler.log(5, "[Listener] Player %s: %s, %s", e.getPlayer().getName(),e.getFormat(),e.getMessage()	);
			qd.chat(e.getMessage());
			return;
		}
		DebugHandler.log(2, "[Listener] Player %s is now editing quest: ",e.getPlayer().getName()	);
		EditorListenerHandler.onChat(p, e.getMessage(), e);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPotion(PotionSplashEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if(e.getEntity().getShooter() instanceof Player) {
			Player p = (Player) e.getEntity().getShooter();
			QuestPlayerData qd = QuestUtil.getData(p);
			if (qd == null) {
				DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
				return;
			}			
			qd.launchProjectile(e.getEntity());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onProjectile(ProjectileLaunchEvent e) {	
		if(Main.lockDown.get()) {
			return;
		}
		if(e.getEntity().getShooter() instanceof Player) {
			if(e.getEntity() instanceof ThrownPotion || e.getEntity() instanceof ThrownExpBottle) {
				return;
			}
			Player p = (Player)e.getEntity().getShooter();
			QuestPlayerData pd = QuestUtil.getData(p);
			if (pd == null) {
				DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
				return;
			}
			pd.launchProjectile(e.getEntity());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		if(!EditorListenerHandler.isListening(e.getPlayer())) {
			QuestPlayerData qd = QuestUtil.getData(e.getPlayer());
			if (qd == null) {
				DebugHandler.log(4, "[Listener] Player " + e.getPlayer().getName() + " has no player data.");
				return;
			}
			qd.command(e.getMessage());
			return;
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(Main.lockDown.get()) {
			return;
		}
		Player p = e.getPlayer();
		QuestPlayerData qd = QuestUtil.getData(p);
		if (qd == null) {
			DebugHandler.log(4, "[Listener] Player " + p.getName() + " has no player data.");
			return;
		}
		if(e.getItem()!=null)
		qd.setLastClicked(e.getItem().clone());
		EditorListenerHandler.onPlayerInteract(p, e.getAction(), e.getItem(), e);
	}
	
	
	

}
