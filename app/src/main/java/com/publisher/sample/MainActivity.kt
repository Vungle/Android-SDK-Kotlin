package com.publisher.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView

import com.vungle.warren.AdConfig
import com.vungle.warren.InitCallback
import com.vungle.warren.LoadAdCallback
import com.vungle.warren.PlayAdCallback
import com.vungle.warren.Vungle
import com.vungle.warren.VungleNativeAd
import com.vungle.warren.error.VungleException
//import com.vungle.warren.Vungle.Consent

import java.util.Arrays

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var initButton: Button
    private lateinit var closeFlexFeedButton: Button
    private lateinit var pauseFlexFeedButton: Button
    private lateinit var resumeFlexFeedButton: Button

    private val loadButtons = arrayOfNulls<Button>(3)
    private val playButtons = arrayOfNulls<Button>(3)

    // Get your Vungle App ID and Placement ID information from Vungle Dashboard
    private val TAG = "VungleSampleApp"

    private val appId = "5ae0db55e2d43668c97bd65e"
    private val autocachePlacementReferenceID = "DEFAULT-6595425"
    private val placementsList = Arrays.asList(autocachePlacementReferenceID, "DYNAMIC_TEMPLATE_INTERSTITIAL-6969365", "FLEX_FEED-2416159")

    private var flexFeedContainer: RelativeLayout? = null
    private var vungleNativeAd: VungleNativeAd? = null
    private var nativeAdView: View? = null

    private val adConfig = AdConfig()

    private val vunglePlayAdCallback = object : PlayAdCallback {
        override fun onAdStart(placementReferenceID: String) {
            Log.d(TAG, "PlayAdCallback - onAdStart" +
                    "\n\tPlacement Reference ID = " + placementReferenceID)

            val index = placementsList.indexOf(placementReferenceID)

            if (placementReferenceID !== autocachePlacementReferenceID) {
                setButtonState(loadButtons[index], true)
            }

            setButtonState(playButtons[index], false)
        }

        override fun onAdEnd(placementReferenceID: String, completed: Boolean, isCTAClicked: Boolean) {
            Log.d(TAG, "PlayAdCallback - onAdEnd" +
                    "\n\tPlacement Reference ID = " + placementReferenceID +
                    "\n\tView Completed = " + completed + "" +
                    "\n\tDownload Clicked = " + isCTAClicked)
        }

        override fun onError(placementReferenceID: String, throwable: Throwable) {
            Log.d(TAG, "PlayAdCallback - onError" +
                    "\n\tPlacement Reference ID = " + placementReferenceID +
                    "\n\tError = " + throwable.localizedMessage)

            checkInitStatus(throwable)
        }
    }

    private val vungleLoadAdCallback = object : LoadAdCallback {
        override fun onAdLoad(placementReferenceID: String) {
            Log.d(TAG, "LoadAdCallback - onAdLoad" +
                    "\n\tPlacement Reference ID = " + placementReferenceID)

            setButtonState(playButtons[placementsList.indexOf(placementReferenceID)], true)
            setButtonState(loadButtons[placementsList.indexOf(placementReferenceID)], false)

        }

        override fun onError(placementReferenceID: String, throwable: Throwable) {
            Log.d(TAG, "LoadAdCallback - onError" +
                    "\n\tPlacement Reference ID = " + placementReferenceID +
                    "\n\tError = " + throwable.localizedMessage)

            checkInitStatus(throwable)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUiElements()
    }

    private fun initSDK() {
        Vungle.init(appId, applicationContext, object : InitCallback {
            override fun onSuccess() {
                Log.d(TAG, "InitCallback - onSuccess")

                //                // Usage example of GDPR API
                //                // To set the user's consent status to opted in:
                //                Vungle.updateConsentStatus(Vungle.Consent.OPTED_IN, “1.0.0”);
                //
                //                // To set the user's consent status to opted out:
                //                Vungle.updateConsentStatus(Vungle.Consent.OPTED_OUT, “1.0.0”);
                //
                //                // To find out what the user's current consent status is:
                //                // This will return null if the GDPR Consent status has not been set
                //                // Otherwise, it will return Vungle.Consent.OPTED_IN or Vungle.Consent.OPTED_OUT
                //                Vungle.Consent currentStatus = Vungle.getConsentStatus();
                //                String consentMessageVersion = Vungle.getConsentMessageVersion();

                setButtonState(initButton, false)
                for (i in 0..2) {
                    if (i != 0) {
                        setButtonState(loadButtons[i], !Vungle.canPlayAd(placementsList[i]))
                    }
                    setButtonState(playButtons[i], Vungle.canPlayAd(placementsList[i]))
                }
            }

            override fun onError(throwable: Throwable) {
                Log.d(TAG, "InitCallback - onError: " + throwable.localizedMessage)
            }

            override fun onAutoCacheAdAvailable(placementReferenceID: String) {
                Log.d(TAG, "InitCallback - onAutoCacheAdAvailable" +
                        "\n\tPlacement Reference ID = " + placementReferenceID)
                // SDK will request auto cache placement ad immediately upon initialization
                // This callback is triggered every time the auto-cached placement is available
                // This is the best place to add your own listeners and propagate them to any UI logic bearing class
                setButtonState(playButtons[0], true)
            }
        })
    }


    private fun checkInitStatus(throwable: Throwable) {
        try {
            val ex = throwable as VungleException
            Log.d(TAG, ex.exceptionCode.toString())

            if (ex.exceptionCode == VungleException.VUNGLE_NOT_INTIALIZED) {
                initSDK()
            }
        } catch (cex: ClassCastException) {
            Log.d(TAG, cex.localizedMessage)
        }
    }

    private fun initUiElements() {
        setContentView(R.layout.activity_main)

        val vungleAppId = findViewById<TextView>(R.id.vungle_app_id)
        val vungleAppIdText = "App ID: $appId"
        vungleAppId.text = vungleAppIdText

        initButton = findViewById(R.id.init_button)

        initButton.setOnClickListener {
            initSDK()
            setButtonState(initButton, false)
        }

        val placementIdTexts = arrayOfNulls<TextView>(3)

        placementIdTexts[0] = findViewById(R.id.placement_id1)
        placementIdTexts[1] = findViewById(R.id.placement_id2)
        placementIdTexts[2] = findViewById(R.id.placement_id3)

        loadButtons[0] = findViewById(R.id.placement_load1)
        loadButtons[1] = findViewById(R.id.placement_load2)
        loadButtons[2] = findViewById(R.id.placement_load3)

        playButtons[0] = findViewById(R.id.placement_play1)
        playButtons[1] = findViewById(R.id.placement_play2)
        playButtons[2] = findViewById(R.id.placement_play3)

        flexFeedContainer = findViewById(R.id.flexfeedcontainer)
        closeFlexFeedButton = findViewById(R.id.flexfeed_close)
        pauseFlexFeedButton = findViewById(R.id.flexfeed_invisible)
        resumeFlexFeedButton = findViewById(R.id.flexfeed_visible)

        setButtonState(closeFlexFeedButton, false)
        setButtonState(pauseFlexFeedButton, false)
        setButtonState(resumeFlexFeedButton, false)

        for (i in 0..2) {
            val placementIdText = "Placement ID: " + placementsList[i]
            placementIdTexts[i]?.text = placementIdText
            setButtonState(loadButtons[i], false)
            setButtonState(playButtons[i], false)
        }

        for (i in 0..2) {
            loadButtons[i]?.setOnClickListener {
                if (Vungle.isInitialized() && i != 0) {
                    setButtonState(loadButtons[i], false)
                    Vungle.loadAd(placementsList[i], vungleLoadAdCallback)
                }
            }

            playButtons[i]?.setOnClickListener {
                if (Vungle.isInitialized() && Vungle.canPlayAd(placementsList[i])) {
                    when (i) {
                        0 -> {
                            // Play default placement with ad customization
                            adConfig.setBackButtonImmediatelyEnabled(true)
                            adConfig.setAutoRotate(true)
                            adConfig.setMuted(false)
                            // Optional settings for rewarded ads
                            Vungle.setIncentivizedFields("TestUser", "RewardedTitle", "RewardedBody", "RewardedKeepWatching", "RewardedClose")

                            Vungle.playAd(placementsList[i], adConfig, vunglePlayAdCallback)
                        }

                        1 -> {
                            // Play Dynamic Template ad
                            Vungle.playAd(placementsList[i], null, vunglePlayAdCallback)
                        }

                        2 -> {
                            // Play Flex-Feed ad
                            vungleNativeAd = Vungle.getNativeAd(placementsList[2], vunglePlayAdCallback)
                            nativeAdView = vungleNativeAd!!.renderNativeView()
                            flexFeedContainer!!.addView(nativeAdView)

                            setButtonState(closeFlexFeedButton, true)
                            setButtonState(pauseFlexFeedButton, true)
                            setButtonState(resumeFlexFeedButton, true)
                        }
                    }

                    setButtonState(playButtons[i], false)
                }
            }
        }

        closeFlexFeedButton.setOnClickListener {
            vungleNativeAd!!.finishDisplayingAd()
            flexFeedContainer!!.removeView(nativeAdView)
            vungleNativeAd = null

            setButtonState(closeFlexFeedButton, false)
            setButtonState(pauseFlexFeedButton, false)
            setButtonState(resumeFlexFeedButton, false)
        }

        resumeFlexFeedButton.setOnClickListener {
            vungleNativeAd!!.setAdVisibility(true)
        }

        pauseFlexFeedButton.setOnClickListener {
            vungleNativeAd!!.setAdVisibility(false)
        }
    }

    private fun setButtonState(button: Button?, enabled: Boolean) {
        button?.isEnabled = enabled

        if (enabled) {
            button?.alpha = 1.0f
        } else {
            button?.alpha = 0.5f
        }
    }

    override fun onPause() {
        if (vungleNativeAd != null) {
            vungleNativeAd!!.setAdVisibility(false)
        }

        super.onPause()
    }

    override fun onStop() {
        if (vungleNativeAd != null) {
            vungleNativeAd!!.setAdVisibility(false)
        }

        super.onStop()
    }
}