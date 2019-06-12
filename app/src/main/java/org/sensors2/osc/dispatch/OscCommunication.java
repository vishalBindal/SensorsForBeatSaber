package org.sensors2.osc.dispatch;

import android.os.HandlerThread;


public class OscCommunication extends HandlerThread {
    private OscHandler handler;

    public OscCommunication(String name, int priority) {
        super(name, priority);
    }

    @Override
    public void run() {
        super.run();
        handler = null;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new OscHandler(this.getLooper());
    }

    public OscHandler getOscHandler() {
        return handler;
    }
}
