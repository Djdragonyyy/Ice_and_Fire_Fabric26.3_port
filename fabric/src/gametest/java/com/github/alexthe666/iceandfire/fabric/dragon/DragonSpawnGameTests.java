package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonSpawnGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void stats(FireDragon d) {
        require(d.ageDays() >= 1 && d.ageDays() <= 80 && d.stage() <= 4,
                "Original initialized dragon grows one to eighty days");
        require(d.variant() >= 0 && d.variant() <= 3 && d.hunger() == 50 && d.getHealth() == d.getMaxHealth(),
                "Initialized dragon has a valid original color, hunger fifty and full grown health");
        require(!d.isTame() && d.command() == 0 && !d.isOrderedToSit() && !d.corpse(),
                "Ordinary initialized dragon is wild, standing and alive");
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void nativeSummonInitializesOriginalStatsWithoutRerandomizingCustomNbtOrReload(GameTestHelper h) throws Exception {
        var source = h.getLevel().getServer().createCommandSourceStack().withLevel(h.getLevel());
        var point = h.absoluteVec(new Vec3(24.5, 10, 24.5));
        for (int sample = 0; sample < 3; sample++) {
            var d = (FireDragon) SummonCommand.createEntity(source, IafContent.FIRE_DRAGON.builtInRegistryHolder(), point, new CompoundTag(), true);
            stats(d);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            d.saveWithoutId(output);
            var restored = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(restored.ageDays() == d.ageDays() && restored.variant() == d.variant() && restored.male() == d.male()
                    && restored.getHealth() == d.getHealth(), "Loading preserves the actual initialized age, color, sex and health");
            d.discard();
        }
        var tag = new CompoundTag(); tag.putInt("AgeTicks", 0); tag.putInt("Variant", 2); tag.putBoolean("Gender", true);
        var custom = (FireDragon) SummonCommand.createEntity(source, IafContent.FIRE_DRAGON.builtInRegistryHolder(), point, tag, false);
        require(custom.ageDays() == 0 && custom.variant() == 2 && custom.male(),
                "Native summon with explicit NBT bypasses random initialization and preserves a chosen newborn");
        custom.discard(); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void nativeSpawnAndGroupCallbackUseConfiguredGrowthWithoutVanillaBabyScaling(GameTestHelper h) {
        for (int x = 16; x <= 32; x++) for (int z = 16; z <= 32; z++) h.setBlock(new BlockPos(x, 7, z), Blocks.STONE);
        var old = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonHealth = 1000; config.dragonAttackDamage = 100;
            DragonCombatConfig.current = config;
            var at = h.absolutePos(new BlockPos(24, 8, 24));
            var nativeSpawn = IafContent.FIRE_DRAGON.spawn(h.getLevel(), at, EntitySpawnReason.COMMAND);
            require(nativeSpawn != null, "Native EntityType spawn succeeds"); stats(nativeSpawn);
            require(nativeSpawn.getMaxHealth() == DragonGrowth.maxHealth(nativeSpawn.ageDays(), 1000)
                    && nativeSpawn.getAttributeValue(Attributes.ATTACK_DAMAGE) == DragonGrowth.attackDamage(nativeSpawn.ageDays(), 100),
                    "Configured health and attack apply at actual native spawn");
            nativeSpawn.discard();
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            require(d.ageDays() == 0, "Raw factory used for eggs and NBT loading remains a newborn");
            d.setPos(Vec3.atBottomCenterOf(at));
            var group = new AgeableMob.AgeableMobGroupData(true, 1F); group.increaseGroupSizeByOne();
            require(d.finalizeSpawn(h.getLevel(), h.getLevel().getCurrentDifficultyAt(at), EntitySpawnReason.NATURAL, group) == group,
                    "Native group data is preserved through the actual callback");
            stats(d); d.refreshDimensions();
            require(group.getGroupSize() == 2 && d.getAge() == 0,
                    "Own growth initializes independently of the vanilla baby-group and breeding clock");
            require(Math.abs(d.getBbWidth() - 0.78F * DragonGrowth.scale(d.ageDays())) < 1.0E-5,
                    "Physical size uses the original dragon scale once, without vanilla baby halving");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
}
