package ar.com.caputo.villageroptimiser.reflected.nmw.entity.ai;

import ar.com.caputo.villageroptimiser.reflected.ReflectedClass;
import ar.com.caputo.villageroptimiser.reflected.nmw.entity.schedule.Schedule;

public class BehaviorController extends ReflectedClass {

    private Schedule schedule;

    public BehaviorController(Class<?> SELF) {
        super(SELF);
    }
    
    public Schedule getSchedule() {
        return this.schedule;
    }

}
