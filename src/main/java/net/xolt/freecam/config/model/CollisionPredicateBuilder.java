package net.xolt.freecam.config.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

class CollisionPredicateBuilder {
   private final Collection<CollisionPredicate> predicates = new ArrayList<>();

   private CollisionPredicateBuilder() {
   }

   public static CollisionPredicateBuilder builder() {
      return new CollisionPredicateBuilder();
   }

   public final CollisionPredicateBuilder matching(String... ids) {
      return this.matching(block -> Arrays.asList(ids).contains(getBlockId(block)));
   }

   public final CollisionPredicateBuilder matching(Pattern... patterns) {
      return this.matching(block -> {
         String id = getBlockId(block);
         return Arrays.stream(patterns).map(pattern -> pattern.matcher(id)).anyMatch(Matcher::find);
      });
   }

   @SafeVarargs
   public final CollisionPredicateBuilder matching(Class<? extends Block>... classes) {
      return this.matching(block -> Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(block)));
   }

   public final CollisionPredicateBuilder matching(CollisionPredicate predicate) {
      this.predicates.add(predicate);
      return this;
   }

   public CollisionPredicate build() {
      return block -> this.predicates.stream().anyMatch(predicate -> predicate.shouldIgnore(block));
   }

   private static String getBlockId(Block block) {
      return BuiltInRegistries.BLOCK.getKey(block).toString();
   }
}
