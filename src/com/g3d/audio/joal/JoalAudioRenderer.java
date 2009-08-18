package com.g3d.audio.joal;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioData;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.AudioSource;
import com.g3d.audio.AudioSource.Status;
import com.g3d.audio.AudioStream;
import com.g3d.audio.DirectionalAudioSource;
import com.g3d.audio.Environment;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.util.BufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALFactory;

/**
 * Implementation of the AudioRenderer for JOAL.
 * Supports reverb effects with environments.
 * @author Kirill
 */
public class JoalAudioRenderer implements AudioRenderer {

    private static final Logger logger = Logger.getLogger(JoalAudioRenderer.class.getName());

    private static final int BUFFER_SIZE = 16384;
    private static final int STREAMING_BUFFER_COUNT = 2;

    private AL al;
    private ALC alc;
    private ALCdevice device;
    private ALCcontext context;

    private final IntBuffer ib = BufferUtils.createIntBuffer(16);
    private final FloatBuffer fb = BufferUtils.createVector3Buffer(2);
    private final ByteBuffer nativeBuf = BufferUtils.createByteBuffer(BUFFER_SIZE);
    private final byte[] arrayBuf = new byte[BUFFER_SIZE];

    /**
     * Audio sources that are currently playing.
     */
    private ArrayList<AudioSource> playingList = new ArrayList<AudioSource>();

    /**
     * The listener. The location, direction, and up vector is used to place the
     * listener inside the audio environment.
     */
    private Camera listener;

    /**
     * Channels used for instanced source playback.
     * A channel is an OpenAL source id that is allocated
     * dynamically to an AudioSource for instanced play with
     * the playSourceInstance method.
     */
    private int[] channels = new int[16];

    /**
     * Mapping of channels to the sources currently attached to them.
     */
    private AudioSource[] chanSrcs = new AudioSource[16];

    /**
     * The next (free) channel. This variable is incremented
     * as more instanced sources are played.
     */
    private int nextChan = 0;

    /**
     * Channels that have been used and then free'd. If not empty
     * should be used instead of the <code>nextChan</code> variable.
     */
    private ArrayList<Integer> freeChans = new ArrayList<Integer>();

    /**
     * True if the underlying implementation supports the ALC_EXT_EFX
     * extension. This is used for reverb effects in the method
     * setEnvironment().
     */
    private boolean supportEfx = false;
    private int auxSends = 0;

    private int reverbFx = -1;
    private int reverbFxSlot = -1;

    /**
     * Create a new <code>JoalAudioRenderer</code>. Does nothing. Use
     * initialize() to load the OpenAL device.
     */
    public JoalAudioRenderer(){
    }

    /**
     * Loads the default OpenAL audio out device.
     * Prints information about the openal library to the logger.
     * Checks support for EFX.
     * Allocates 16 channel source ids for instanced playback.
     */
    @Override
    public void initialize() {
        alc = ALFactory.getALC();
        device = alc.alcOpenDevice(null);
        context = alc.alcCreateContext(device, null);

        alc.alcMakeContextCurrent(context);

        al = ALFactory.getAL();
        al.alGetError();

        logger.finer("Audio Vendor: "+al.alGetString(AL.AL_VENDOR));
        logger.finer("Audio Renderer: "+al.alGetString(AL.AL_RENDERER));
        logger.finer("Audio Version: "+al.alGetString(AL.AL_VERSION));

        // Create channel sources
        ib.rewind();
        al.alGenSources(ib.remaining(), ib);
        ib.clear();
        ib.get(channels);
        ib.rewind();

        supportEfx = alc.alcIsExtensionPresent(device, "ALC_EXT_EFX");
        logger.finer("Audio effect extension supported: "+supportEfx);

        // Allocate effect & effect slot for reverb environmental effects
        if (supportEfx){
            ib.position(0).limit(1);
            alc.alcGetIntegerv(device,  AL.ALC_MAX_AUXILIARY_SENDS, 1, ib);
            auxSends = ib.get(0);
            System.out.println("Max auxilary sends: "+auxSends);

            // create slot
            ib.position(0).limit(1);
            al.alGenAuxiliaryEffectSlots(1, ib);
            reverbFxSlot = ib.get(0);

            // create effect
            ib.position(0).limit(1);
            al.alGenEffects(1, ib);
            reverbFx = ib.get(0);
            al.alEffecti(reverbFx, AL.AL_EFFECT_TYPE, AL.AL_EFFECT_REVERB);
        }
    }

