package edu.hitsz.application;

import android.content.Context;
import android.os.Handler;

import edu.hitsz.application.gamemode.HardMode;

public class HardGame extends BaseGame {

    public HardGame(Context context, Handler activityHandler, int gameOverMessageWhat) {
        super(context, new HardMode(), activityHandler, gameOverMessageWhat);
    }
}
