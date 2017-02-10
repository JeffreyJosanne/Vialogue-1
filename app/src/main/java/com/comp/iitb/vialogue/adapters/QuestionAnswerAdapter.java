package com.comp.iitb.vialogue.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.comp.iitb.vialogue.R;

/**
 * Created by shubh on 10-02-2017.
 */

public abstract class QuestionAnswerAdapter extends Dialog {

    private RelativeLayout mQuestionLayout;
    private RelativeLayout mOptionLayout;
    private RelativeLayout mHintLayout;
    private RelativeLayout mSolutionLayout;
    private Button mDone;
    private LayoutInflater mLayoutInflater;

    public QuestionAnswerAdapter(Context context) {
        super(context);
    }

    public QuestionAnswerAdapter(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected QuestionAnswerAdapter(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.question_answer);
        mLayoutInflater = LayoutInflater.from(getContext());
        setCancelable(false);

        mQuestionLayout = (RelativeLayout) findViewById(R.id.question_layer);
        mQuestionLayout.addView(onCreateQuestionView());

        mOptionLayout = (RelativeLayout) findViewById(R.id.option_layer);
        mOptionLayout.addView(onCreateOptionsView());

        mHintLayout = (RelativeLayout) findViewById(R.id.hint_layer);
        mHintLayout.addView(onCreateHintsView());

        mSolutionLayout = (RelativeLayout) findViewById(R.id.solution_layer);
        mSolutionLayout.addView(onCreateSolutionView());

        mDone = (Button) findViewById(R.id.done_button);

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAnswerCorrect()){
                    onAnswerCorrect();
                } else {
                    onAnswerIncorrect();
                }
            }
        });
    }

    public Button getDoneButton(){
        return mDone;
    }

    public LayoutInflater getLayoutInflator(){
        return mLayoutInflater;
    }

    public void showHints(){
        mHintLayout.setVisibility(View.VISIBLE);
    }

    public void hideHints(){
        mHintLayout.setVisibility(View.GONE);
    }

    public void showSolution(){
        mSolutionLayout.setVisibility(View.VISIBLE);
    }

    public void hideSolution(){
        mSolutionLayout.setVisibility(View.GONE);
    }



    protected abstract View onCreateQuestionView();

    protected abstract View onCreateOptionsView();

    protected abstract View onCreateHintsView();

    protected abstract View onCreateSolutionView();

    protected abstract boolean isAnswerCorrect();

    protected abstract void onAnswerIncorrect();

    protected abstract void onAnswerCorrect();



}
