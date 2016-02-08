package com.spotxchange.sdk.brightcoveplugin;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import com.brightcove.player.event.AbstractComponent;
import com.brightcove.player.event.Component;
import com.brightcove.player.event.Emits;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.event.ListensFor;

import com.spotxchange.sdk.android.SpotxAdListener;
import com.spotxchange.sdk.android.SpotxAdSettings;
import com.spotxchange.sdk.android.SpotxAdView;

import java.util.HashMap;
import java.util.Map;

@Emits(events = {
        EventType.WILL_INTERRUPT_CONTENT,
        EventType.WILL_RESUME_CONTENT
})
@ListensFor(events = {
        EventType.CUE_POINT,
        EventType.DID_STOP
})

/**
 *
 */
public class SpotxBrightcovePlugin extends AbstractComponent implements Component {
    public static final String TAG = SpotxBrightcovePlugin.class.getSimpleName();

    private Context _ctx;
    private SpotxAdView _adView;
    private SpotxAdSettings _adSettings;
    private ViewGroup _viewGroup;
    private Event _origEvent;

    private LayoutParams _lparams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

    /**
     * SpotxBrightcovePlugin Constructor
     *
     * @param emitter
     * @param ctx
     * @param vg
     * @param adSettings
     *
     * @throws IllegalArgumentException
     */
    public SpotxBrightcovePlugin(EventEmitter emitter, Context ctx, ViewGroup vg, SpotxAdSettings adSettings) {
        super(emitter, SpotxBrightcovePlugin.class);

        if(adSettings == null){
            throw new IllegalArgumentException("SpotxAdSettings cannot be null!");
        }

        _viewGroup = vg;
        _ctx = ctx;
        _adSettings = adSettings;
        addListener(EventType.CUE_POINT, new OnCuePointListener());
        addListener(EventType.DID_STOP, new OnStopListener());
    }

    /**
     * Unsupported SpotxBrightcovePlugin Constructor
     *
     * @param emitter
     * @param ctx
     * @param vg
     */
    public SpotxBrightcovePlugin(EventEmitter emitter, Context ctx, ViewGroup vg) {
        super(emitter, SpotxBrightcovePlugin.class);
        throw new UnsupportedOperationException("This SpotxBrightcovePlugin constructor is not supported. Please consult documentation.");
    }

    /**
     * Removes the SpotxAdView from the view group
     */
    public void remove(){
        if(_adView != null) {
            _adView.setVisibility(View.GONE);
            _adView.unsetAdListener();
            _viewGroup.removeView(_adView);
            _adView = null;
        }
    }

    /**
     * Listens for the user set cues to play an ad
     */
    private class OnCuePointListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            Log.d(TAG, "OnCuePointListener: " + event.properties);

            // save original event
            _origEvent = (Event) event.properties.get(Event.ORIGINAL_EVENT);
            eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);

            // do the ad view
            Log.d(TAG, "Creating SpotxAdView...");
            if(_adView != null){
                remove();
            }

            _adView = new SpotxAdView(_ctx, _adSettings);
            _adView.setVisibility(View.INVISIBLE);
            _adView.setAdListener(_spotxAdListener);
            _viewGroup.addView(_adView, _lparams);
            _adView.init();
        }
    }

    private class OnStopListener implements EventListener {

        @Override
        public void processEvent(Event event) {
            Log.d(TAG, "OnStopListener: " + event.properties);
            remove();
        }
    }

    /**
     * Cleans up the ad view and fires the original events and resumes the video content
     */
    private void resumeContent() {
        // cleanup ad view
        remove();

        // fire resumeContent content event
        if (_origEvent == null) {
            _origEvent = new Event(EventType.PLAY);
            _origEvent.properties.put(Event.SKIP_CUE_POINTS, true);
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(Event.ORIGINAL_EVENT, _origEvent);
        eventEmitter.emit(EventType.WILL_RESUME_CONTENT, properties);
    }

    private final SpotxAdListener _spotxAdListener = new SpotxAdListener() {
        @Override
        public void adLoaded() {
            Log.d(TAG, "Ad Loaded!");
            _adView.setVisibility(View.VISIBLE);
        }

        @Override
        public void adStarted() {
            Log.d(TAG, "Ad Started!");
        }

        @Override
        public void adCompleted() {
            Log.d(TAG, "Ad Completed!");
            resumeContent();
        }

        @Override
        public void adError() {
            Log.d(TAG, "Ad Error!");
            resumeContent();
        }

        @Override
        public void adExpired() {
            Log.d(TAG, "Ad Expired!");
            resumeContent();
        }

        @Override
        public void adClicked() {
            Log.d(TAG, "Ad Clicked!");
            // TODO?
        }
    };

}