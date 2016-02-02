/*
 *
 */
package com.spotxchange.sdk.brightcoveplugin;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

public class SpotxBrightcovePlugin extends AbstractComponent implements Component {
    public static final String TAG = SpotxBrightcovePlugin.class.getSimpleName();

    private Context _ctx;
    private SpotxAdView _adView;
    private SpotxAdSettings _adSettings;
    private ViewGroup _viewGroup;
    private Event _origEvent;

    private final SpotxAdListener _spotxAdListener = new SpotxAdListener() {
        @Override
        public void adLoaded() {

        }

        @Override
        public void adStarted() {

        }

        @Override
        public void adCompleted() {
            resume();
        }

        @Override
        public void adError() {
            resume();
        }

        @Override
        public void adExpired() {
            resume();
        }

        @Override
        public void adClicked() {

        }

    };

    public SpotxBrightcovePlugin(EventEmitter emitter, Context ctx, ViewGroup vg) {
        super(emitter, SpotxBrightcovePlugin.class);
        _viewGroup = vg;
        _ctx = ctx;
    }

    public void init(SpotxAdSettings adSettings){
        _adSettings = adSettings;
        addListener(EventType.CUE_POINT, new OnCuePointListener());
    }

    // CUE_POINT happens after one or more cue points has been
    // reached.
    private class OnCuePointListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            Log.v(TAG, "OnCuePointListener: " + event.properties);

            if(_adSettings != null){
                // Store the original event, so it can be emitted again
                // upon resume.
                _origEvent = (Event) event.properties.get(Event.ORIGINAL_EVENT);

                eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);

                _adView = new SpotxAdView(_ctx, _adSettings);
                _adView.setVisibility(View.INVISIBLE);
                _adView.setAdListener(_spotxAdListener);
                _viewGroup.addView(_adView, new RelativeLayout.LayoutParams(_viewGroup.getLayoutParams().width, _viewGroup.getLayoutParams().height));
                _adView.init();
            }
            else{
                // TODO
                // Warn that adsettings are not set
            }
        }
    }

    private void resume() {
        _viewGroup.removeView(_adView);
        _adView = null;

        if (_origEvent == null) {
            _origEvent = new Event(EventType.PLAY);
            _origEvent.properties.put(Event.SKIP_CUE_POINTS, true);
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(Event.ORIGINAL_EVENT, _origEvent);
        eventEmitter.emit(EventType.WILL_RESUME_CONTENT, properties);
    }

}