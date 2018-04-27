package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import kotel.hanzan.data.StaticData;
import kotel.hanzan.view.CardSliderView;

public class Test extends AppCompatActivity {
    CardSliderView sliderView;

    private ArrayList<String> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        imageList = new ArrayList<>();
        imageList.add("https://s3.ap-northeast-2.amazonaws.com/hanjan/vr_test_01.jpg");
        imageList.add("https://postfiles.pstatic.net/MjAxODAzMjBfMjc0/MDAxNTIxNTMyNDA1ODE3.So2k_cDkWrzepftlgQJKFpTr0-4AzCCfqCnooEJ9ZZsg.YecgcwirPvD018mB1wzzKcVoQHL-eP4fts-l_o2PakIg.JPEG.nong-up/Apple_Picking_070.jpg?type=w966");
        imageList.add("https://s3.ap-northeast-2.amazonaws.com/hanjan/vr_test_01.jpg");

        sliderView = findViewById(R.id.test_sliderView);

        sliderView.setImageList(imageList);


        int sliderViewHeight = StaticData.displayWidth/2;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,sliderViewHeight);
        sliderView.setLayoutParams(params);

        sliderView.setClickListener((View view, int position) -> {
            Intent intent = new Intent(Test.this,VrPage.class);
            intent.putExtra("src",sliderView.getImageString(position));
            startActivity(intent);
        });

    }
}
