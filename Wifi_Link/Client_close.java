package Wifi_Link;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import ControlTool.AppConstant;
import scp.cabmonitoring.MainActivity;

/**
 * Created by Administrator on 2017/4/11.
 */
public class Client_close
{

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public Client_close(Socket socket,InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.socket = socket;
        Client_close_operate();
    }
    public void Client_close_operate()
    {
        try
        {
            System.out.println("进入Wifi关闭函数");

            System.out.println("关闭之前socket"+socket);
            System.out.println("关闭之前inputStream"+inputStream);
            System.out.println("关闭之前outputStreamt"+outputStream);
            MainActivity.Wifi_State= AppConstant.Wifi_State.DISCONNECT;

                if(socket!=null)
                    socket.close();
            if(inputStream!=null)
                inputStream.close();
            if(outputStream!=null)
                outputStream.close();
            System.out.println("关闭之后socket"+socket);
            System.out.println("关闭之后inputStream"+inputStream);
            System.out.println("关闭之后outputStream"+outputStream);
        }
        catch (Exception E)
        {
            System.out.println("关闭client_socket失败。。。。。。。。。。。。。。。");
            E.printStackTrace();
        }

    }
}
