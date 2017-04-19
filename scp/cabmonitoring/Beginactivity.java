package scp.cabmonitoring;

import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import ControlTool.kill;

public class Beginactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beginactivity);
        //关闭进程
        kill.getInstance().addActivity(this);
        //App无标题
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_beginactivity);
        Window window=getWindow();//设置透明状态栏，这样让contentview向上移动
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup mContentView=(ViewGroup)findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView=mContentView.getChildAt(0);
        if(mChildView!=null)
        {
            //注意不是设置ContentView 的FitsSystemWindows,而是设置ContentView 的第一个子View,使其不为系统View预留空间。
            ViewCompat.setFitsSystemWindows(mChildView,false);
        }
        //改变字的大小
        TextView Text = (TextView)findViewById(R.id.textView1);
        Spannable span = new SpannableString(Text.getText());
        // span.setSpan(new AbsoluteSizeSpan(45),0,2,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(29),2,7,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//SpannableString类来对指定文本进行相关渲染处理
        Text.setText(span);
        //Activity跳转
        UpdateThread t=new UpdateThread();
        new Thread(t).setDaemon(true);
        new Thread(t).start();
    }
    class  UpdateThread implements Runnable
    {
        private int m=0;
        public void run()
        {
            while(m<2000)
            {
                System.out.println("m=" + m);
                try
                {
                    Thread.sleep(1);
                } catch (InterruptedException x)
                {
                }
                m++;
            }
            System.out.println("m=200");
            Intent mainIntent = new Intent(Beginactivity.this,MainActivity.class);
            Beginactivity.this.startActivity(mainIntent);
            Beginactivity.this.finish();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.beginctivity, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
