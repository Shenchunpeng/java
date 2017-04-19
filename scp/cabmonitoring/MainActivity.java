package scp.cabmonitoring;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ControlTool.AppConstant;
import Wifi_Link.TcpClientThread;
import dialog.LinkDialog;

/**
 * Created by Administrator on 2017/4/10.
 */
public class MainActivity extends AppCompatActivity
{
    private TextView Client_settings,TextView_current_ip,textview_notice ;
    private TextView V_ON,V_OFF,PV_UP,PV_DOWN,PV_Start,PV_Stop,TextView_send_recieve_String;
    private CheckBox checkBox_link,checkBox_disconnect,checkBox_again;
    private LinkDialog linkDialog;
    public  String device_ip="",device_port="";
    private EditText ip,port;
   public  String send_recieve_String="";
    public static int Wifi_State= AppConstant.Wifi_State.DISCONNECT;
    public  int Wifi_State_Operate=0;
    private TcpClientThread tcpClientThread;
    private  UpdateBarHander updateBarHander;
    private SharedPreferences sharedPreferences;
    private int message_cishu=0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initial();
        tcpClientThread.setClientThread_Callbackdata(new TcpClientThread.CALLbackdata() {
            @Override
            public void wifi_sate(boolean wifi_sate)
            {

                Message msg = new Message();
                if(wifi_sate==false)
                {
                    Wifi_State=AppConstant.Wifi_State.DISCONNECT;
                    msg.what =AppConstant.Wifi_State.DISCONNECT;
                    msg.obj=AppConstant.Wifi_State.DISCONNECT;
                }
                else
                {
                    Wifi_State=AppConstant.Wifi_State.LINK;
                    msg.what =AppConstant.Wifi_State.LINK;
                    msg.obj=AppConstant.Wifi_State.LINK;
                }
                updateBarHander.sendMessage(msg);
                System.out.println("     tcpClientThread.setClientThread_Callbackdata(new TcpClientThread.CALLbackdata()，其中Wifi_State="+wifi_sate );
            }
        });
    }

    public boolean CompoundButton=true;
    public class OnCheckBoxListenner implements CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if(CompoundButton)
            {
                switch (buttonView.getId()) {
                    case R.id.checkBox_link:
                        if (isChecked && Wifi_State == AppConstant.Wifi_State.DISCONNECT)//在Wifi未链接时候，是不能再次链接的
                        {//链接操作进行。。。。。。。。。
                            checkBox_link.setClickable(false);//1、当点击链接后不能，再次链接，除非断开
                            checkBox_link.setEnabled(true);
                            checkBox_disconnect.setChecked(false);
                            checkBox_again.setChecked(false);
                            checkBox_link.setChecked(false);//2、同时让它复位，等待链接成功后会自动置位
                            Wifi_State_Operate = AppConstant.Wifi_State.LINK;
                            if (device_ip.matches("[0-9.]+") && device_port.matches("[0-9]+")) {
                                sharedPreferences.edit().putString("device_IP", device_ip).commit();
                                int device_PORT = Integer.parseInt(device_port);
                                sharedPreferences.edit().putInt("device_PORT", device_PORT).commit();
                                Wifi_State_Operate = AppConstant.Wifi_State.LINK;
                                sharedPreferences.edit().putInt("Wifi_State_Operate", Wifi_State_Operate).commit();
                                new Thread(tcpClientThread).start();
                            } else
                                Toast.makeText(MainActivity.this, "请输入合法的Ip地址和端口port", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                        }
                        System.out.println("进入LinkDialog--------checkBox_link:函数");
                        break;
                    case R.id.checkBox_disconnect:
                        if (isChecked) {//断开操作进行。。。。。。。。。
                            checkBox_link.setClickable(true);
                            checkBox_link.setEnabled(true);
                            checkBox_link.setChecked(false);
                            checkBox_again.setChecked(false);
                            checkBox_again.setClickable(true);
                            Wifi_State_Operate = AppConstant.Wifi_State.DISCONNECT;
                            sharedPreferences.edit().putInt("Wifi_State_Operate", Wifi_State_Operate).commit();
                            new Thread(tcpClientThread).start();
                        } else
                            System.out.println("进入LinkDialog--------checkBox_disconnect:函数");
                        break;
                    case R.id.checkBox_again:
                        if (isChecked) {
                            checkBox_disconnect.setChecked(false);
                            checkBox_link.setChecked(false);
                            checkBox_again.setClickable(false);
                            Wifi_State_Operate = AppConstant.Wifi_State.AGAIN;
                            sharedPreferences.edit().putInt("Wifi_State_Operate", Wifi_State_Operate).commit();
                            new Thread(tcpClientThread).start();
                        }
                        else
                        {

                        }
                        System.out.println("进入LinkDialog--------checkBox_again:函数");
                        break;
                }
            }
        }
    }
    public List<Socket> alivedSockets= new ArrayList<Socket>(); //保存所有接受的socket
    protected class UpdateBarHander extends android.os.Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            CompoundButton=false;
            super.handleMessage(msg);
            if(msg.what==AppConstant.Instrument_stream_mode)//把命令流中已经成功发送的数据返回。如果没有返回，实际上我也可以看到，在广播那里我已经对变量操作了
            {
                if(message_cishu<100)
                {
                    message_cishu++;
                    if(send_recieve_String.length()>0)
                    {
                        send_recieve_String=send_recieve_String+"\r\n"+ msg.obj.toString();//接受到的命令
                    }
                    else
                        send_recieve_String=msg.obj.toString();//接受到的命令
                }
                else
                {
                    message_cishu=0;
                    send_recieve_String=msg.obj.toString();//接受到的命令
                }
                TextView_send_recieve_String.setText(send_recieve_String);
            }
            if(msg.what==AppConstant.Wifi_State.DISCONNECT)//Client已经断开
            {

                TextView_current_ip.setText("192.168.100.1(server)");
                textview_notice.setText("故障诊断，设备已经离线");
                checkBox_link.setChecked(false);
                checkBox_disconnect.setChecked(true);
                checkBox_link.setClickable(true);//这个仅仅只能让它点击无效，颜色和正常一样
                if (Wifi_State_Operate == AppConstant.Wifi_State.AGAIN)//如果打开重连模式
                {
                    System.out.println("链接失败。打开了重连模式");
                    System.out.println("1、发送断开链接这个消息，线程已经清空了的socket！,2、开启“链接线程--LInk”，执行Link");
                    //1、继续保持重连继续勾上
                    checkBox_again.setChecked(true);
                    //2、置位链接标志位
                    sharedPreferences.edit().putInt("Wifi_State_Operate", AppConstant.Wifi_State.LINK).commit();

                    new Thread(tcpClientThread).start();
                }
                else//如果没有打开重连模式
                {
                    System.out.println("链接失败。没有打开重连模式");
                    //1、继续保持重连取消
                    checkBox_again.setChecked(false);
                    //2、置位"断开链接"标志位
                    sharedPreferences.edit().putInt("Wifi_State_Operate", AppConstant.Wifi_State.DISCONNECT).commit();
                }
            }
            if (msg.what==AppConstant.Wifi_State.LINK)//Client已经链接上
            {
                checkBox_link.setChecked(true);
                checkBox_disconnect.setChecked(false);
                checkBox_link.setClickable(false);
                System.out.println(" 正在开始采样"+send_recieve_String);
                if( Wifi_State_Operate ==AppConstant.Wifi_State.AGAIN)
                {
                    System.out.println("链接成功。打开了重连模式");
                    checkBox_again.setChecked(true);
                }
                else
                {
                    checkBox_again.setChecked(false);
                    System.out.println("链接成功。没有打开重连模式");
                }
                alivedSockets=tcpClientThread.getConnectedSockets();        Socket socket=null;int i = 0;
                String   alivedSockets_Ip="192.168.100.1(server)";
               for( i = 0;i<alivedSockets.size();i++)
               {
                    HashMap<String,String> map= new HashMap<String, String>();
                    if (alivedSockets.get(i).isConnected())
                    {
                        socket =alivedSockets.get(i);
                        map.put("lianjie",socket.getInetAddress().toString());
                        alivedSockets_Ip=socket.getInetAddress().toString();
                        alivedSockets_Ip=socket.getLocalAddress().toString();
                        int  alivedSockets_Port=socket.getLocalPort();
                        String[] strings=alivedSockets_Ip.split("/");
                        System.out.println(" String[] strings="+strings);
                        alivedSockets_Ip=strings[1];
                       // sharedPreferences.edit().putString("alivedSockets_Ip",alivedSockets_Ip).commit();
                        //sharedPreferences.edit().putString("alivedSockets_Port",String.valueOf(alivedSockets_Port)).commit();
                    }
              }
                alivedSockets_Ip=alivedSockets_Ip+"(client)";
                TextView_current_ip.setText(alivedSockets_Ip);
                textview_notice.setText("故障诊断，实时检测过程中");
            }
            CompoundButton=true;
        }
    }
    public class OnclickListenner_all implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case  R.id.Button_save:
                    if(ip.getText().toString().length()<=0||port.getText().toString().length()<=0)
                    {
                        Toast.makeText(MainActivity.this, "请输入Ip地址和端口port", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {

                        device_ip=ip.getText().toString();
                        device_port=port.getText().toString();
                        if (device_ip.matches("[0-9.]+") && device_port.matches("[0-9]+")) {
                            sharedPreferences.edit().putString("device_IP", device_ip).commit();
                            int device_PORT = Integer.parseInt(device_port);
                            sharedPreferences.edit().putInt("device_PORT", device_PORT).commit();
                        } else
                            Toast.makeText(MainActivity.this, "请输入合法的Ip地址和端口port", Toast.LENGTH_SHORT).show();
                        linkDialog.dismiss();
                    }
                    System.out.println("进入LinkDialog---------OnclickListenner_all函数" );
                    break;
                case  R.id.Button_cancel:
                    System.out.println("进入LinkDialog---------OnclickListenner_all函数" );
                    linkDialog.dismiss();
                    break;
                case R.id.V_ON:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("V_ON",AppConstant.Instrument_stream_mode);
                        send_recieve_String="";
                    }
                    break;
                case R.id.V_OFF:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("V_OFF",AppConstant.Instrument_stream_mode);
                    }
                    break;
                case R.id.PV_UP:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("V+",AppConstant.Instrument_stream_mode);
                    }
                    break;
                case R.id.PV_DOWN:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("V-",AppConstant.Instrument_stream_mode);
                    }
                    break;
                case R.id.PV_Start:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("START",AppConstant.Instrument_stream_mode);
                    }
                    break;
                case R.id.PV_Stop:
                    if(Wifi_State==AppConstant.Wifi_State.LINK)
                    {
                        wifi_send_and_show("STOP",AppConstant.Instrument_stream_mode);
                    }
                    break;
            }
        }
    }
    public void wifi_send_and_show(String s,int mode)
    {

        //开始采样
        Message msg=new Message();
        msg = new Message();
        msg.what =mode;//代表发送按钮操作了一次
        msg.obj =s;
        tcpClientThread.wifi_send_Handler.sendMessage(msg);
        if(send_recieve_String!=null&&msg.obj!=null)
        {
            s=msg.obj.toString();
            s=send_recieve_String+ "\r\n";
            send_recieve_String=send_recieve_String+ "\r\n"+msg.obj.toString();
        }
        else if(msg.obj!=null)
        {
            send_recieve_String=msg.obj.toString();
        }
        TextView_send_recieve_String.setText(send_recieve_String);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.Client_settings:
                linkDialog.show();
                ip=(EditText)linkDialog.findViewById(R.id.ip);
                port=(EditText)linkDialog.findViewById(R.id.port);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public DialogInterface.OnClickListener listener=null;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            // 确认对话框
            final AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setMessage("你确定要退出？");
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case AlertDialog.BUTTON1:// "确认"按钮退出程序
                            NotificationManager notificationManager = (NotificationManager) MainActivity.this
                                    .getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancel(0);
                            //   tcpServerThread.WifiSend("c");
                            System.exit(0);
                            break;
                        case AlertDialog.BUTTON2:// "取消"第二个按钮取消对话框
                            isExit.cancel();
                            break;
                        default: break;
                    }
                }
            };
            // 注册监听
            //设置对话框按钮的文字和点击事件
            isExit.setButton( AlertDialog.BUTTON1,"YES",listener);
            isExit.setButton( AlertDialog.BUTTON2,"NO",listener);
            // 显示对话框
            isExit.show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        // return  true;
        return super.onCreateOptionsMenu(menu);
    }
    public void  initial()
    {
        /***********这个我在菜单里面就生成了
        Client_settings=(TextView)findViewById(R.id.Client_settings);
        Client_settings.setOnClickListener(new OnclickListenner_all());
         */
        linkDialog=new LinkDialog(MainActivity.this,R.style.Dialog);
        //设置触摸对话框以外的地方不关闭对话框
        linkDialog.setCanceledOnTouchOutside(false);
        linkDialog.getlinkDialog_cancelListener(new OnclickListenner_all());
        linkDialog.getlinkDialog_saveListener(new OnclickListenner_all());

        //界面UI初始化
        //chekbox操作区域
        checkBox_link=(CheckBox)findViewById(R.id.checkBox_link);
        checkBox_disconnect=(CheckBox)findViewById(R.id.checkBox_disconnect);
        checkBox_again=(CheckBox)findViewById(R.id.checkBox_again);
        checkBox_link.setOnCheckedChangeListener(new OnCheckBoxListenner());
        checkBox_disconnect.setOnCheckedChangeListener(new OnCheckBoxListenner());
        checkBox_again.setOnCheckedChangeListener(new OnCheckBoxListenner());
        TextView_current_ip=(TextView)findViewById(R.id.TextView_current_ip);
        //指令交互区域
        TextView_send_recieve_String=(TextView)findViewById(R.id.TextView_send_recieve_String);
        TextView_send_recieve_String.setMovementMethod(ScrollingMovementMethod.getInstance());
        //最下面notice区域
        textview_notice=(TextView)findViewById(R.id.textview_notice) ;
        //按钮调压区域
        V_ON=(TextView)findViewById(R.id.V_ON);
        V_ON.setOnClickListener(new OnclickListenner_all());
        V_OFF=(TextView)findViewById(R.id.V_OFF);
        V_OFF.setOnClickListener(new OnclickListenner_all());
        PV_UP=(TextView)findViewById(R.id.PV_UP);
        PV_UP.setOnClickListener(new OnclickListenner_all());
        PV_DOWN=(TextView)findViewById(R.id.PV_DOWN);
        PV_DOWN.setOnClickListener(new OnclickListenner_all());
        PV_Start=(TextView)findViewById(R.id.PV_Start);
        PV_Start.setOnClickListener(new OnclickListenner_all());
        PV_Stop=(TextView)findViewById(R.id.PV_Stop);
        PV_Stop.setOnClickListener(new OnclickListenner_all());
        //接受消息区域
        updateBarHander=new UpdateBarHander();
        tcpClientThread=new TcpClientThread(MainActivity.this,updateBarHander);//这是个专门为最大值，特征频率跟新数据的线程

        sharedPreferences =getSharedPreferences("GuZhangZhenDuan_Data", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
        message_cishu=0;
    }
}
