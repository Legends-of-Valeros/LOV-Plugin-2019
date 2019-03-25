package com.legendsofvaleros.modules.professions;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;

import java.util.ArrayList;

/**
 * Created by Crystall on 02/12/2019
 */
public class ProfessionsController extends ProfessionsAPI {
    private static ArrayList<Module> professionModules = new ArrayList<>();

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    public void onRegionEnter(RegionEnterEvent e) {
        e.getRegion().
        professionModules.forEach(module -> {
            module.load
        });
    }

    public void onRegionLeave(RegionLeaveEvent e) {

    }
}
