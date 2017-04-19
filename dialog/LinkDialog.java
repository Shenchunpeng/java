package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import scp.cabmonitoring.MainActivity;
import scp.cabmonitoring.R;

/**
 * Created by Administrator on 2017/4/10.
 */
public class LinkDialog  extends Dialog
{
    private Button Button_save,Button_cancel;
    public  View.OnClickListener getlinkDialog_saveListener,getlinkDialog_cancelListener;
    public LinkDialog(Context context, int themeResId) {
        super(context, themeResId);
        MainActivity mainActivity=(MainActivity)context;
        System.out.println("进入LinkDialog---------LinkDialog函数" );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkdialog);
        System.out.println("进入LinkDialog---------onCreate函数" );
        initiate();

    }


    public void getlinkDialog_saveListener(View.OnClickListener listener)
    {
        System.out.println("进入DataInformation---------getdatainformation_confirmListener函数" );
        this.getlinkDialog_saveListener=listener;
    }
    public void  getlinkDialog_cancelListener(View.OnClickListener listener)
    {
        System.out.println("进入DataInformation---------getgetlinkDialog_cancelListener函数" );
        this.getlinkDialog_cancelListener=listener;
    }
    public void initiate()
    {
        Button_save=(Button)findViewById(R.id.Button_save);
        Button_cancel=(Button)findViewById(R.id.Button_cancel);
        //Button_save.setOnClickListener(new OnclickListenner_all());
        Button_save.setOnClickListener(getlinkDialog_saveListener);
        //Button_cancel.setOnClickListener(new OnclickListenner_all());
        Button_cancel.setOnClickListener(getlinkDialog_cancelListener);
    }
}
