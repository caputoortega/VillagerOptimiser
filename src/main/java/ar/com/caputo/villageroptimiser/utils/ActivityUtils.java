package ar.com.caputo.villageroptimiser.utils;

import com.google.common.collect.Sets;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityUtils {
    private final String NAME = Bukkit.getServer().getClass().getPackage().getName();
    private final String VERSION = NAME.substring(NAME.lastIndexOf('.') + 1);
    private final Set<String> IGNORE_JOB_SITE_VERSIONS =
         Sets.newHashSet("v1_19_R1");

    private Method VILLAGER_GET_HANDLE_METHOD;
    private Method VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD;
    private Method BEHAVIOUR_CONTROLLER_GET_SCHEDULE_METHOD;
    private Method CURRENT_ACTIVITY_METHOD;
    private Method SET_SCHEDULE_METHOD;

    private Field ACTIVITIES_FIELD;

    private Object ACTIVITY_CORE;
    private Object ACTIVITY_IDLE;
    private Object ACTIVITY_WORK;
    private Object ACTIVITY_MEET;
    private Object ACTIVITY_REST;
    private Object SCHEDULE_EMPTY;
    private Object SCHEDULE_SIMPLE;
    private Object SCHEDULE_VILLAGER_DEFAULT;
    private Object SCHEDULE_VILLAGER_BABY;

    public ActivityUtils() {
        
        // NMS changes its resource hierarchy on 1.17+ versions
        // by adding packages into account.
        boolean newApi = Integer.parseInt(VERSION.split("_")[1]) >= 17;

        try {

            if(newApi) {

                Class<?> behaviorControllerClass = Class.forName("net.minecraft.world.entity.ai.BehaviorController");
                Class<?> activityClass = Class.forName("net.minecraft.world.entity.schedule.Activity");
                Class<?> scheduleClass = Class.forName("net.minecraft.world.entity.schedule.Schedule");
                Class<?> entityLivingClass = Class.forName("net.minecraft.world.entity.EntityLiving");
                Class<?> craftVillager = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftVillager");
    
                this.VILLAGER_GET_HANDLE_METHOD = craftVillager.getMethod("getHandle");

                 // EntityLiving::getBehaviorController()
                this.VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD = entityLivingClass.getMethod("dy");

                // BehaviorController::getSchedule()
                this.BEHAVIOUR_CONTROLLER_GET_SCHEDULE_METHOD = behaviorControllerClass.getMethod("b"); 
                this.CURRENT_ACTIVITY_METHOD = scheduleClass.getMethod("a", int.class);

                // BehaviorController::setSchedule()
                this.SET_SCHEDULE_METHOD = behaviorControllerClass.getDeclaredMethod("a", scheduleClass); 
    
                Map<String, String> activitiesFieldNameMap = new HashMap<>();
                activitiesFieldNameMap.put("v1_19_R1", "k");
    
                ACTIVITIES_FIELD = behaviorControllerClass.getDeclaredField(activitiesFieldNameMap.get(VERSION));
                ACTIVITIES_FIELD.setAccessible(true);
    
                ACTIVITY_CORE = activityClass.getField("a").get(null); // CORE
                ACTIVITY_IDLE = activityClass.getField("b").get(null); // IDLE
                ACTIVITY_WORK = activityClass.getField("c").get(null); // WORK
                ACTIVITY_MEET = activityClass.getField("f").get(null); // MEET
                ACTIVITY_REST = activityClass.getField("e").get(null); // REST

                SCHEDULE_EMPTY = scheduleClass.getField("c").get(null); // EMPTY
                SCHEDULE_SIMPLE = scheduleClass.getField("d").get(null); // SIMPLE
                SCHEDULE_VILLAGER_BABY = scheduleClass.getField("e").get(null); // VILLAGER_BABY
                SCHEDULE_VILLAGER_DEFAULT = scheduleClass.getField("f").get(null); // VILLAGER_DEFAULT

            }

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setActivitiesNormal(Villager villager) {
        try {
            ((Set) ACTIVITIES_FIELD.get(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)))).clear();
            ((Set) ACTIVITIES_FIELD.get(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)))).add(ACTIVITY_CORE);
            Object currentSchedule = BEHAVIOUR_CONTROLLER_GET_SCHEDULE_METHOD.invoke(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)));
            Object currentActivity;
            if (currentSchedule == null) {
                currentActivity = ACTIVITY_IDLE;
            } else {
                currentActivity = CURRENT_ACTIVITY_METHOD.invoke(currentSchedule, (int) villager.getWorld().getTime());
            }
            ((Set) ACTIVITIES_FIELD.get(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)))).add(currentActivity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setActivitiesEmpty(Villager villager) {
        try {
            ((Set) ACTIVITIES_FIELD.get(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)))).clear();
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setScheduleNormal(Villager villager) {
        try {
            SET_SCHEDULE_METHOD.invoke(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)), villager.isAdult() ? (villager.getProfession() == Villager.Profession.NITWIT ? SCHEDULE_SIMPLE : SCHEDULE_VILLAGER_DEFAULT) : SCHEDULE_VILLAGER_BABY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setScheduleEmpty(Villager villager) {
        try {
            SET_SCHEDULE_METHOD.invoke(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)), SCHEDULE_EMPTY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean badCurrentActivity(Villager villager) {
        try {
            Object currentSchedule = BEHAVIOUR_CONTROLLER_GET_SCHEDULE_METHOD.invoke(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager)));
            if (currentSchedule == null) {
                return false;
            }
            Object currentActivity = CURRENT_ACTIVITY_METHOD.invoke(currentSchedule, (int) villager.getWorld().getTime());
            return badActivity(currentActivity, villager);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }

        return false;
    }

    public boolean wouldBeBadActivity(Villager villager) {
        Object wouldBeSchedule = villager.isAdult() ? (villager.getProfession() == Villager.Profession.NITWIT ? SCHEDULE_VILLAGER_DEFAULT : SCHEDULE_SIMPLE) : SCHEDULE_VILLAGER_BABY;

        try {
            Object currentActivity = CURRENT_ACTIVITY_METHOD.invoke(wouldBeSchedule, (int) villager.getWorld().getTime());
            return badActivity(currentActivity, villager);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }

        return false;
    }

    private boolean badActivity(Object activity, Villager villager) {

        if (activity == ACTIVITY_REST) {
            return villager.getMemory(MemoryKey.HOME) == null || isPlaceholderMemory(villager, MemoryKey.HOME);
        }
        if (activity == ACTIVITY_WORK) {
            return !IGNORE_JOB_SITE_VERSIONS.contains(VERSION) && (villager.getMemory(MemoryKey.JOB_SITE) == null || isPlaceholderMemory(villager, MemoryKey.JOB_SITE));
        }
        if (activity == ACTIVITY_MEET) {
            return villager.getMemory(MemoryKey.MEETING_POINT) == null || isPlaceholderMemory(villager, MemoryKey.MEETING_POINT);
        }

        return false;
    }

    public void replaceBadMemories(Villager villager) {

        if (villager.getMemory(MemoryKey.HOME) == null) {
            villager.setMemory(MemoryKey.HOME, new Location(villager.getWorld(), villager.getLocation().getBlockX(), -10000, villager.getLocation().getBlockZ()));
        }
        if (villager.getMemory(MemoryKey.JOB_SITE) == null && !IGNORE_JOB_SITE_VERSIONS.contains(VERSION)) {
            villager.setMemory(MemoryKey.JOB_SITE, new Location(villager.getWorld(), villager.getLocation().getBlockX(), -10000, villager.getLocation().getBlockZ()));
        }
        if (villager.getMemory(MemoryKey.MEETING_POINT) == null) {
            villager.setMemory(MemoryKey.MEETING_POINT, new Location(villager.getWorld(), villager.getLocation().getBlockX(), -10000, villager.getLocation().getBlockZ()));
        }
    }

    public boolean isPlaceholderMemory(Villager villager, MemoryKey<Location> memoryKey) {
        Location memoryLocation = villager.getMemory(memoryKey);
        return memoryLocation != null && memoryLocation.getY() < 0;
    }

    public void clearPlaceholderMemories(Villager villager) {

        if (villager.getMemory(MemoryKey.HOME) != null && isPlaceholderMemory(villager, MemoryKey.HOME)) {
            villager.setMemory(MemoryKey.HOME, null);
        }
        if (villager.getMemory(MemoryKey.JOB_SITE) != null && isPlaceholderMemory(villager, MemoryKey.JOB_SITE)) {
            villager.setMemory(MemoryKey.JOB_SITE, null);
        }
        if (villager.getMemory(MemoryKey.MEETING_POINT) != null && isPlaceholderMemory(villager, MemoryKey.MEETING_POINT)) {
            villager.setMemory(MemoryKey.MEETING_POINT, null);
        }
    }

    public boolean isScheduleNormal(Villager villager) {
        try {
            return BEHAVIOUR_CONTROLLER_GET_SCHEDULE_METHOD.invoke(VILLAGER_GET_BEHAVIOR_CONTROLLER_METHOD.invoke(VILLAGER_GET_HANDLE_METHOD.invoke(villager))) == (villager.isAdult() ? (villager.getProfession() == Villager.Profession.NITWIT ? SCHEDULE_SIMPLE : SCHEDULE_VILLAGER_DEFAULT) : SCHEDULE_VILLAGER_BABY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

}
