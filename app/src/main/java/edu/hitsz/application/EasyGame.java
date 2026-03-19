package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.application.gamemode.EasyMode;

public class EasyGame extends BaseGame {

    public EasyGame(Context context) {
        super(context, new EasyMode());
    }
}
