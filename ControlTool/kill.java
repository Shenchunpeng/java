package ControlTool;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/30.
 */
public class kill extends Application {
    //运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<Activity>();
    //为了实现每次使用该类时不创建新的对象而创建的静态对象
    private static kill instance;
    //构造方法
    private kill(){}
    //实例化一次
    public synchronized static kill getInstance()
    {
        if (null == instance) {
            instance = new kill();
        }
        return instance;
    }
    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }
    //关闭每一个list内的activity
    public void exit() {
        try {
            for (Activity activity:mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    //杀进程 ,垃圾回收
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
}


