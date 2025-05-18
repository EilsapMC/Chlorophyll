package me.mrhua269.chlorophyll.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.mrhua269.chlorophyll.Chlorophyll;
import me.mrhua269.chlorophyll.impl.ChlorophyllLevelTickLoop;
import me.mrhua269.chlorophyll.utils.TextColorUtils;
import me.mrhua269.chlorophyll.utils.bridges.ITaskSchedulingLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class StatusCommand {
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("chlorophyll")
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("status")
                                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("current")
                                                .requires(CommandSourceStack::isPlayer)
                                                .executes(commandContext -> {
                                                    final CommandSourceStack source = commandContext.getSource();
                                                    final ServerLevel level = source.getLevel();

                                                    final MutableComponent result = Component.literal("[").withColor(TextColorUtils.colorOrException("#cdd6f4"))
                                                            .append(Component.literal("Chlorophyll").withColor(TextColorUtils.colorOrException("#a6e3a1")))
                                                            .append(Component.literal("] \n").withColor(TextColorUtils.colorOrException("#cdd6f4")));

                                                    result.append(buildThreadPoolStats()).append("\n").withColor(TextColorUtils.colorOrException("#89dceb"));
                                                    result.append(buildStatMsg(level));

                                                    if (level instanceof ITaskSchedulingLevel) {
                                                        source.sendSuccess(() -> result, false);
                                                    } else {
                                                        source.sendFailure(Component.literal("This world does not support chlorophyll."));
                                                    }

                                                    return 1;
                                                })
                                        )
                                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("all")
                                                .requires(CommandSourceStack::isPlayer)
                                                .executes(commandContext -> {
                                                    final CommandSourceStack source = commandContext.getSource();
                                                    final MutableComponent result = Component.literal("[").withColor(TextColorUtils.colorOrException("#cdd6f4"))
                                                            .append(Component.literal("Chlorophyll").withColor(TextColorUtils.colorOrException("#a6e3a1")))
                                                            .append(Component.literal("] \n").withColor(TextColorUtils.colorOrException("#cdd6f4")));
                                                    result.append(buildThreadPoolStats()).withColor(TextColorUtils.colorOrException("#89dceb"));

                                                    for (ServerLevel level : Chlorophyll.server.getAllLevels()) {
                                                        result.append(Component.literal("\n\n"));
                                                        result.append(buildStatMsg(level));
                                                    }

                                                    source.sendSuccess(() -> result, false);
                                                    return 1;
                                                })
                                        )
                        )
        );
    }

    public static @NotNull Component buildThreadPoolStats() {
        return Component.literal("Thread pool stats: ")
                .append("\n  Core pool size: ").withColor(TextColorUtils.colorOrException("#89dceb"))
                .append(String.valueOf(Chlorophyll.workerPool.getCorePoolSize())).withColor(TextColorUtils.colorOrException("#89dceb"))
                .append(Component.literal("\n  Active threads: ").withColor(TextColorUtils.colorOrException("#89dceb")))
                .append(String.valueOf(Chlorophyll.workerPool.getActiveCount()))
                .append(Component.literal("\n  Total tasks: ").withColor(TextColorUtils.colorOrException("#89dceb")))
                .append(String.valueOf(Chlorophyll.workerPool.getTaskCount()))
                .append(Component.literal("\n  Completed tasks: ").withColor(TextColorUtils.colorOrException("#89dceb")))
                .append(String.valueOf(Chlorophyll.workerPool.getCompletedTaskCount()));
    }

    public static @NotNull Component buildStatMsg(ServerLevel level) {
        final ChlorophyllLevelTickLoop tickLoop = ((ITaskSchedulingLevel) level).chlorophyll$getTickLoop();

        final MutableComponent msg =
                Component.literal("Current tick loop: ").append(level.dimension().location().toString()).withColor(TextColorUtils.colorOrException("#89dceb"));
        msg.append(Component.literal("\nCurrent mspt: ").append(String.format("%.3f", (double) tickLoop.getLastTickTime() / 1_000_000L))).append("ms").withColor(TextColorUtils.colorOrException("#89dceb"));
        msg.append(Component.literal("\nCurrent tick count: ").append(String.valueOf(tickLoop.getTickCount())).withColor(TextColorUtils.colorOrException("#89dceb")));

        return msg;
    }
}
