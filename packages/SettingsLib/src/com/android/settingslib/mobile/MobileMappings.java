/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settingslib.mobile;

import android.content.Context;
import android.content.res.Resources;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.Annotation;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;

import com.android.settingslib.R;
import com.android.settingslib.SignalIcon.MobileIconGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the utility functions to create the RAT to MobileIconGroup mappings.
 */
public class MobileMappings {

    /**
     * Generates the RAT key from the TelephonyDisplayInfo.
     */
    public static String getIconKey(TelephonyDisplayInfo telephonyDisplayInfo) {
        if (telephonyDisplayInfo.getOverrideNetworkType()
                == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE) {
            return toIconKey(telephonyDisplayInfo.getNetworkType());
        } else {
            return toDisplayIconKey(telephonyDisplayInfo.getOverrideNetworkType());
        }
    }

    /**
     * Converts the networkType into the RAT key.
     */
    public static String toIconKey(@Annotation.NetworkType int networkType) {
        return Integer.toString(networkType);
    }

    /**
     * Converts the displayNetworkType into the RAT key.
     */
    public static String toDisplayIconKey(@Annotation.OverrideNetworkType int displayNetworkType) {
        switch (displayNetworkType) {
            case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA:
                return toIconKey(TelephonyManager.NETWORK_TYPE_LTE) + "_CA";
            case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
                return toIconKey(TelephonyManager.NETWORK_TYPE_LTE) + "_CA_Plus";
            case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
                return toIconKey(TelephonyManager.NETWORK_TYPE_NR);
            case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED:
                return toIconKey(TelephonyManager.NETWORK_TYPE_NR) + "_Plus";
            default:
                return "unsupported";
        }
    }

    /**
     * Produce the default MobileIconGroup.
     */
    public static MobileIconGroup getDefaultIcons(Config config) {
        if (!config.showAtLeast3G) {
            return TelephonyIcons.G;
        } else {
            return TelephonyIcons.THREE_G;
        }
    }

