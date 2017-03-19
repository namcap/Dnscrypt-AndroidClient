package namcap.dnscryptAndroidclient;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 3/11/2017.
 */

public class DnscryptService extends Service {

    private static final String name="DnscryptService";
    private static boolean running=false;
    private static final int NOTIFICATION_CODE=1;
    private WorkerThread workerThread=null;

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        if (! running && intent!=null) {
            ArrayList<String> servers = new ArrayList<>(DataBucket.getServers());
            int port = DataBucket.portSelected;
            if (!servers.isEmpty() && 1024 <= port && port <= 65535) {
                Toast.makeText(this, "Starting " + name, Toast.LENGTH_SHORT).show();
                //Clear log
                sendMsg(Constants.SERVICE_LOG_EVENT, Constants.SERVICE_LOG_EVENT_CLEAR, true);
                //Start service
                workerThread = new WorkerThread(servers, port);
                workerThread.start();

                //Set running flag
                running = true;
            }
        }
        if (! running) {
            this.stopSelf();
        }
        sendMsg(Constants.SERVICE_STATUS_EVENT,Constants.SERVICE_STATUS_EVENT_NEW_STATUS,running);
        //Foreground
        startForeground(NOTIFICATION_CODE,getNotification());
        return Service.START_STICKY;
    }

    public static boolean isRunning(){
        return running;
    }

    private void sendMsg(String event_name,String key,Serializable data) {
        Intent intent=new Intent(event_name);
        //Put extra data
        intent.putExtra(key,data);
        //Return true if receiver exists and false if not
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Notification getNotification(){
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);

        Notification notification=builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_AUTO_CANCEL;

        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

    @Override
    public void onRebind(Intent intent) {

    }

    @Override
    public void onDestroy(){
        //Stop service
        if (workerThread != null) {
            workerThread.keepRunning=false;
            workerThread.interrupt();
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stopForeground(true);
        running=false;
        Toast.makeText(this,name+" destroyed",Toast.LENGTH_LONG).show();
        sendMsg(Constants.SERVICE_STATUS_EVENT,Constants.SERVICE_STATUS_EVENT_NEW_STATUS,running);
        super.onDestroy();
    }

    private class WorkerThread extends Thread {

        private final ArrayList<String> servers;
        private final int port;
        private final int INTERVAL_BETWEEN_RETRIES_MS=1000;
        private final int INTERNAL_BETWEEN_CHECKING_LOG=20;
        private final int log_maxLines=200;
        volatile boolean keepRunning=true;
        private Process process=null;
        private BufferedReader bufferedReader=null;

        private WorkerThread(ArrayList<String> servers,int port) {
            this.servers = servers;
            this.port = port;
        }

        public void run() {
            String line;
            boolean bin_running;
            try {
                while (keepRunning) {
                    //Loop through servers
                    for (String i : servers) {
                        if (! keepRunning)
                            break;
                        try {
                            //Start dnscrypt binary in a separate process
                            process=new ProcessBuilder()
                                    .command("/system/xbin/dnscrypt-proxy",
                                            "--loglevel=3",
                                            "--resolvers-list="+Constants.CSV_FILE,
                                            "--local-address=127.0.0.1:"+port,
                                            "--resolver-name="+i)
                                    .redirectErrorStream(true)
                                    .start();
                            bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
                            //Obtain output from process and forward it to log
                            bin_running=true;
                            while (keepRunning && bin_running) {
                                //Loop will continue as long as binary and Service are still running
                                try{
                                    process.exitValue();
                                    //Process has been terminated
                                    bin_running=false;
                                } catch (IllegalThreadStateException e){
                                    //Process still running
                                    //Empty block here
                                }
                                while (bufferedReader.ready()) {
                                    line=bufferedReader.readLine();
                                    sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,line+"\n");
                                }
                                Thread.sleep(INTERNAL_BETWEEN_CHECKING_LOG);
                            }
                            bufferedReader.close();
                            //Add a newline to separate output from different runs
                            sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,"\n");
                        } catch (IOException e) {
                            sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,e.toString()+"\n");
                        }
                        Thread.sleep(INTERVAL_BETWEEN_RETRIES_MS);
                    }
                }
            } catch (InterruptedException e) {
                //Break out of loops on interrupted
                //Empty block here
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }
}
