package tv.slicedlime.tradertest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import tv.slicedlime.tradertest.mixin.ServerWorldMixin;
import tv.slicedlime.tradertest.mixin.WanderingTraderManagerMixin;

import java.util.List;

public class SpawnTraderCommand {

    private static final SimpleCommandExceptionType ERROR_ALL_FAILED =
            new SimpleCommandExceptionType(Component.literal("All spawn attempts failed"));

    private static final SimpleCommandExceptionType ERROR_NO_SPAWNER =
            new SimpleCommandExceptionType(Component.literal("No spawner available"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawntrader")
                .executes(SpawnTraderCommand::run));
    }

    // throws CommandSyntaxException — обов'язково, бо .create() є checked exception
    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel world = context.getSource().getLevel();
        WanderingTraderSpawner manager = getWanderingTraderManager(world);

        if (manager != null) {
            for (int i = 0; i < 1000; i++) {
                boolean success = ((WanderingTraderManagerMixin) manager).invokeSpawn(world);
                if (success) {
                    context.getSource().sendSuccess(() -> Component.literal("Success"), false);
                    return 1;
                }
            }
            throw ERROR_ALL_FAILED.create();
        } else {
            throw ERROR_NO_SPAWNER.create();
        }
    }

    static WanderingTraderSpawner getWanderingTraderManager(ServerLevel world) {
        List<CustomSpawner> spawners = ((ServerWorldMixin) world).getCustomSpawners();
        for (CustomSpawner spawner : spawners) {
            if (spawner instanceof WanderingTraderSpawner traderSpawner) {
                return traderSpawner;
            }
        }
        return null;
    }
}