package ru.p4ejlov0d.galateahunter.utils.registries;

import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.command.Command;

public class CommandsRegistrar {
    public static void register(@NotNull Command command) {
        command.register();
    }
}