    /**
     * Destroys the 16 channel sources that were allocated.
     * Destroys the context and close the device.
     */
    @Override
    public void cleanup(){
        // delete channel-based sources
        ib.rewind();
        ib.put(channels);
        ib.flip();
        al.alDeleteSources(ib.remaining(), ib);

        if (supportEfx){
            ib.position(0).limit(1);
            ib.put(0, reverbFx);
            al.alDeleteEffects(1, ib);

            ib.position(0).limit(1);
            ib.put(0, reverbFxSlot);
            al.alDeleteAuxiliaryEffectSlots(1, ib);
        }

        alc.alcMakeContextCurrent(null);
        alc.alcDestroyContext(context);
        alc.alcCloseDevice(device);

        alc = null;
        al = null;
        device = null;
        context = null;
    }

    /**
     * Sets listener parameters.
     * @param pos The position of the listener
     * @param vel Velocity vector
     * @param dir Direction (look at)
     * @param up Up vector, usually this is 0, 1, 0
     */
    private void setListenerParams(Vector3f pos, Vector3f vel, Vector3f dir, Vector3f up){
        al.alListener3f(AL.AL_POSITION, pos.x, pos.y, pos.z);
        al.alListener3f(AL.AL_VELOCITY, vel.x, vel.y, vel.z);
        fb.rewind();
        fb.put(dir.x).put(dir.y).put(dir.z);
        fb.put(up.x).put(up.y).put(up.z);
        fb.flip();
        al.alListenerfv(AL.AL_ORIENTATION, fb);
    }

    public void setEnvironment(Environment env){
        al.alEffectf(reverbFx, AL.AL_REVERB_DENSITY,             env.getDensity());
        al.alEffectf(reverbFx, AL.AL_REVERB_DIFFUSION,           env.getDiffusion());
        al.alEffectf(reverbFx, AL.AL_REVERB_GAIN,                env.getGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_GAINHF,              env.getGainHf());
        al.alEffectf(reverbFx, AL.AL_REVERB_DECAY_TIME,          env.getDecayTime());
        al.alEffectf(reverbFx, AL.AL_REVERB_DECAY_HFRATIO,       env.getDecayHFRatio());
        al.alEffectf(reverbFx, AL.AL_REVERB_REFLECTIONS_GAIN,    env.getReflectGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_REFLECTIONS_DELAY,   env.getReflectDelay());
        al.alEffectf(reverbFx, AL.AL_REVERB_LATE_REVERB_GAIN,    env.getLateReverbGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_LATE_REVERB_DELAY,   env.getLateReverbDelay());
        al.alEffectf(reverbFx, AL.AL_REVERB_AIR_ABSORPTION_GAINHF, env.getAirAbsorbGainHf());
        al.alEffectf(reverbFx, AL.AL_REVERB_ROOM_ROLLOFF_FACTOR, env.getRoomRolloffFactor());

        // attach effect to slot
        al.alAuxiliaryEffectSloti(reverbFxSlot, AL.AL_EFFECTSLOT_EFFECT, reverbFx);
    }

