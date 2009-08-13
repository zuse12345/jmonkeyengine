package com.g3d.audio.lwjgl;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioData;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.AudioSource;
import com.g3d.audio.AudioSource.Status;
import com.g3d.audio.DirectionalAudioSource;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import org.lwjgl.openal.AL11;
import static org.lwjgl.openal.AL10.*;

public class LwjglAudioRenderer implements AudioRenderer {

    private static final Logger logger = Logger.getLogger(LwjglAudioRenderer.class.getName());

    private final IntBuffer ib = BufferUtils.createIntBuffer(16);
    private final FloatBuffer fb = BufferUtils.createVector3Buffer(2);

    private ArrayList<AudioSource> playingList = new ArrayList<AudioSource>();
    private Camera listener;

    private int[] channels = new int[16];
    private AudioSource[] chanSrcs = new AudioSource[16];
    private int nextChan = 0;
    private ArrayList<Integer> freeChans = new ArrayList<Integer>();

    public LwjglAudioRenderer(){
    }

    private void setListenerParams(Vector3f pos, Vector3f vel, Vector3f dir, Vector3f up){
        alListener3f(AL_POSITION, pos.x, pos.y, pos.z);
        alListener3f(AL_VELOCITY, vel.x, vel.y, vel.z);
        fb.rewind();
        fb.put(dir.x).put(dir.y).put(dir.z);
        fb.put(up.x).put(up.y).put(up.z);
        fb.flip();
        alListener(AL_ORIENTATION, fb);
    }

    private void setSourceParams(int id, AudioSource src, boolean forceNonLoop){
        if (src.isPositional()){
            Vector3f pos = src.getPosition();
            Vector3f vel = src.getVelocity();
            alSource3f(id, AL_POSITION, pos.x, pos.y, pos.z);
            alSource3f(id, AL_VELOCITY, vel.x, vel.y, vel.z);
        }else{
            // play in headspace
            alSourcei(id, AL_SOURCE_RELATIVE, AL_TRUE);
            alSource3f(id, AL_POSITION, 0,0,0);
            alSource3f(id, AL_VELOCITY, 0,0,0);
        }

        if (forceNonLoop){
            alSourcei(id,  AL_LOOPING, AL_FALSE);
        }else{
            alSourcei(id,  AL_LOOPING, src.isLooping() ? AL_TRUE : AL_FALSE);
        }
        alSourcef(id,  AL_GAIN, src.getVolume());
        alSourcef(id,  AL_PITCH, src.getPitch());
        alSourcef(id,  AL11.AL_SEC_OFFSET, src.getTimeOffset());

        if (src instanceof DirectionalAudioSource){
            DirectionalAudioSource das = (DirectionalAudioSource) src;
            Vector3f dir = das.getDirection();
            alSource3f(id, AL_DIRECTION, dir.x, dir.y, dir.z);
            alSourcef(id, AL_CONE_INNER_ANGLE, das.getInnerAngle());
            alSourcef(id, AL_CONE_OUTER_ANGLE, das.getOuterAngle());
            alSourcef(id, AL_CONE_OUTER_GAIN,  0);
        }else{
            alSourcef(id, AL_CONE_INNER_ANGLE, 360);
            alSourcef(id, AL_CONE_OUTER_ANGLE, 360);
            alSourcef(id, AL_CONE_OUTER_GAIN, 1f);
        }
    }

    public void initialize(){
        try{
            AL.create();
        }catch (LWJGLException ex){
            logger.log(Level.SEVERE, "Failed to load audio library", ex);
        }

        System.out.println("Vendor: "   + alGetString(AL_VENDOR));
        System.out.println("Renderer: " + alGetString(AL_RENDERER));
        System.out.println("Version: "  + alGetString(AL_VERSION));

        // Create channel sources
        ib.rewind();
        alGenSources(ib);
        ib.clear();
        ib.get(channels);
        ib.rewind();
    }

    public void cleanup(){
        // delete channel-based sources
        ib.rewind();
        ib.put(channels);
        ib.flip();
        alDeleteSources(ib);
        
        // XXX: Delete other buffers/sources
        AL.destroy();
    }

    public void update(float tpf){
        // delete channel-based sources that finished playing
        for (int i = 0; i < channels.length; i++){
            AudioSource src = chanSrcs[i];
            int sourceId = channels[i];
            if (src != null){
                if (alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_STOPPED){
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
            if (alGetSourcei(id, AL_SOURCE_STATE) == AL_STOPPED){
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
        alSourcei(sourceId, AL_BUFFER, src.getAudioData().getId());

        // set parameters, like position and max distance

        // if original source has looping enabled- ignore it
        // If these sources were looped, they will never become AL_STOPPED
        // which means the channel will be hogged forever
        setSourceParams(sourceId, src, true);

        alSourceStop(sourceId);
        alSourceRewind(sourceId);

        // play the channel
        alSourcePlay(sourceId);

        System.out.println("Playing on "+index);
    }

    public void playSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Playing){
            alSourcePlay(src.getId());
            src.setStatus(Status.Playing);
            playingList.add(src);
        }
    }

    public void pauseSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Paused){
            alSourcePause(src.getId());
            src.setStatus(Status.Paused);
            playingList.remove(src);
        }
    }

    public void stopSource(AudioSource src) {
        if (src.isUpdateNeeded())
            updateSource(src);

        if (src.getStatus() != Status.Stopped){
            alSourceStop(src.getId());
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
            ib.rewind();
            alGenSources(ib);
            id = ib.get(0);
            src.setId(id);

            // also need to attach data
            // note that it already has an id
            alSourcei(id, AL_BUFFER, audioData.getId());
        }

        setSourceParams(id, src, false);
        src.clearUpdateNeeded();
    }

    public void deleteSource(AudioSource src) {
        int id = src.getId();
        if (id != -1){
            ib.put(0,id);
            ib.position(0).limit(1);
            alDeleteSources(ib);
            src.resetObject();
        }
    }

    private int convertFormat(AudioData ad){
        switch (ad.getBitsPerSample()){
            case 8:
                if (ad.getChannels() == 1)
                    return AL_FORMAT_MONO8;
                else if (ad.getChannels() == 2)
                    return AL_FORMAT_STEREO8;

                break;
            case 16:
                if (ad.getChannels() == 1)
                    return AL_FORMAT_MONO16;
                else
                    return AL_FORMAT_STEREO16;
        }
        throw new UnsupportedOperationException("Unsupported channels/bits combination: "+
                                                "bits="+ad.getBitsPerSample()+", channels="+ad.getChannels());
    }

    public void updateAudioBuffer(AudioBuffer ab){
        int id = ab.getId();
        if (ab.getId() == -1){
            ib.position(0).limit(1);
            alGenBuffers(ib);
            id = ib.get(0);
            ab.setId(id);
        }

        alBufferData(id, convertFormat(ab), ab.getData(), ab.getSampleRate());
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
                alDeleteBuffers(ib);
                ab.resetObject();
            }
        }
    }

}
