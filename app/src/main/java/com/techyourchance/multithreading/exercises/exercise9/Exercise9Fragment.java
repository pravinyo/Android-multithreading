package com.techyourchance.multithreading.exercises.exercise9;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.techyourchance.multithreading.DefaultConfiguration;
import com.techyourchance.multithreading.R;
import com.techyourchance.multithreading.common.BaseFragment;

import java.math.BigInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Exercise9Fragment extends BaseFragment {

    public static Fragment newInstance() {
        return new Exercise9Fragment();
    }

    private static final int MAX_TIMEOUT_MS = DefaultConfiguration.DEFAULT_FACTORIAL_TIMEOUT_MS;

    private EditText mEdtArgument;
    private EditText mEdtTimeout;
    private Button mBtnStartWork;
    private TextView mTxtResult;

    private ComputeFactorialUseCase mComputeFactorialUseCase;
    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComputeFactorialUseCase = new ComputeFactorialUseCase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_9, container, false);

        mEdtArgument = view.findViewById(R.id.edt_argument);
        mEdtTimeout = view.findViewById(R.id.edt_timeout);
        mBtnStartWork = view.findViewById(R.id.btn_compute);
        mTxtResult = view.findViewById(R.id.txt_result);

        mBtnStartWork.setOnClickListener(v -> {
            if (mEdtArgument.getText().toString().isEmpty()) {
                return;
            }

            mTxtResult.setText("");
            mBtnStartWork.setEnabled(false);


            InputMethodManager imm =
                    (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mBtnStartWork.getWindowToken(), 0);

            int argument = Integer.parseInt(mEdtArgument.getText().toString());

            mDisposable = mComputeFactorialUseCase.computeFactorialAndNotify(argument, getTimeout())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        switch (result.intValue()) {
                            case -1:
                                onFactorialComputationAborted();
                                break;
                            case -2:
                                onFactorialComputationTimedOut();
                                break;
                            default:
                                onFactorialComputed(result);
                                break;
                        }
                    });
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null){
            mDisposable.dispose();
        }
    }

    @Override
    protected String getScreenTitle() {
        return "Exercise 9";
    }

    private int getTimeout() {
        int timeout;
        if (mEdtTimeout.getText().toString().isEmpty()) {
            timeout = MAX_TIMEOUT_MS;
        } else {
            timeout = Integer.parseInt(mEdtTimeout.getText().toString());
            if (timeout > MAX_TIMEOUT_MS) {
                timeout = MAX_TIMEOUT_MS;
            }
        }
        return timeout;
    }

    @SuppressLint("SetTextI18n")
    public void onFactorialComputed(BigInteger result) {
        mTxtResult.setText(result.toString());
        mBtnStartWork.setEnabled(true);
    }

    public void onFactorialComputationTimedOut() {
        mTxtResult.setText(R.string.timeout);
        mBtnStartWork.setEnabled(true);
    }

    public void onFactorialComputationAborted() {
        mTxtResult.setText(R.string.abort);
        mBtnStartWork.setEnabled(true);
    }
}
