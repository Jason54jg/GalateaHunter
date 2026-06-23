package ru.p4ejlov0d.galateahunter.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.screen.GalateaHunterScreen;

public class MainGuiCommand implements Command {
    @Override
    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess)
                -> dispatcher.register(ClientCommands.literal("gh").executes(this::execute))
        );
    }

    private int execute(@NotNull CommandContext<FabricClientCommandSource> context) {
        Minecraft client = context.getSource().getClient();
        client.schedule(() -> client.gui.setScreen(GalateaHunterScreen.createGui()));

        return 1;
    }
}
