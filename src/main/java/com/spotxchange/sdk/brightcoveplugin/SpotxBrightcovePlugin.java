package com.spotxchange.sdk.brightcoveplugin;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
        EventType.WILL_RESUME_CONTENT,
})
@ListensFor(events = {
        EventType.CUE_POINT,
})

/*
 *
 */
public class SpotxBrightcovePlugin extends AbstractComponent implements Component {
    public static final String TAG = SpotxBrightcovePlugin.class.getSimpleName();

    private Context _ctx;
    private SpotxAdView _adView;
    private SpotxAdSettings _adSettings;
    private ViewGroup _viewGroup;
    private Event _origEvent;

    /**
     *
     * @param emitter
     * @param ctx
     * @param vg
     */
    public SpotxBrightcovePlugin(EventEmitter emitter, Context ctx, ViewGroup vg) {
        super(emitter, SpotxBrightcovePlugin.class);
        _viewGroup = vg;
        _ctx = ctx;
    }

    public void init(SpotxAdSettings adSettings){
        _adSettings = adSettings;
        addListener(EventType.CUE_POINT, new OnCuePointListener());
    }

    private class OnCuePointListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            Log.d(TAG, "OnCuePointListener: " + event.properties);

            if(_adSettings != null){
                // save original event
                _origEvent = (Event) event.properties.get(Event.ORIGINAL_EVENT);
                eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);

                // create new ad view
                Log.d(TAG, "Creating SpotxAdView...");
                _adView = new SpotxAdView(_ctx, _adSettings);
                _adView.setVisibility(View.INVISIBLE);
                _adView.setAdListener(_spotxAdListener);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
                _viewGroup.addView(_adView, layoutParams);
                _adView.init();
            }
            else{
                throw new IllegalArgumentException("AdSettings cannot be null. Did you forget to call init()?");
            }
        }
    }

    private void resume() {
        // cleanup ad view
        _viewGroup.removeView(_adView);
        _adView = null;

        // fire resume content event
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
            resume();
        }

        @Override
        public void adError() {
            Log.d(TAG, "Ad Error!");
            resume();
        }

        @Override
        public void adExpired() {
            Log.d(TAG, "Ad Expired!");
            resume();
        }

        @Override
        public void adClicked() {
            Log.d(TAG, "Ad Clicked!");
            // TODO
        }

    };

}