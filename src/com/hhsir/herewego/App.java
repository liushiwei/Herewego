package com.hhsir.herewego;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;

import com.hhsir.herewego.logic.GoGame;
import com.hhsir.herewego.util.Log;

/**
 * the central Application-Context
 */
public class App extends Application {

    private static App instance;
    private static GoGame game;

    public static boolean isTesting = false;

    // the InteractionScope holds things like mode/act game between activities
    private static InteractionScope interaction_scope;

    public static String getVersion() {
        try {
            return instance.getPackageManager().getPackageInfo(instance.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.w(App.class,"cannot determine app version - that's strange but not critical");
            return "vX.Y";
        }
    }


    public static int getVersionCode() {
        try {
            return instance.getPackageManager().getPackageInfo(instance.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.w(App.class,"cannot determine app version - that's strange but not critical");
            return 0;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        interaction_scope = new InteractionScope();
    }

    public static InteractionScope getInteractionScope() {
        return interaction_scope;
    }

    public static GoGame getGame() {
        if (game == null) {
            game = new GoGame((byte) 9);
        }
        return game;
    }

    public static void setGame(GoGame p_game) {
        getInteractionScope().ask_variant_session = true;

        if (game == null) {
            game = p_game;
        } else { // keep listeners and stuff
            game.setGame(p_game);
        }
    }

    
}
