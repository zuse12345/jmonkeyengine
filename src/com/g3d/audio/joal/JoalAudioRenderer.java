package com.g3d.audio.joal;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioData;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.AudioSource;
import com.g3d.audio.AudioSource.Status;
import com.g3d.audio.DirectionalAudioSource;
import com.g3d.audio.Environment;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;
import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALFactory;
import org.lwjgl.openal.AL10;

public class JoalAudioRenderer implements AudioRenderer {

    private static final Logger logger = Logger.getLogger(JoalAudioRenderer.class.getName());

    private AL al;
    private ALC alc;
    private ALCdevice device;
    private ALCcontext context;

    private final IntBuffer ib = BufferUtils.createIntBuffer(16);
    private final FloatBuffer fb = BufferUtils.createVector3Buffer(2);

    private ArrayList<AudioSource> playingList = new ArrayList<AudioSource>();
    private Camera listener;

    private int[] channels = new int[16];
    private AudioSource[] chanSrcs = new AudioSource[16];
    private int nextChan = 0;
    private ArrayList<Integer> freeChans = new ArrayList<Integer>();
    private boolean supportEfx = false;

    public JoalAudioRenderer(){
    }

    public void initialize() {
        alc = ALFactory.getALC();
        device = alc.alcOpenDevice(null);
        context = alc.alcCreateContext(device, null);

        alc.alcMakeContextCurrent(context);

        al = ALFactory.getAL();
        al.alGetError();

        System.out.println("Vendor: "   + al.alGetString(AL.AL_VENDOR));
        System.out.println("Renderer: " + al.alGetString(AL.AL_RENDERER));
        System.out.println("Version: "  + al.alGetString(AL.AL_VERSION));

        supportEfx = alc.alcIsExtensionPresent(device, "ALC_EXT_EFX");
        System.out.println("Audio effect extension supported: "+supportEfx);

        // Create channel sources
        ib.rewind();
        al.alGenSources(ib.remaining(), ib);
        ib.clear();
        ib.get(channels);
        ib.rewind();
    }

    public void cleanup(){
        // delete channel-based sources
        ib.rewind();
        ib.put(channels);
        ib.flip();
        al.alDeleteSources(ib.remaining(), ib);

        alc.alcMakeContextCurrent(null);
        alc.alcDestroyContext(context);
        alc.alcCloseDevice(device);

        alc = null;
        al = null;
        device = null;
        context = null;
    }

    private void setListenerParams(Vector3f pos, Vector3f vel, Vector3f dir, Vector3f up){
        al.alListener3f(AL.AL_POSITION, pos.x, pos.y, pos.z);
        al.alListener3f(AL.AL_VELOCITY, vel.x, vel.y, vel.z);
        fb.rewind();
        fb.put(dir.x).put(dir.y).put(dir.z);
        fb.put(up.x).put(up.y).put(up.z);
        fb.flip();
        al.alListenerfv(AL.AL_ORIENTATION, fb);
    }

    private void setEffect(int id, Environment env){
        ib.position(0).limit(1);
        al.alGenAuxiliaryEffectSlots(1, ib);
        int fxslot = ib.get(0);

        ib.position(0).limit(1);
        al.alGenEffects(1, ib);
        int fx = ib.get(0);
        
        setEnvironment(fx, env);

        al.alAuxiliaryEffectSloti(fxslot, AL.AL_EFFECTSLOT_EFFECT, fx);
        al.alSource3i(id, AL.AL_AUXILIARY_SEND_FILTER, fxslot, 0, AL.AL_FILTER_NULL);
    }

