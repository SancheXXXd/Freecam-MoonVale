package net.xolt.freecam.config.model;

import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface CollisionPredicate {
   boolean shouldIgnore(Block var1);

   static CollisionPredicate create(ModConfigDTO.CollisionConfig config) {
      if (config.ignoreAll) {
         return CollisionPredicates.IGNORE;
      } else {
         CollisionPredicateBuilder builder = CollisionPredicateBuilder.builder();
         if (config.ignoreTransparent) {
            builder.matching(CollisionPredicates.IGNORE_TRANSPARENT);
         }

         if (config.ignoreOpenable) {
            builder.matching(CollisionPredicates.IGNORE_OPENABLE);
         }

         if (config.ignoreCustom) {
            builder.matching(CollisionPredicates.whitelist(config.whitelist));
         }

         return builder.build();
      }
   }
}
