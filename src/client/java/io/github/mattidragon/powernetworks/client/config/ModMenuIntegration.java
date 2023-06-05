package io.github.mattidragon.powernetworks.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.mattidragon.powernetworks.PowerNetworks;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigClient.createScreen(parent, PowerNetworks.CONFIG.get(), config -> PowerNetworks.CONFIG.set(config));
    }
}
