package com.ninjrok.geoquiz;

/**
 * Created by niranjan on 12/6/16.
 */
public class TrueFalse {

    private String mQuestion;
    private boolean mQuestionAnswer;

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestionId(String question) {
        mQuestion = question;
    }

    public boolean getQuestionAnswer() {
        return mQuestionAnswer;
    }

    public void setQuestionAnswer(boolean questionAnswer) {
        mQuestionAnswer = questionAnswer;
    }

    public TrueFalse(String questionId, boolean questionAnswer) {
        mQuestion = questionId;
        mQuestionAnswer = questionAnswer;
    }

}
