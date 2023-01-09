package ar.com.caputo.villageroptimiser.reflected.nmw.entity;

import ar.com.caputo.villageroptimiser.reflected.ReflectedClass;
import ar.com.caputo.villageroptimiser.reflected.nmw.entity.ai.BehaviorController;

public class EntityLiving extends ReflectedClass {

    private BehaviorController behaviorController;

    public EntityLiving(Class<?> SELF) {
        super(SELF);
    }

    public BehaviorController getBehaviorController() {
        return new BehaviorController(null);
    }
    
}
