#!/usr/bin/env python3
"""Validate mixin target bytecode in the supplied Plasmo Voice build; not a live audio test."""
import os
from pathlib import Path
import subprocess
import sys
import zipfile
import json

ROOT=Path(__file__).resolve().parents[1]

def disassemble(jar, cls):
    home=os.environ.get('JAVA_HOME')
    command=str(Path(home)/'bin/javap') if home else 'javap'
    return subprocess.check_output([command,'-p','-c','-classpath',str(jar),cls],text=True)

def main():
    if len(sys.argv)!=2:raise SystemExit('Usage: python3 scripts/verify_plasmo_voice.py /path/to/plasmovoice.jar')
    jar=Path(sys.argv[1])
    with zipfile.ZipFile(jar) as z:
        metadata=json.loads(z.read('fabric.mod.json'))
        assert metadata['id']=='plasmovoice' and metadata['version']=='2.1.17',metadata
    listener=disassemble(jar,'su.plo.voice.client.audio.device.AlOutputDevice$AlListener')
    body=listener[listener.index('  public final void update();'):]
    assert body.count('org/lwjgl/openal/AL11.alListener3f:(IFFF)V')==1
    assert body.count('org/lwjgl/openal/AL11.alListenerfv:(I[F)V')==1
    source=disassemble(jar,'su.plo.voice.client.audio.source.BaseClientAudioSource')
    assert 'protected final net.minecraft.world.entity.Entity getListener();' in source
    assert 'Method getListener:()Lnet/minecraft/world/entity/Entity;' in source
    assert 'Method getListenerPosition:()Lnet/minecraft/world/phys/Vec3;' in source
    assert 'Method net/minecraft/world/phys/Vec3.distanceTo:(Lnet/minecraft/world/phys/Vec3;)D' in source
    assert 'Method calculateDistanceGain:(DD)D' in source
    al_source=disassemble(jar,'su.plo.voice.client.audio.device.source.BaseAlSource')
    assert 'public boolean isRelative();' in al_source
    assert 'public void setFloatArray(int, float[]);' in al_source
    assert al_source.count('org/lwjgl/openal/AL11.alSourcefv:(II[F)V')==1
    print('Plasmo Voice 2.1.17 listener and spatial-source hooks and source-distance call chain: OK (static bytecode checks).')

if __name__=='__main__':main()
