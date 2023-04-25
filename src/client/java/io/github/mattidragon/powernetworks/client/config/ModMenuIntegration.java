package io.github.mattidragon.powernetworks.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.mattidragon.powernetworks.config.PowerNetworksConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigClient.createScreen(parent, PowerNetworksConfig.get(), PowerNetworksConfig::set);
    }
}
