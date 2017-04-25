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
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
            ArrayList<String> servers = new ArrayList<>(DataBucket.servers);
            int port = DataBucket.portSelected;
            if (!servers.isEmpty() && 1024 <= port && port <= 65535) {
                Toast.makeText(this, "Starting " + name, Toast.LENGTH_SHORT).show();
                //Clear log
                sendMsg(Constants.SERVICE_LOG_EVENT, Constants.SERVICE_LOG_EVENT_CLEAR, true);
                //Start service
                workerThread = new WorkerThread(servers,
                        port,
                        DataBucket.ephemeral_keys,
                        DataBucket.logLevel);
                workerThread.start();

                //Set running flag
                running = true;
            }
        }
        if (! running) {
            this.stopSelf();
        }
        else {
            //Start foreground
            startForeground(NOTIFICATION_CODE,getNotification());
        }
        sendMsg(Constants.SERVICE_STATUS_EVENT,Constants.SERVICE_STATUS_EVENT_NEW_STATUS,running);
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
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        Runtime.getRuntime().gc();
    }

    @Override
    public void onDestroy(){
        //Stop service
        if (workerThread != null) {
            workerThread.keepRunning=false;
            workerThread.interrupt();
            if (workerThread.process!=null) workerThread.process.destroy();
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
        private final Character logLevel;
        private final int INTERVAL_BETWEEN_RETRIES_MS=1000;
        private final int INTERNAL_BETWEEN_CHECKING_LOG=20;
        private final boolean ephemeral_keys;
        private final Random rnd=new Random();
        volatile boolean keepRunning=true;
        private Process process=null;
        private BufferedReader bufferedReader=null;

        private WorkerThread(ArrayList<String> servers,int port,boolean ephemeral_keys,Character logLevel) {
            this.servers = servers;
            this.port = port;
            this.ephemeral_keys=ephemeral_keys;
            this.logLevel=logLevel;
        }

        public void run() {
            String line;
            boolean bin_running;
            int loop_cnt=0;
            int exitVal=-1;
            String i;
            //Command for testing servers
            ArrayList<String> cmd_test=new ArrayList<>(Arrays.asList(
                    "/system/xbin/dnscrypt-proxy",
                    "--resolver-name=",
                    "--resolvers-list="+Constants.CSV_FILE,
                    "--loglevel="+logLevel,
                    "--test=0" //margin
            ));
            //Command that really start the proxy
            ArrayList<String> command=new ArrayList<>(Arrays.asList(
                    "/system/xbin/dnscrypt-proxy",
                    "--resolver-name=",
                    "--loglevel="+logLevel,
                    "--resolvers-list="+Constants.CSV_FILE,
                    "--local-address=127.0.0.1:"+port
            ));
            if (ephemeral_keys) {
                command.add("--ephemeral-keys");
            }
            try {
                while (keepRunning) {
                    i=servers.get(rnd.nextInt(servers.size()));
                    if (! keepRunning)
                        break;
                    try {
                        if (1<=loop_cnt) {
                            Runtime.getRuntime().gc();
                            loop_cnt=0;
                        }
                        else {
                            loop_cnt+=1;
                        }
                        //Test server
                        sendMsg(Constants.SERVICE_LOG_EVENT,
                                Constants.SERVICE_LOG_EVENT_APPEND,
                                "Testing server "+i+"\n");
                        //Start a process
                        cmd_test.set(1,"--resolver-name="+i);
                        process=new ProcessBuilder()
                                .command(cmd_test)
                                .redirectErrorStream(true)
                                .start();
                        bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
                        bin_running=true;
                        while (keepRunning && bin_running) {
                            //Loop will continue as long as binary and service are still running
                            try{
                                exitVal=process.exitValue();
                                //Process has been terminated
                                bin_running=false;
                            } catch (IllegalThreadStateException e){
                                //Process still running
                            }
                            do {
                                //Forward output
                                line=bufferedReader.readLine();
                                sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,line+"\n");
                            } while (bufferedReader.ready());
                            Thread.sleep(INTERNAL_BETWEEN_CHECKING_LOG);
                        }
                        bufferedReader.close();
                        if (exitVal != 0) {
                            switch (exitVal) {
                                case 2:
                                    sendMsg(Constants.SERVICE_LOG_EVENT,
                                            Constants.SERVICE_LOG_EVENT_APPEND,
                                            "No valid certificate\n");
                                    break;
                                case 3:
                                    sendMsg(Constants.SERVICE_LOG_EVENT,
                                            Constants.SERVICE_LOG_EVENT_APPEND,
                                            "Timeout occurred\n");
                                    break;
                                case 4:
                                    sendMsg(Constants.SERVICE_LOG_EVENT,
                                            Constants.SERVICE_LOG_EVENT_APPEND,
                                            "A valid certificate expires soon\n");
                                    break;
                                default:
                                    sendMsg(Constants.SERVICE_LOG_EVENT,
                                            Constants.SERVICE_LOG_EVENT_APPEND,
                                            "Undocumented exit value "+exitVal);
                                    break;
                            }
                            Thread.sleep(INTERVAL_BETWEEN_RETRIES_MS);
                            continue;
                        }
                        //else server is online and certificate is valid
                        //Add a newline to separate output from different runs
                        sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,"\n");

                        //Connect to server
                        sendMsg(Constants.SERVICE_LOG_EVENT,
                                Constants.SERVICE_LOG_EVENT_APPEND,
                                "Connecting to server "+i+"\n");
                        command.set(1,"--resolver-name="+i);
                        //Start a process
                        process=new ProcessBuilder()
                                .command(command)
                                .redirectErrorStream(true)
                                .start();
                        bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
                        //Obtain output from process and forward it to log
                        bin_running=true;
                        while (keepRunning && bin_running) {
                            //Loop will continue as long as binary and service are still running
                            try{
                                process.exitValue();
                                //Process has been terminated
                                bin_running=false;
                            } catch (IllegalThreadStateException e){
                                //Process still running
                                //Empty block here
                            }
                            do {
                                line=bufferedReader.readLine();
                                sendMsg(Constants.SERVICE_LOG_EVENT,Constants.SERVICE_LOG_EVENT_APPEND,line+"\n");
                            } while (bufferedReader.ready());
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
