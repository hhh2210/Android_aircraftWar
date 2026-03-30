package edu.hitsz.application;

import android.content.Context;
import android.os.Handler;

import edu.hitsz.application.gamemode.NormalMode;

public class NormalGame extends BaseGame {

    public NormalGame(Context context, Handler activityHandler, int gameOverMessageWhat) {
        super(context, new NormalMode(), activityHandler, gameOverMessageWhat);
    }
}
