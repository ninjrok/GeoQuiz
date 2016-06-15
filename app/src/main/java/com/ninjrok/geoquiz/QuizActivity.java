package com.ninjrok.geoquiz;

/**
 * Created by niranjan on 12/6/16.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class QuizActivity extends AppCompatActivity {

    private Button mTrueButton, mFalseButton, mNextButton, mPrevButton, mCheatButton;
    private TextView mQuestionTextView;

    private int mCurrentIndex = 0;

    private static final String API_URL = "https://api.myjson.com/bins/2g2ag";
    private static final String DEBUG_TAG = "QuizActivity";
    private static TrueFalse[] mQuestionBank;
    private static boolean[] mCheatedArray;
    private static boolean mIsCheater, mIsLoaded;

    private class PopulateQuestionBank extends AsyncTask<Void, Void, String> {

        ProgressDialog progDialog = new ProgressDialog(QuizActivity.this);

        private void writeTrueFalseData(InputStream inputStream) {
            BufferedReader jsonContent = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                while ((line = jsonContent.readLine()) != null) {
                    sb.append(line+"\n");
                }
                jsonContent.close();

                String data = sb.toString();
                JSONArray jsonArr = new JSONArray(data);
                Log.d(DEBUG_TAG, jsonArr.toString());
                mQuestionBank = new TrueFalse[jsonArr.length()];
                mCheatedArray = new boolean[jsonArr.length()];
                for (int i=0; i < jsonArr.length();i++) {
                    JSONObject jsonObject = jsonArr.getJSONObject(i);
                    mQuestionBank[i] = new TrueFalse(jsonObject.getString("question"),
                            Boolean.valueOf(jsonObject.getString("answer")));
                    mCheatedArray[i] = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url = null;
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            try {
                url = new URL(API_URL);
                Log.d(DEBUG_TAG, "Loading data from "+API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                inputStream = conn.getInputStream();
                writeTrueFalseData(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(DEBUG_TAG, "Inside preExecute");
            progDialog.setMessage("Loading");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(DEBUG_TAG, "Inside PostExecute");
            if (progDialog.isShowing()) {
                progDialog.dismiss();
            }
            updateQuestionTextView();
            mIsLoaded = true;
        }
    }

    private void updateQuestionTextView() {
        String question = mQuestionBank[mCurrentIndex].getQuestion();
        mQuestionTextView.setText(question);
        Log.d(DEBUG_TAG, "Successfully updated question.");
    }

    private void checkAnswer(boolean userInput) {
        boolean answer = mQuestionBank[mCurrentIndex].getQuestionAnswer();
        int toastID = 0;

        if (mIsCheater || mCheatedArray[mCurrentIndex]) {
            toastID = R.string.judgement_toast;
        }
        else {
            if (answer == userInput) {
                toastID = R.string.correct_toast;
            } else {
                toastID = R.string.incorrect_toast;
            }
        }
        Toast.makeText(QuizActivity.this, toastID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(DEBUG_TAG, "Inside onSaveInstanceState");
        // This can be done more easily if mCurrentIndex was declared static
        // That way the value does'nt change for any instance of the activity
        savedInstanceState.putInt("KEY_INDEX", mCurrentIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        mIsCheater = data.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false);
        mCheatedArray[mCurrentIndex] = mIsCheater;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt("KEY_INDEX", 0);
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (! mIsLoaded) {
                Log.d(DEBUG_TAG, "Starting async task.");
                new PopulateQuestionBank().execute();
            }
            else {
                updateQuestionTextView();
            }
        } else {
            Log.d(DEBUG_TAG, "No Network Available");
            mQuestionTextView.setText(R.string.no_network);
        }

        mTrueButton = (Button) findViewById(R.id.true_button);
        mFalseButton = (Button) findViewById(R.id.false_button);
        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        mCheatButton = (Button) findViewById(R.id.cheat_button);

        mTrueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        mFalseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentIndex == 0) {
                    mCurrentIndex = (int) (mQuestionBank.length - 1);
                }
                else {
                    mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                }
                mIsCheater = false;
                updateQuestionTextView();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestionTextView();
            }
        });

        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizActivity.this, CheatActivity.class);
                boolean answer = mQuestionBank[mCurrentIndex].getQuestionAnswer();
                intent.putExtra(CheatActivity.EXTRA_ANSWER, answer);
                startActivityForResult(intent, 0);
            }
        });
    }
}
