package io.github.mattidragon.powernetworks.client.config;

import dev.isxander.yacl3.api.OptionFlag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class RequireDisconnectScreen extends ConfirmScreen {
    public static final OptionFlag FLAG = client -> {
        if (client.world != null) {

            client.setScreen(new RequireDisconnectScreen(client.currentScreen));
        }
    };

    public RequireDisconnectScreen(Screen parent) {
        super(accepted -> {
            var client = MinecraftClient.getInstance();
            if (accepted) {
                if (client.world != null)
                    client.world.disconnect();
                client.disconnect();
                client.setScreen(null);
            } else {
                client.setScreen(parent);
            }
        }, Text.translatable("screen.power_networks.disconnect.title"), Text.translatable("screen.power_networks.disconnect.message"));
    }
}
