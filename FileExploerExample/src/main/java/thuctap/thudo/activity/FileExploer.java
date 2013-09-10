package thuctap.thudo.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.os.Handler;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import thuctap.thudo.adapter.DownloadListAdapter;
import thuctap.thudo.fileexploerexample.R;
import thuctap.thudo.fragment.FileListFragment;
import thuctap.thudo.models.FilesModels;
import thuctap.thudo.adapter.FileListAdapter;
import thuctap.thudo.util.JsonHandle;
import thuctap.thudo.fragment.DownloadFragment;
import thuctap.thudo.adapter.DownloadListAdapter.ViewHolder;

public class FileExploer extends Activity implements DownloadFragment.OnFragmentViewCreated{
    private ListView exploerListView = null;
    private ListView downloadListView = null;
    private List<FilesModels> filemodels  = new ArrayList<FilesModels>();
    private List<FilesModels> fileDownload =  new ArrayList<FilesModels>();
    private FileListAdapter adapter;
    private DownloadListAdapter downloadAdapter  = null;
    private Socket clientSocket = null;
    public static final int SERVERPORT = 1234;
    public static final String SERVER_IP = "10.0.2.2";
    private Thread myConnThread = null;
    private  BufferedReader in = null;
    private PrintWriter out = null;
    private Context mContext  ;
    private String path = ".";

    TableLayout table1 ;

