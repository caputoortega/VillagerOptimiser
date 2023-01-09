package ar.com.caputo.villageroptimiser.tasks;

import org.bukkit.Bukkit;

import ar.com.caputo.villageroptimiser.VillagerOptimiser;

import java.lang.reflect.InvocationTargetException;

public class CompatibilityCheckTask implements Runnable {
    private final static String NMS_NAME = Bukkit.getServer().getClass().getPackageName();
    private final static String VERSION = NMS_NAME.substring(NMS_NAME.lastIndexOf('.') + 1);

    private VillagerOptimiser avl;
    private String[] supportedVersions = new String[]{"v1_19_R1"};

    private boolean pass;

    public CompatibilityCheckTask(VillagerOptimiser avl) {
        this.avl = avl;
        run();
    }

    @Override
    public void run() {
        try {
            Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.memory.CraftMemoryMapper").getMethod("toNms", Object.class).invoke(null, (Object) null);
        } catch (InvocationTargetException e) {
            pass = false;
            disablePlugin("You need to be using a more recent build of Craftbukkit/Spigot/Paper!");
            return;
        } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
            pass = false;
            disablePlugin("This plugin is not compatible with the version of Minecraft you are using (" + NMS_NAME + ")");
            return;
        }
        for (String string : supportedVersions) {
            if (string.equals(VERSION)) {
                pass = true;
                break;
            }
            pass = false;
        }
        if (!pass) {
            disablePlugin("This plugin is not compatible with the version of Minecraft you are using.");
        }
    }

    private void disablePlugin(String message) {
        VillagerOptimiser.logger().warning(message);
        Bukkit.getPluginManager().disablePlugin(avl);
    }

    public boolean passedCheck() {
        return pass;
    }
}
