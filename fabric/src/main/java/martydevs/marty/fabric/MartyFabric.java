package martydevs.marty.fabric;

import net.fabricmc.api.ModInitializer;

import martydevs.marty.Marty;

public final class MartyFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Marty.init();
    }

}
