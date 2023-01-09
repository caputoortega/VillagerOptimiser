package ar.com.caputo.villageroptimiser.tasks;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Villager;

public class NormalActivityTask 
                extends ActivityOptimiser
                implements Runnable {
    
    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {

            Collection<Villager> villagers = world.getEntitiesByClass(Villager.class);
            
            villagers.forEach(villager -> {

                if(!activityUtils.wouldBeBadActivity(villager) && !activityUtils.isScheduleNormal(villager)) {
                    activityUtils.setScheduleNormal(villager);
                    activityUtils.setActivitiesNormal(villager);
                }
                activityUtils.clearPlaceholderMemories(villager);

            });

        }
    }
}
