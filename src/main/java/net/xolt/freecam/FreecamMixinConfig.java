package net.xolt.freecam;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class FreecamMixinConfig implements IMixinConfigPlugin {
   private static final Map<String, String> MOD_MIXINS = Map.of(
      "net.xolt.freecam.mixins.IrisHandRendererMixin", "iris", "net.xolt.freecam.mixins.IrisShadowRendererMixin", "iris"
   );

   public void onLoad(String mixinPackage) {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      String modId = MOD_MIXINS.get(mixinClassName);
      return modId == null || ModPlatform.get().isModLoaded(modId);
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public String getRefMapperConfig() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
