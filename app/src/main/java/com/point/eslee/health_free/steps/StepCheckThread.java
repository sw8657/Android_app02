package com.point.eslee.health_free.steps;

import android.os.Handler;

/**
 * Created by Administrator on 2017-01-12.
 */

public class StepCheckThread extends Thread {
    Handler handler;
    boolean isRun = true;

    public StepCheckThread(Handler handler){
        this.handler = handler;
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run(){
        //반복적으로 수행할 작업을 한다.
        while(isRun){
            handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄
            try{
                Thread.sleep(10000); //10초씩 쉰다.
            }catch (Exception e) {}
        }
    }

}
