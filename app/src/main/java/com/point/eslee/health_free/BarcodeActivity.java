package com.point.eslee.health_free;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BarcodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 제거
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 테두리 제거
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 뒷배경 블러 처리
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        layoutParams.flags= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount= 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_barcode);
    }

    public void onClickBarCode(View view) {

        Toast.makeText(this,"바코드",Toast.LENGTH_SHORT).show();

    }
}