    private final Handler myHandler  = new Handler(){

        public void handleMessage(Message msg) {
           initFileList();
        }
    };
    private  final  Handler startDownloadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

        }
    };


    @Override
    public void onFragmentViewCreated(View view) {
        initDownloadList(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this.getApplicationContext();
        this.myConnThread = new Thread(new ConnThread(this.clientSocket , in ,out , "" ));
        this.myConnThread.start();
        prepareActionBar();
        mContext = getApplicationContext();
        table1 = (TableLayout) findViewById(R.id.tableLayout2);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.file_exploer,menu);
       return true;
    }



    public void initFileList(){

        exploerListView = (ListView) findViewById(R.id.list);
        adapter  = new FileListAdapter(this, filemodels);
        exploerListView.setAdapter(adapter);
        exploerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (exploerListView.isClickable()){
                    FilesModels file = (FilesModels) exploerListView.getAdapter().getItem(i);
                    if (file.getIsDir()){
                        path = path + "/" + file.getName();
                        myConnThread = new Thread(new ConnThread(clientSocket , in , out , path));
                        myConnThread.start();
                    }else{
                        String pathDownload =  path + "/" + file.getName();
                        int position = fileDownload.size();
                        fileDownload.add(file);
                        getActionBar().setSelectedNavigationItem(1);
                        if (downloadAdapter != null){
                             downloadAdapter.notifyDataSetChanged();
                            downloadListView.invalidate();
                            Log.i("getdownloadcount" , String.valueOf(downloadListView.getChildCount()));
                          //  View v = downloadListView.getChildAt(position);
                            Thread downloadThread  = new Thread(new DownloadThread(file ,pathDownload , position ));
                            downloadThread.start();

                        }else {
                            Thread downloadThread  = new Thread(new DownloadThread(file ,pathDownload ,position));
                            downloadThread.start();
                        }

                    }
                }
            }
        });
    }

    public void initDownloadList(View v){

        downloadListView = (ListView)v.findViewById(R.id.download_list);
        downloadAdapter = new DownloadListAdapter(this, fileDownload);
        downloadListView.setAdapter(downloadAdapter);


    }
    private void updateDownloadList(FilesModels file){
        fileDownload.add(file);
        downloadAdapter.notifyDataSetChanged();
    }

    private void prepareActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab fileExplorerTab = actionBar.newTab().setText("File Explorer").setTag("fileExplorer");
        ActionBar.Tab downloadTab = actionBar.newTab().setText("Download").setTag("download");
        Fragment downloadFragment = new DownloadFragment();
        Fragment fileExplorerFragment =  new FileListFragment();
        downloadFragment.setRetainInstance(true);
        fileExplorerFragment.setRetainInstance(true);
        downloadTab.setTabListener( new MyTabsListener(downloadFragment));
        fileExplorerTab.setTabListener(new MyTabsListener(fileExplorerFragment));
        actionBar.addTab(fileExplorerTab);
        actionBar.addTab(downloadTab);


       // mSpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, gotoLocations);
        //actionBar.setListNavigationCallbacks(mSpinnerAdapter, getActionbarListener(actionBar));

    }


    class MyTabsListener implements ActionBar.TabListener {
        public Fragment fragment;

        public MyTabsListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            /*
            FragmentManager fm = getFragmentManager();
            if(fm.findFragmentByTag(tab.getTag().toString()) == null){
                ft = fm.beginTransaction();
                ft.add(R.id.fragment_container,fragment ,tab.getTag().toString());
                ft.addToBackStack("BackStack" + tab.getTag().toString());
            }
            else{
                Fragment frag = fm.findFragmentByTag(tab.getTag().toString());
                ft.show(frag);

            }
*/
            if(fragment.isAdded()){
                ft.show(fragment);
            }else{

                ft.add(R.id.fragment_container ,fragment );




        }

        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
           // Fragment frag = getFragmentManager().findFragmentByTag(tab.getTag().toString());
            ft.hide(fragment);
           // ft.remove(fragment);
        }

    }

    private ActionBar.OnNavigationListener getActionbarListener(final ActionBar actionBar) {
        return new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {

                int selectedIndex = actionBar.getSelectedNavigationIndex();

                if(selectedIndex == 0)
                {
                    return false;
                }
                switch (selectedIndex) {


                }


                return true;
            }

        };
    }





    private void updateData(List<FilesModels> list){
        this.filemodels = list;
    }

    private  void  updateDownloadData (List<FilesModels> list){
        this.fileDownload = list;
    }

    class ConnThread implements  Runnable {
        Socket mainClientSocket = null;
        BufferedReader inThread =  null;
        PrintWriter outThread = null;
        JsonHandle handle;
        List<FilesModels> list;
        String getStr =  "get|";



        public ConnThread(Socket socket , BufferedReader in , PrintWriter out  , String path){
            this.mainClientSocket = socket;
            this.inThread = in;
            this.outThread = out;
            this.getStr  = this.getStr + path;
        }
        public void run(){
            try{
                if (this.mainClientSocket == null){
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    this.mainClientSocket =  new Socket(serverAddr , SERVERPORT);
                    this.inThread = new BufferedReader(new InputStreamReader(this.mainClientSocket.getInputStream()));
                    this.outThread = new PrintWriter((mainClientSocket.getOutputStream()) , true);
                }


                outThread.println(this.getStr);
                String inputStr = inThread.readLine();
                this.handle = new JsonHandle(inputStr);
                this.list = this.handle.jsonHandle();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateData(list);
                        clientSocket = mainClientSocket;
                        in = inThread;
                        out = outThread;

                    }
                });
                Message msg = myHandler.obtainMessage();
                myHandler.sendMessage(msg);


            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    class DownloadThread implements Runnable{
        Socket socket ;
        InputStream in ;
        PrintWriter out;
        String fileName;
        String filePath;
        int position;
        long fileSize;
        ProgressBar progress = null;
        File path;
        File fileEnv = Environment.getExternalStorageDirectory();


        public  DownloadThread(FilesModels file ,  String filePath , View v){
            this.fileName = file.getName();
            this.fileSize = file.getSize();
            this.filePath = filePath;
            ViewHolder holder = (ViewHolder)v.getTag();
            this.progress = holder.resBar;
            this.path = new File(fileEnv.getAbsolutePath() + "/" + fileName);
            int i = 1;

            while (this.path.exists()){
                this.path  = new File((fileEnv.getAbsolutePath() + "/" + fileName + "_" + String.valueOf(i)));
                i++;
            }
            Log.i("path" , path.getAbsolutePath());

        }

        public  DownloadThread(FilesModels file ,  String filePath , int position){
            this.fileName = file.getName();
            this.fileSize = file.getSize();
            this.filePath = filePath;
            this.position = position;
            this.path = new File(fileEnv.getAbsolutePath() + "/" + fileName);
            int i = 1;
            while (this.path.exists()){
                this.path  = new File((fileEnv.getAbsolutePath() + "/" + fileName + "_" + String.valueOf(i)));
                i++;
            }
            Log.i("path" , path.getAbsolutePath());

        }



        public void run(){
            try{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // Toast.makeText(mContext , "start download file " + fileName, Toast.LENGTH_SHORT).show() ;
                    }
                });

                Thread.sleep(1000);
                this.progress = (ProgressBar)downloadListView.getChildAt(position).findViewById(R.id.download_progressBar);
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                this.socket =  new Socket(serverAddr , SERVERPORT);
                this.in = socket.getInputStream();
                this.out = new PrintWriter((socket.getOutputStream()) , true);
                FileOutputStream fos = new FileOutputStream(this.path);
                out.println("download|" +filePath);
                long size = 0 ;
                int count = 0 ;

                byte[] buffer = new byte[4096];
                socket.setSoTimeout(2000);
                while ( size <= this.fileSize && (count = in.read(buffer)) >= 0  ){
                    fos.write(buffer , 0 , count);
                    size = size + count;
                    int progressStatus  = Integer.parseInt(String.valueOf(100*size/fileSize));
                    if (this.progress !=  null){
                        Log.i("progress status" , String.valueOf(size*100/fileSize));
                        progress.setProgress(progressStatus);
                    }




                }
                this.in.close();
                this.out.close();
                this.socket.close();
                Log.i("download", "download complete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "download file " + fileName + "complete", Toast.LENGTH_SHORT).show();
                        fileDownload.remove(position);
                        Log.i("size after download" , String.valueOf(fileDownload.size()));
                        downloadAdapter.notifyDataSetChanged();
                        downloadListView.invalidate();
                    }
                });
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (FileNotFoundException e){
                Toast.makeText(mContext,"khong the ghi file vao thu muc sdcard" ,  Toast.LENGTH_LONG).show();
            }catch (SocketTimeoutException e){
                Log.i("download" , "download complete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileDownload.remove(position);
                        Log.i("size after download" , String.valueOf(fileDownload.size()));
                        downloadAdapter.notifyDataSetChanged();
                        downloadListView.invalidate();
                        Toast.makeText(mContext, "download file " + fileName + " complete", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    
}
