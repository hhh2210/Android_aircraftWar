package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.application.gamemode.HardMode;

public class HardGame extends BaseGame {

    public HardGame(Context context) {
        super(context, new HardMode());
    }
}
