package Wifi_Link;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ControlTool.AppConstant;
import scp.cabmonitoring.MainActivity;

/**
 * Created by Administrator on 2017/4/11.
 */
public class TcpClientThread implements  Runnable {

    MainActivity mainActivity;
    SharedPreferences sharedPreferences;
    private Handler msgHandler;
    private int Wifi_State_Operate = 0;
    public Socket socket;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    Client_close client_close;
    private int Instrument_stream_or_Data_stream_mode = 0;
    Wifi_Operate_Readdata wifi_operate_readdata;
    public Handler wifi_send_Handler;
    public TcpClientThread(MainActivity mainActivity, Handler msgHandler) {
        this.mainActivity = mainActivity;
        this.msgHandler = msgHandler;
        sharedPreferences = mainActivity.getSharedPreferences("GuZhangZhenDuan_Data", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
    }
    private CALLbackdata calLbackdata;

    public void setClientThread_Callbackdata(CALLbackdata callbackdata) {
        this.calLbackdata = callbackdata;
    }
    public interface CALLbackdata {
        public void wifi_sate(boolean wifi_sate);
    }

    @Override
    public void run() {

        mainActivity.Wifi_State = AppConstant.Wifi_State.DISCONNECT;

        switch (Wifi_State_Operate = sharedPreferences.getInt("Wifi_State_Operate", AppConstant.Wifi_State.DISCONNECT)) {
            case AppConstant.Wifi_State.LINK:
                System.out.println("checkBox_link选择成功，已经进入Wifi链接模式,进入到 TcpServerThread 线程。。。。。。。。。。。。。");
                Wifi_State_Operate_Link();
                break;
            case AppConstant.Wifi_State.DISCONNECT:
                System.out.println("checkBox_disconnect选择成功，断开wifi链接,进入到 TcpServerThread 线程。。。。。。。。。。。。。");
                client_close=new Client_close(socket,inputStream,outputStream);
                break;
            case AppConstant.Wifi_State.AGAIN:
                System.out.println("checkBox_again选择成功，重新链接操作中,进入到 TcpServerThread 线程。。。。。。。。。。。。。。。");
                client_close=new Client_close(socket,inputStream,outputStream);//1、先清除之前的链接
                calLbackdata.wifi_sate(false);//把断开置位 ，使得断开网络选上，执行Link操作。
                break;

        }
    }
    //链接操作子函数
    public boolean Wifi_State_Operate_Link() {
        int port = sharedPreferences.getInt("device_PORT", 8000);
        String ip = sharedPreferences.getString("device_IP", "192.168.191.1");
        try {
            if (socket != null)
            {
                socket = new Socket(ip, port);
                System.out.println("\"11111111111111 Socket已经存在了\"" + "端口为" + socket.getLocalPort());
            }
            else
            {

                socket = new Socket(ip, port);
                System.out.println("\"11111111111111 Socket不存在\"" + "端口为" + socket.getLocalPort());
            }
         //   socket = new Socket(ip, port);
            InetAddress inetAddress = socket.getInetAddress();
            sockets.add(socket);
            MainActivity.Wifi_State = AppConstant.Wifi_State.LINK;
            calLbackdata.wifi_sate(true);
            System.out.println("\"1111111111111111111111111111111111111111111111\"+   socket与客户端通信成功。。。。。,socket.getLocalAddress()=="+socket.getLocalAddress());
            mainActivity.send_recieve_String = "";
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            wifi_operate_readdata=new Wifi_Operate_Readdata( socket,inputStream,outputStream);
            new Thread(wifi_operate_readdata).start();
        } catch (IOException e)
        {
            System.out.println("\"1111111111111111111111111111111111111111111111\"+   socket与server网络超时。。。。。。。。。。。。");
            e.printStackTrace();
        }
        Looper.prepare();
        wifi_send_Handler=new Wifi_send_Handler();
        Looper.loop();// 让Looper开始工作，从消息队列里取消息，处理消息。
        return true;
    }
    //都是server发送数据给client
    public class Wifi_send_Handler extends  Handler
    {
        @Override
        public void handleMessage(Message receiveMsg)
        {  Log.e("TcpServerThread", "程序进入ReceiveHandler");
            String header="";
            switch(receiveMsg.what)
            {
                case AppConstant.Instrument_stream_mode://命令流
                    WifiSend(header+receiveMsg.obj.toString(),AppConstant.Instrument_stream_mode);//发送数据
                    Log.e("TcpServerThread", "程序进入ReceiveHandler,receiveMsg.what"+"case AppConstant.Instrument_stream_mode:"+receiveMsg.what);
                    break;
                case AppConstant.Data_stream_mode://数据流
                    Log.e("TcpServerThread", "程序进入ReceiveHandler,receiveMsg.what"+"AppConstant.Data_stream_mode"+receiveMsg.what);
                    break;
                default:
                    Log.e("TcpServerThread", "程序进入ReceiveHandler,receiveMsg.what"+"default===="+receiveMsg.what);
                break;
            }
        }
    }
    //发送命令
    public void WifiSend(String command,int mode)
    {
        String _pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(_pattern);
        System.out.println("开始：" + format.format(new Date()));
        try{
            if(mode==AppConstant.Instrument_stream_mode)
              //  outputStream.write((command.toString()+"\r\n").getBytes("gbk"));
             outputStream.write((command.toString()).getBytes("gbk"));
            else if(mode==AppConstant.Caiyang.Caiyang_Start)
                outputStream.write(command.toString().getBytes("gbk"));
            outputStream.flush();
            System.out.println( command.toString()+"\r\n");
            boolean i;
            i= socket.getOOBInline();
            System.out.println("socket.getOOBInline()="+i);
        }catch (Exception e)
        {
            System.out.println(e);
            System.out.println("wifi发送数据的时候出错了");
            client_close=new Client_close( socket,inputStream,outputStream);
        }
        System.out.println("数据发送结束：" + format.format(new Date()));
        boolean sucess=true;
        sucess= socket.isConnected();
        System.out.println("数据发送结束：" + "socket.isConnected()"+sucess);
    }

    public class Wifi_Operate_Readdata implements Runnable {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public Wifi_Operate_Readdata( Socket socketm,InputStream inputStream, OutputStream outputStream)
        {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.socket = socket;
            Instrument_stream_or_Data_stream_mode = AppConstant.Instrument_stream_mode;
        }

        @Override
        public void run() {

            while (MainActivity.Wifi_State == AppConstant.Wifi_State.LINK)//链接正常的情况下
            {

                System.out.println(" Wifi_Operate_Readdata_Thread正在运行");
                switch (Instrument_stream_or_Data_stream_mode)//命令流或者数据流
                {
                    case AppConstant.Instrument_stream_mode://命令流，这种情况
                        instructions_Read(socket, inputStream, outputStream);
                        break;
                }
            }
        }
    }
    public void instructions_Read(  Socket socket, InputStream inputStream, OutputStream outputStream)
    {
        System.out.println("instructions_Read正在运行");
        byte[] cbuff = new byte[256];
        String string;
        try
        {
            int size = inputStream.read(cbuff);
            if(size>0)
            {

                string = new String(cbuff, "gbk");
                System.out.println("数组整合前" + string);//系统默认打印为gnk了，但是汉子编码依旧是UTF-8
                msgHandler_SendMessage(string);//发送状态信息
            }
            else
            {
                System.out.println("Socket已经断开，。size的大小:" + size);
                string="Socket已经断开，。size的大小:" + size;
                MainActivity.Wifi_State=AppConstant.Wifi_State.DISCONNECT;
                client_close=new Client_close(socket,inputStream,outputStream);
                calLbackdata.wifi_sate(false);
                msgHandler_SendMessage(string);//发送状态信息
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void msgHandler_SendMessage( String string)
    {
//        if (mainActivity.send_recieve_String.length() > 0)
//        {
//            mainActivity.send_recieve_String = mainActivity.send_recieve_String + "\r\n" + string;
//        }
//        else
            mainActivity.send_recieve_String = string;
        Message message = new Message();
        message.what = AppConstant.Instrument_stream_mode;
        message.obj = mainActivity.send_recieve_String;
        msgHandler.sendMessage(message);
    }

    ///得到仍处于连接当中的Socket
    List<Socket> sockets=new ArrayList<Socket>();//保存所有接受的socket
    public ArrayList<Socket > getConnectedSockets()
    {
        ArrayList<Socket > alivedSockets = new ArrayList<Socket>();
        for(int i=0;i<sockets.size();i++){
            if(sockets.get(i).isConnected()){
                alivedSockets .add(sockets.get(i));
            }
        }
        return alivedSockets;
    }
}