    /**
     * Produce a mapping of data network types to icon groups for simple and quick use in
     * updateTelephony.
     */
    public static Map<String, MobileIconGroup> mapIconSets(Config config) {
        final Map<String, MobileIconGroup> networkToIconLookup = new HashMap<>();

        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EVDO_0),
                TelephonyIcons.THREE_G);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EVDO_A),
                TelephonyIcons.THREE_G);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EVDO_B),
                TelephonyIcons.THREE_G);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EHRPD),
                TelephonyIcons.THREE_G);
        if (config.show4gFor3g) {
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_UMTS),
                    TelephonyIcons.FOUR_G);
        } else {
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_UMTS),
                    TelephonyIcons.THREE_G);
        }
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_TD_SCDMA),
                TelephonyIcons.THREE_G);

        if (!config.showAtLeast3G) {
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_UNKNOWN),
                    TelephonyIcons.UNKNOWN);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EDGE),
                    TelephonyIcons.E);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_CDMA),
                    TelephonyIcons.ONE_X);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_1xRTT),
                    TelephonyIcons.ONE_X);
        } else {
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_UNKNOWN),
                    TelephonyIcons.THREE_G);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_EDGE),
                    TelephonyIcons.THREE_G);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_CDMA),
                    TelephonyIcons.THREE_G);
            networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_1xRTT),
                    TelephonyIcons.THREE_G);
        }

        MobileIconGroup hGroup = TelephonyIcons.THREE_G;
        MobileIconGroup hPlusGroup = TelephonyIcons.THREE_G;
        if (config.show4gFor3g) {
            hGroup = TelephonyIcons.FOUR_G;
            hPlusGroup = TelephonyIcons.FOUR_G;
        } else if (config.hspaDataDistinguishable) {
            hGroup = TelephonyIcons.H;
            hPlusGroup = TelephonyIcons.H_PLUS;
        }

        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_HSDPA), hGroup);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_HSUPA), hGroup);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_HSPA), hGroup);
        networkToIconLookup.put(toIconKey(TelephonyManager.NETWORK_TYPE_HSPAP), hPlusGroup);

        if (config.show4gForLte) {
            networkToIconLookup.put(toIconKey(
                    TelephonyManager.NETWORK_TYPE_LTE),
                    TelephonyIcons.FOUR_G);
            if (config.hideLtePlus) {
                networkToIconLookup.put(toDisplayIconKey(
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA),
                        TelephonyIcons.FOUR_G);
            } else {
                networkToIconLookup.put(toDisplayIconKey(
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA),
                        TelephonyIcons.FOUR_G_PLUS);
            }
        } else {
            networkToIconLookup.put(toIconKey(
                    TelephonyManager.NETWORK_TYPE_LTE),
                    TelephonyIcons.LTE);
            if (config.hideLtePlus) {
                networkToIconLookup.put(toDisplayIconKey(
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA),
                        TelephonyIcons.LTE);
            } else {
                networkToIconLookup.put(toDisplayIconKey(
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA),
                        TelephonyIcons.LTE_PLUS);
            }
        }
        networkToIconLookup.put(toIconKey(
                TelephonyManager.NETWORK_TYPE_IWLAN),
                TelephonyIcons.WFC);
        networkToIconLookup.put(toDisplayIconKey(
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO),
                TelephonyIcons.LTE_CA_5G_E);
        networkToIconLookup.put(toDisplayIconKey(
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA),
                TelephonyIcons.NR_5G);
        networkToIconLookup.put(toDisplayIconKey(
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED),
                TelephonyIcons.NR_5G_PLUS);
        networkToIconLookup.put(toIconKey(
                TelephonyManager.NETWORK_TYPE_NR),
                TelephonyIcons.NR_5G);
        return networkToIconLookup;
    }

    /**
     * Wrapper class of system configs and Carrier configs.
     */
    public static class Config {
        public boolean showAtLeast3G = false;
        public boolean show4gFor3g = false;
        public boolean alwaysShowCdmaRssi = false;
        public boolean show4gForLte = false;
        public boolean hideLtePlus = false;
        public boolean hspaDataDistinguishable;
        public boolean alwaysShowDataRatIcon = false;

        public boolean showRsrpSignalLevelforLTE = false;
        public boolean hideNoInternetState = false;
        public boolean showVolteIcon = false;
        public boolean alwaysShowNetworkTypeIcon = false;
        public boolean enableRatIconEnhancement = false;
        public boolean showVowifiIcon = false;
        public boolean enableDdsRatIconEnhancement = false;

        /**
         * Reads the latest configs.
         */
        public static Config readConfig(Context context) {
            Config config = new Config();
            Resources res = context.getResources();

            config.showAtLeast3G = res.getBoolean(R.bool.config_showMin3G);
            config.alwaysShowCdmaRssi =
                    res.getBoolean(com.android.internal.R.bool.config_alwaysUseCdmaRssi);
            config.hspaDataDistinguishable =
                    res.getBoolean(R.bool.config_hspa_data_distinguishable);

            CarrierConfigManager configMgr = (CarrierConfigManager)
                    context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            // Handle specific carrier config values for the default data SIM
            int defaultDataSubId = SubscriptionManager.from(context)
                    .getDefaultDataSubscriptionId();
            PersistableBundle b = configMgr.getConfigForSubId(defaultDataSubId);
            if (b != null) {
                config.alwaysShowDataRatIcon = b.getBoolean(
                        CarrierConfigManager.KEY_ALWAYS_SHOW_DATA_RAT_ICON_BOOL);
                config.show4gForLte = b.getBoolean(
                        CarrierConfigManager.KEY_SHOW_4G_FOR_LTE_DATA_ICON_BOOL);
                config.show4gFor3g = b.getBoolean(
                        CarrierConfigManager.KEY_SHOW_4G_FOR_3G_DATA_ICON_BOOL);
                config.hideLtePlus = b.getBoolean(
                        CarrierConfigManager.KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL);
            }

            config.alwaysShowNetworkTypeIcon = res.getBoolean(R.bool.config_alwaysShowTypeIcon);
            config.showRsrpSignalLevelforLTE =
                    res.getBoolean(R.bool.config_showRsrpSignalLevelforLTE);
            config.hideNoInternetState = res.getBoolean(R.bool.config_hideNoInternetState);
            config.showVolteIcon = true;
            config.showVowifiIcon = true;
            if ( config.alwaysShowNetworkTypeIcon ) {
                config.hideLtePlus = false;
            }

            config.enableRatIconEnhancement =
                    SystemProperties.getBoolean("persist.sysui.rat_icon_enhancement", false);
            config.enableDdsRatIconEnhancement =
                    SystemProperties.getBoolean("persist.sysui.dds_rat_icon_enhancement", false);

            return config;
        }
    }
}
