#!/usr/bin/env python3
"""Exercise compiled voice hooks using isolated Minecraft/OpenAL stubs, not a game client."""
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
STUBS = {
'net/minecraft/world/phys/Vec3.java': '''package net.minecraft.world.phys; public class Vec3 { public final double x,y,z; public Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;} }''',
'net/minecraft/world/entity/Entity.java': '''package net.minecraft.world.entity; import net.minecraft.world.phys.Vec3; public class Entity { public Vec3 eye=new Vec3(0,65,0); public Vec3 getEyePosition(){return eye;} }''',
'net/minecraft/client/player/LocalPlayer.java': '''package net.minecraft.client.player; public class LocalPlayer extends net.minecraft.world.entity.Entity {}''',
'org/joml/Vector3fc.java': '''package org.joml; public interface Vector3fc { float x();float y();float z(); }''',
'net/minecraft/client/Camera.java': '''package net.minecraft.client; import org.joml.Vector3fc; public class Camera { public record V(float x,float y,float z) implements Vector3fc {} public net.minecraft.world.phys.Vec3 pos=new net.minecraft.world.phys.Vec3(0,65,0);public net.minecraft.world.phys.Vec3 position(){return pos;}public Vector3fc forward=new V(0,0,-1),up=new V(0,1,0); public Vector3fc forwardVector(){return forward;} public Vector3fc upVector(){return up;} }''',
'net/minecraft/client/renderer/GameRenderer.java': '''package net.minecraft.client.renderer; public class GameRenderer {public final net.minecraft.client.Camera camera=new net.minecraft.client.Camera();public net.minecraft.client.Camera mainCamera(){return camera;}}''',
'net/minecraft/client/Minecraft.java': '''package net.minecraft.client; public class Minecraft {public net.minecraft.client.player.LocalPlayer player=new net.minecraft.client.player.LocalPlayer(); public net.minecraft.world.entity.Entity cameraEntity=player; public final net.minecraft.client.renderer.GameRenderer gameRenderer=new net.minecraft.client.renderer.GameRenderer(); public net.minecraft.world.entity.Entity getCameraEntity(){return cameraEntity;}}''',
'net/xolt/freecam/Freecam.java': '''package net.xolt.freecam; public class Freecam {public static final net.minecraft.client.Minecraft MC=new net.minecraft.client.Minecraft();public static boolean enabled;public static boolean isEnabled(){return enabled;}}''',
'net/xolt/freecam/util/FreeCamera.java': '''package net.xolt.freecam.util; public class FreeCamera extends net.minecraft.world.entity.Entity {}''',
'org/lwjgl/openal/AL11.java': '''package org.lwjgl.openal; public class AL11 {public static float[] position,orientation,source;public static int sourceParameter;public static void alSourcefv(int pointer,int parameter,float[] values){source=values.clone();sourceParameter=parameter;} public static void alListener3f(int p,float x,float y,float z){position=new float[]{x,y,z};}public static void alListenerfv(int p,float[] v){orientation=v.clone();}}''',
'org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable.java': '''package org.spongepowered.asm.mixin.injection.callback; public class CallbackInfoReturnable<T> { public T value;public boolean cancelled; public void setReturnValue(T value){this.value=value;cancelled=true;}}''',
'VoiceRoutingTest.java': '''
import java.lang.reflect.*;
import java.util.Arrays;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.mixins.PlasmoVoiceListenerMixin;
import net.xolt.freecam.mixins.PlasmoVoiceSourceMixin;
import net.xolt.freecam.mixins.PlasmoVoiceSpatialMixin;
import net.xolt.freecam.util.VoiceSpatialTransform;
import net.xolt.freecam.util.FreeCamera;
import net.minecraft.client.Camera;
import org.lwjgl.openal.AL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class VoiceRoutingTest {
 static class Spatial extends PlasmoVoiceSpatialMixin {boolean relative;public boolean isRelative(){return relative;}}
 static int checks;
 static void check(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
 static void array(float[] actual,float...expected){check(Arrays.equals(actual,expected));}
 public static void main(String[] args)throws Exception{
  Object output=new PlasmoVoiceListenerMixin(), source=new PlasmoVoiceSourceMixin();
  Method position=output.getClass().getDeclaredMethod("freecam$anchorVoicePosition",int.class,float.class,float.class,float.class);
  Method rotation=output.getClass().getDeclaredMethod("freecam$rotateVoiceWithCamera",int.class,float[].class);
  Method listener=source.getClass().getDeclaredMethod("freecam$usePlayerForVoiceDistance",CallbackInfoReturnable.class);
  position.setAccessible(true);rotation.setAccessible(true);listener.setAccessible(true);
  Freecam.enabled=true;Freecam.MC.cameraEntity=new FreeCamera();
  position.invoke(output,4100,100f,80f,200f);array(AL11.position,0,65,0);
  position.invoke(output,4100,-200f,10f,-400f);array(AL11.position,0,65,0);
  CallbackInfoReturnable<Object> cb=new CallbackInfoReturnable<>();listener.invoke(source,cb);check(cb.cancelled&&cb.value==Freecam.MC.player);
  rotation.invoke(output,4111,new float[]{1,0,0,0,1,0});array(AL11.orientation,0,0,-1,0,1,0);
  Freecam.MC.gameRenderer.camera.forward=new Camera.V(-1,0,0);
  rotation.invoke(output,4111,new float[]{0,0,-1,0,1,0});array(AL11.orientation,-1,0,0,0,1,0);
  position.invoke(output,4100,50f,30f,60f);array(AL11.position,0,65,0);
  Freecam.enabled=false;
  cb=new CallbackInfoReturnable<>();listener.invoke(source,cb);check(cb.cancelled&&cb.value==Freecam.MC.player);
  Freecam.MC.cameraEntity=Freecam.MC.player;
  cb=new CallbackInfoReturnable<>();listener.invoke(source,cb);check(!cb.cancelled);
  position.invoke(output,4100,3f,4f,5f);array(AL11.position,3,4,5);
  float[] vanilla={0,0,1,0,1,0};rotation.invoke(output,4111,vanilla);array(AL11.orientation,vanilla);
  Freecam.enabled=true;Freecam.MC.player=null;
  cb=new CallbackInfoReturnable<>();listener.invoke(source,cb);check(!cb.cancelled);
  position.invoke(output,4100,6f,7f,8f);array(AL11.position,6,7,8);
  Freecam.MC.player=new net.minecraft.client.player.LocalPlayer();Freecam.MC.cameraEntity=new FreeCamera();Freecam.enabled=true;
  Spatial spatial=new Spatial();
  Method project=PlasmoVoiceSpatialMixin.class.getDeclaredMethod("freecam$projectVoiceBearing",int.class,int.class,float[].class);project.setAccessible(true);
  Freecam.MC.gameRenderer.camera.pos=new net.minecraft.world.phys.Vec3(-8,65,0);
  float[] original={-4,65,0};project.invoke(spatial,7,4100,original);array(AL11.source,4,65,0);array(original,-4,65,0);
  position.invoke(output,4100,-8f,65f,0f);array(AL11.position,0,65,0);
  cb=new CallbackInfoReturnable<>();listener.invoke(source,cb);check(cb.cancelled&&cb.value==Freecam.MC.player);
  Freecam.MC.gameRenderer.camera.pos=new net.minecraft.world.phys.Vec3(0,65,0);
  project.invoke(spatial,7,4100,original);array(AL11.source,-4,65,0);
  Freecam.MC.gameRenderer.camera.pos=new net.minecraft.world.phys.Vec3(-3.99,65,0);
  project.invoke(spatial,7,4100,original);array(AL11.source,-4,65,0);
  spatial.relative=true;Freecam.MC.gameRenderer.camera.pos=new net.minecraft.world.phys.Vec3(-8,65,0);
  project.invoke(spatial,7,4100,original);array(AL11.source,-4,65,0);
  spatial.relative=false;project.invoke(spatial,7,4102,original);array(AL11.source,-4,65,0);
  Freecam.enabled=false;Freecam.MC.cameraEntity=Freecam.MC.player;
  project.invoke(spatial,7,4100,original);array(AL11.source,-4,65,0);
  array(VoiceSpatialTransform.reproject(0,65,0,-4,65,0,original),-4,65,0);
  array(VoiceSpatialTransform.reproject(0,65,0,3,70,10,new float[]{0,65,0}),0,65,0);
  java.util.Random random=new java.util.Random(116);
  for(int i=0;i<1000;i++){
   double px=random.nextDouble()*20-10,py=65,pz=random.nextDouble()*20-10;
   double cx=random.nextDouble()*20-10,cy=60+random.nextDouble()*10,cz=random.nextDouble()*20-10;
   float[] real={(float)(random.nextDouble()*20-10),66,(float)(random.nextDouble()*20-10)};
   float[] virtual=VoiceSpatialTransform.reproject(px,py,pz,cx,cy,cz,real);
   double actual=Math.sqrt(Math.pow(real[0]-px,2)+Math.pow(real[1]-py,2)+Math.pow(real[2]-pz,2));
   double preserved=Math.sqrt(Math.pow(virtual[0]-px,2)+Math.pow(virtual[1]-py,2)+Math.pow(virtual[2]-pz,2));
   check(Math.abs(actual-preserved)<1e-4);
   double dot=(virtual[0]-px)*(real[0]-cx)+(virtual[1]-py)*(real[1]-cy)+(virtual[2]-pz)*(real[2]-cz);
   double bearing=Math.sqrt(Math.pow(real[0]-cx,2)+Math.pow(real[1]-cy,2)+Math.pow(real[2]-cz,2));
   check(dot/(preserved*bearing)>0.999999);
  }
  System.out.println("Passed "+checks+" production-bytecode voice-routing and spatial-projection checks with stubs; no live audio/game test.");
 }
}'''
}

def tool(name):
    home = os.environ.get('JAVA_HOME')
    return str(Path(home) / 'bin' / name) if home else name

def main():
    classes = ROOT / 'build/classes/java/main'
    if not (classes / 'net/xolt/freecam/mixins/PlasmoVoiceListenerMixin.class').exists():
        raise SystemExit('Run ./gradlew classes first.')
    with tempfile.TemporaryDirectory(prefix='voice-routing-') as tmp:
        base=Path(tmp); output=base/'classes'; output.mkdir()
        paths=[]
        for name, content in STUBS.items():
            path=base/'src'/name;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(content);paths.append(str(path))
        subprocess.run([tool('javac'),'-cp',str(classes),'-d',str(output),*paths],check=True)
        subprocess.run([tool('java'),'-cp',str(output)+os.pathsep+str(classes),'VoiceRoutingTest'],check=True)

if __name__ == '__main__':
    main()
