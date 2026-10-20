package net.xolt.freecam.config.model;

import java.util.regex.Pattern;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;

class CollisionPredicates {
   static final CollisionPredicate COLLIDE = block -> false;
   static final CollisionPredicate IGNORE = block -> true;
   static final CollisionPredicate IGNORE_TRANSPARENT = CollisionPredicateBuilder.builder()
      .matching(TransparentBlock.class)
      .matching(IronBarsBlock.class)
      .matching(BarrierBlock.class)
      .build();
   static final CollisionPredicate IGNORE_OPENABLE = CollisionPredicateBuilder.builder()
      .matching(FenceGateBlock.class)
      .matching(DoorBlock.class, TrapDoorBlock.class)
      .build();

   private CollisionPredicates() {
   }

   static CollisionPredicate whitelist(ModConfigDTO.CollisionConfig.CollisionWhitelist whitelist) {
      String[] ids = whitelist.ids.stream().map(id -> (String)(id.contains(":") ? id : "minecraft:" + id)).toArray(String[]::new);
      Pattern[] patterns = whitelist.patterns.stream().map(Pattern::compile).toArray(Pattern[]::new);
      return CollisionPredicateBuilder.builder().matching(ids).matching(patterns).build();
   }
}
