package edu.hitsz.application;

import android.content.Context;
import android.os.Handler;

import edu.hitsz.application.gamemode.EasyMode;

public class EasyGame extends BaseGame {

    public EasyGame(Context context, Handler activityHandler, int gameOverMessageWhat) {
        super(context, new EasyMode(), activityHandler, gameOverMessageWhat);
    }
}
