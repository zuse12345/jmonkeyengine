package g3dtools.deploy;

import com.g3d.asset.pack.J3PCreator;
import com.g3d.asset.pack.ProgressListener;
import java.io.File;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public class PackerWorker extends SwingWorker implements ProgressListener {

    private J3PCreator creator;
    private File outFile;
    private ProgressMonitor pm;
    private int progress = 0;

    public PackerWorker(J3PCreator creator, File outFile, ProgressMonitor pm) {
        this.creator = creator;
        this.outFile = outFile;
        this.pm = pm;
    }

    private void checkCanceled(){
        if (pm.isCanceled() && outFile != null){
            creator.cancel();
            outFile = null;
        }
    }

    public void onText(String note) {
        checkCanceled();
        pm.setNote(note);
    }

    public void onProgress(int amount) {
        checkCanceled();
        progress += amount;
        pm.setProgress(progress);
    }

    public void onMaxProgress(int amount) {
        checkCanceled();
        pm.setMaximum(amount);
    }

    public void onError(String text, Throwable ex){
        System.out.println("error: "+text);
        ex.printStackTrace();
        pm.close();
        outFile = null;
    }

    @Override
    protected Object doInBackground() throws Exception {
        creator.setProgressListener(this);
        creator.finish(outFile);
        return outFile;
    }

}