    private void setSourceParams(int id, AudioSource src, boolean forceNonLoop){
        if (src.isPositional()){
            Vector3f pos = src.getPosition();
            Vector3f vel = src.getVelocity();
            al.alSource3f(id, AL.AL_POSITION, pos.x, pos.y, pos.z);
            al.alSource3f(id, AL.AL_VELOCITY, vel.x, vel.y, vel.z);
            al.alSourcef(id, AL.AL_MAX_DISTANCE, src.getMaxDistance());
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

        if (src.isReverbEnabled()){
            al.alSource3i(id, AL.AL_AUXILIARY_SEND_FILTER, reverbFxSlot, 0, AL.AL_FILTER_NULL);
        }

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

    private boolean fillBuffer(AudioStream as, int id){
        int size = 0;
        int result;

        while (size < arrayBuf.length){
            if (!as.isOpen())
                break;
            
            result = as.readSamples(arrayBuf, size, arrayBuf.length - size);

            if(result > 0){
                size += result;
            }else{
                try{
                    as.close();
                }catch (IOException ex){
                }
                break;
            }

        }

        if (size == 0){
            // nullify buffer
            nativeBuf.position(0).limit(0);
            al.alBufferData(id, convertFormat(as), nativeBuf, 0, 0);
            System.out.println("Nullify");
            return false;
        }

        nativeBuf.position(0).limit(nativeBuf.capacity());
        nativeBuf.put(arrayBuf, 0, size);
        nativeBuf.position(0).limit(size);

        al.alBufferData(id, convertFormat(as), nativeBuf, size, as.getSampleRate());
        System.out.println("Filled buffer "+id+" with "+size+" bytes");

        return true;
    }

    private boolean updateStreamingSource(AudioSource src, AudioStream as){
        boolean active = true;

        int id = src.getId();
        assert id >= 0;

        ib.position(0).limit(1);
        al.alGetSourcei(id, AL.AL_BUFFERS_PROCESSED, ib);
        int processed = ib.get(0);

        while((processed--) != 0){
            int buffer;

            ib.position(0).limit(1);
            al.alSourceUnqueueBuffers(id, 1, ib);
            buffer = ib.get(0);

            active = fillBuffer(as, buffer);

            ib.position(0).limit(1);
            ib.put(0, buffer);
            al.alSourceQueueBuffers(id, 1, ib);
        }

        return active;
    }

    @Override
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

            // update streaming
            if (src.getAudioData() instanceof AudioStream){
                AudioStream as = (AudioStream) src.getAudioData();
                if (as.isOpen()){
                    updateStreamingSource(src, as);
                    if (src.getStatus() == Status.Stopped){
                        // source was stopped due to starvation
                        // resume playing
                        al.alSourcePlay(src.getId());
                        src.setStatus(Status.Playing);
                        toRem.remove(src);
                    }
                }
            }
        }
        playingList.removeAll(toRem);

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
            updateAudioData(src, src.getAudioData());
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

        int id = src.getId();
        if (id == -1){
            ib.position(0).limit(1);
            al.alGenSources(ib.remaining(), ib);
            id = ib.get(0);
            src.setId(id);
        }
        
        setSourceParams(id, src, false);

        // update audio data first before continuing
        if (audioData.isUpdateNeeded())
            updateAudioData(src, audioData);

        
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

    public void updateAudioBuffer(AudioSource src, AudioBuffer ab){
        int id = ab.getId();
        boolean needAttach = false;
        if (ab.getId() == -1){
            ib.position(0).limit(1);
            al.alGenBuffers(ib.remaining(), ib);
            id = ib.get(0);
            ab.setId(id);
            needAttach = true;
        }

        al.alBufferData(id, convertFormat(ab), ab.getData(), ab.getData().remaining(), ab.getSampleRate());
        if (needAttach)
            al.alSourcei(src.getId(), AL.AL_BUFFER, id);

        ab.clearUpdateNeeded();
    }

    public void updateAudioStream(AudioSource src, AudioStream as){
        ib.position(0).limit(STREAMING_BUFFER_COUNT);
        al.alGenBuffers(STREAMING_BUFFER_COUNT, ib);

        for (int i = 0; i < STREAMING_BUFFER_COUNT; i++){
            int id = ib.get(i);
            fillBuffer(as, id);

            ib.position(i).limit(i+1);
            al.alSourceQueueBuffers(src.getId(), 1, ib);
            ib.position(0).limit(STREAMING_BUFFER_COUNT);
        }

        as.clearUpdateNeeded();
    }

    public void updateAudioData(AudioSource src, AudioData ad){
        if (ad instanceof AudioBuffer){
            updateAudioBuffer(src, (AudioBuffer) ad);
        }else if (ad instanceof AudioStream){
            updateAudioStream(src, (AudioStream) ad);
        }
    }

    @Override
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
        }else if (ad instanceof AudioStream){
            AudioStream as = (AudioStream) ad;
            // TODO: Delete stream
        }
    }

}