    private void setEnvironment(int fx, Environment env){
        al.alEffecti(fx, AL.AL_EFFECT_TYPE, AL.AL_EFFECT_REVERB);

        al.alEffectf(fx, AL.AL_REVERB_DENSITY,             env.getDensity());

        al.alEffectf(fx, AL.AL_REVERB_DIFFUSION,           env.getDiffusion());
        al.alEffectf(fx, AL.AL_REVERB_GAIN,                env.getGain());
        al.alEffectf(fx, AL.AL_REVERB_GAINHF,              env.getGainHf());

        al.alEffectf(fx, AL.AL_REVERB_DECAY_TIME,          env.getDecayTime());
        al.alEffectf(fx, AL.AL_REVERB_DECAY_HFRATIO,       env.getDecayHFRatio());

        al.alEffectf(fx, AL.AL_REVERB_REFLECTIONS_GAIN,    env.getReflectGain());
        al.alEffectf(fx, AL.AL_REVERB_REFLECTIONS_DELAY,   env.getReflectDelay());

        al.alEffectf(fx, AL.AL_REVERB_LATE_REVERB_GAIN,    env.getLateReverbGain());
        al.alEffectf(fx, AL.AL_REVERB_LATE_REVERB_DELAY,   env.getLateReverbDelay());

        al.alEffectf(fx, AL.AL_REVERB_AIR_ABSORPTION_GAINHF, env.getAirAbsorbGainHf());
        al.alEffectf(fx, AL.AL_REVERB_ROOM_ROLLOFF_FACTOR, env.getRoomRolloffFactor());
    }

    private void setSourceParams(int id, AudioSource src, boolean forceNonLoop){
        if (src.isPositional()){
            Vector3f pos = src.getPosition();
            Vector3f vel = src.getVelocity();
            al.alSource3f(id, AL.AL_POSITION, pos.x, pos.y, pos.z);
            al.alSource3f(id, AL.AL_VELOCITY, vel.x, vel.y, vel.z);
        }else{
            // play in headspace
            al.alSourcei(id, AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);
            al.alSource3f(id, AL.AL_POSITION, 0,0,0);
            al.alSource3f(id, AL.AL_VELOCITY, 0,0,0);
        }

        if (forceNonLoop){
            al.alSourcei(id,  AL.AL_LOOPING, AL.AL_FALSE);
        }else{
            al.alSourcei(id,  AL.AL_LOOPING, src.isLooping() ? AL.AL_TRUE : AL.AL_FALSE);
        }

        al.alSourcef(id,  AL.AL_GAIN, src.getVolume());
        al.alSourcef(id,  AL.AL_PITCH, src.getPitch());
        al.alSourcef(id,  AL.AL_SEC_OFFSET, src.getTimeOffset());

        if (src instanceof DirectionalAudioSource){
            DirectionalAudioSource das = (DirectionalAudioSource) src;
            Vector3f dir = das.getDirection();
            al.alSource3f(id, AL.AL_DIRECTION, dir.x, dir.y, dir.z);
            al.alSourcef(id, AL.AL_CONE_INNER_ANGLE, das.getInnerAngle());
            al.alSourcef(id, AL.AL_CONE_OUTER_ANGLE, das.getOuterAngle());
            al.alSourcef(id, AL.AL_CONE_OUTER_GAIN,  0);
        }else{
            al.alSourcef(id, AL.AL_CONE_INNER_ANGLE, 360);
            al.alSourcef(id, AL.AL_CONE_OUTER_ANGLE, 360);
            al.alSourcef(id, AL.AL_CONE_OUTER_GAIN, 1f);
        }
    }

    public void update(float tpf){
        // delete channel-based sources that finished playing
        for (int i = 0; i < channels.length; i++){
            AudioSource src = chanSrcs[i];
            int sourceId = channels[i];
            if (src != null){
                ib.position(0).limit(1);
                al.alGetSourcei(sourceId, AL.AL_SOURCE_STATE, ib);
                if (ib.get(0) == AL.AL_STOPPED){
                    chanSrcs[i] = null;
                    freeChannel(i);
                    System.out.println("Freed index "+i);
                }
            }
        }

        ArrayList<AudioSource> toRem = new ArrayList<AudioSource>();
        for (AudioSource src : playingList){
            int id = src.getId();
            if (src.isUpdateNeeded()){
                updateSource(src);
            }
            ib.position(0).limit(1);
            al.alGetSourcei(id, AL.AL_SOURCE_STATE, ib);
            if (ib.get(0) == AL.AL_STOPPED){
                toRem.add(src);
                src.setStatus(Status.Stopped);
            }
        }
        for (AudioSource src : toRem)
            playingList.remove(src);

        if (listener != null){
            Vector3f pos = listener.getLocation();
            Vector3f dir = listener.getDirection();
            Vector3f up = listener.getUp();
            setListenerParams(pos, Vector3f.ZERO, dir, up);
        }
    }

    private int newChannel(){
        if (freeChans.size() > 0)
            return freeChans.remove(0);
        else
            return nextChan++;
    }

