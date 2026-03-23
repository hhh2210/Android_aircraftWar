package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.application.gamemode.NormalMode;

public class NormalGame extends BaseGame {

    public NormalGame(Context context) {
        super(context, new NormalMode());
    }
}
