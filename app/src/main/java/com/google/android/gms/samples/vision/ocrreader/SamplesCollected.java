package com.google.android.gms.samples.vision.ocrreader;

/**
 * Created by krohlfing on 10/7/2016.
 */

public class SamplesCollected {
    public interface SamplesCollectedListener {
        public void onSamplesCollected();
    }

    private SamplesCollectedListener listener;

    public SamplesCollected() {
        this.listener = null;
    }

    public void setSamplesCollectedListener(SamplesCollectedListener listener) {
        this.listener = listener;
    }
}
