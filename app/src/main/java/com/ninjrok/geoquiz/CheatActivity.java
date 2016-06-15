package com.ninjrok.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by niranjan on 14/6/16.
 */
public class CheatActivity extends Activity {

    private boolean mAnswer, mAnswerShown;
    private TextView mAnswerTextView;
    private Button mShowAnswerButton;

    public static final String EXTRA_ANSWER = "com.ninjrok.geoquiz.answer";
    public static final String EXTRA_ANSWER_SHOWN = "com.ninjrok.geoquiz.answer_shown";

    private void reportBackToQuizActivity(boolean result) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ANSWER_SHOWN, result);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);

        mAnswer = getIntent().getBooleanExtra(EXTRA_ANSWER, false);

        if (savedInstanceState != null) {
            mAnswerShown = savedInstanceState.getBoolean("ANSWER_SHOWN");
        }

        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);
        mShowAnswerButton = (Button) findViewById(R.id.showAnswerButton);

        if (mAnswerShown) {
            if (mAnswer) {
                mAnswerTextView.setText(R.string.true_button);
            }
            else {
                mAnswerTextView.setText(R.string.false_button);
            }
            reportBackToQuizActivity(true);
        }
        else {
            reportBackToQuizActivity(false);
        }

        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswerShown = true;
                if (mAnswer) {
                    mAnswerTextView.setText(R.string.true_button);
                }
                else {
                    mAnswerTextView.setText(R.string.false_button);
                }
                reportBackToQuizActivity(true);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("ANSWER_SHOWN", mAnswerShown);
    }
}
