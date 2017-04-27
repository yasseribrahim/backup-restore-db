package com.example.interactive.backupandrestoredb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.internal.schedulers.ExecutorScheduler;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FreqActivity extends AppCompatActivity {
    private TextView output;
    private int counter = 1;
    Observable<Long> observable;
    CompositeSubscription subscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freq);

        output = (TextView) findViewById(R.id.output);
        Subscriber subscriber = new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                output.append("complete.\n");
                Log.i("FreqActivity", "Complete");
            }

            @Override
            public void onError(Throwable e) {
                output.append(e.getMessage() + "\n");
                Log.i("FreqActivity", e.getMessage());
            }

            @Override
            public void onNext(Long integer) {
                output.append(integer + "\n");
                Log.i("FreqActivity", integer + "");
            }
        };
        observable = Observable.interval(0, 1, TimeUnit.SECONDS);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                    }
                }).doAfterTerminate(new Action0() {
            @Override
            public void call() {
                Log.i("FreqActivity", "doAfterTerminate");
            }
        }).doOnTerminate(new Action0() {
            @Override
            public void call() {
                Log.i("FreqActivity", "doOnTerminate");
            }
        }).doOnEach(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                Log.i("FreqActivity", "Complete");
            }

            @Override
            public void onError(Throwable e) {
                Log.i("FreqActivity", "Error");
            }

            @Override
            public void onNext(Long aLong) {
                Log.i("FreqActivity", "next");
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Log.i("FreqActivity", "doOnUnsubscribe");
            }
        })
                .subscribe(subscriber);

        subscription.add(subscriber);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