    private void freeChannel(int index){
        if (index == nextChan-1)
            nextChan--;
        else
            freeChans.add(index);
    }

    public void setListener(Camera listener) {
        this.listener = listener;
    }

    public void playSourceInstance(AudioSource src){
        if (src.getAudioData().isUpdateNeeded()){
            updateAudioData(src.getAudioData());
        }

        // create a new index for an audio-channel
        int index = newChannel();

        int sourceId = channels[index];

        // assert that the channel is free since the index is available
        assert chanSrcs[index] == null;

        // set the source to play on the provided channel
        chanSrcs[index] = src;

        // attach the audio data onto the channel
        al.alSourcei(sourceId, AL.AL_BUFFER, src.getAudioData().getId());

        // set parameters, like position and max distance

        // if original source has looping enabled- ignore it
        // If these sources were looped, they will never become AL_STOPPED
        // which means the channel will be hogged forever
        setSourceParams(sourceId, src, true);

        al.alSourceStop(sourceId);
        al.alSourceRewind(sourceId);

        // play the channel
        al.alSourcePlay(sourceId);

        System.out.println("Playing on "+index);
    }

    public void playSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Playing){
            al.alSourcePlay(src.getId());
            src.setStatus(Status.Playing);
            playingList.add(src);
        }
    }

    public void pauseSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Paused){
            al.alSourcePause(src.getId());
            src.setStatus(Status.Paused);
            playingList.remove(src);
        }
    }

    public void stopSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Stopped){
            al.alSourceStop(src.getId());
            src.setStatus(Status.Stopped);
            playingList.remove(src);
        }
    }

    public void updateSource(AudioSource src) {
        AudioData audioData = src.getAudioData();
        if (audioData == null){
            logger.warning("Attempted to load source with no audio data!");
            return;
        }

        // update audio data first before continuing
        if (audioData.isUpdateNeeded())
            updateAudioData(audioData);

        int id = src.getId();
        if (id == -1){
            ib.position(0).limit(1);
            al.alGenSources(ib.remaining(), ib);
            id = ib.get(0);
            src.setId(id);

            // also need to attach data
            // note that it already has an id
            al.alSourcei(id, AL.AL_BUFFER, audioData.getId());

            setEffect(id, new Environment());
        }

        setSourceParams(id, src, false);
        src.clearUpdateNeeded();
    }

    public void deleteSource(AudioSource src) {
        int id = src.getId();
        if (id != -1){
            ib.put(0,id);
            ib.position(0).limit(1);
            al.alDeleteSources(ib.remaining(), ib);
            src.resetObject();
        }
    }

    private int convertFormat(AudioData ad){
        switch (ad.getBitsPerSample()){
            case 8:
                if (ad.getChannels() == 1)
                    return AL.AL_FORMAT_MONO8;
                else if (ad.getChannels() == 2)
                    return AL.AL_FORMAT_STEREO8;

                break;
            case 16:
                if (ad.getChannels() == 1)
                    return AL.AL_FORMAT_MONO16;
                else
                    return AL.AL_FORMAT_STEREO16;
        }
        throw new UnsupportedOperationException("Unsupported channels/bits combination: "+
                                                "bits="+ad.getBitsPerSample()+", channels="+ad.getChannels());
    }

    public void updateAudioBuffer(AudioBuffer ab){
        int id = ab.getId();
        if (ab.getId() == -1){
            ib.position(0).limit(1);
            al.alGenBuffers(ib.remaining(), ib);
            id = ib.get(0);
            ab.setId(id);
        }

        al.alBufferData(id, convertFormat(ab), ab.getData(), ab.getData().remaining(), ab.getSampleRate());
        ab.clearUpdateNeeded();
    }

    public void updateAudioData(AudioData ad){
        if (ad instanceof AudioBuffer){
            updateAudioBuffer((AudioBuffer) ad);
        }
    }

    public void deleteAudioData(AudioData ad){
        if (ad instanceof AudioBuffer){
            AudioBuffer ab = (AudioBuffer) ad;
            int id = ab.getId();
            if (id != -1){
                ib.put(0,id);
                ib.position(0).limit(1);
                al.alDeleteBuffers(ib.remaining(), ib);
                ab.resetObject();
            }
        }
    }

}
