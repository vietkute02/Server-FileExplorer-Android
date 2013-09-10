package thuctap.thudo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import thuctap.thudo.activity.FileExploer;
import thuctap.thudo.adapter.DownloadListAdapter;
import thuctap.thudo.fileexploerexample.R;
import thuctap.thudo.models.FilesModels;

/**
 * Created by vietanha34 on 9/9/13.
 */
public class DownloadFragment extends Fragment{

    private TextView downloadList ;
    public DownloadListAdapter adapter;
    private List<FilesModels> fileDownload = new ArrayList<FilesModels>();
    OnFragmentViewCreated mListener;

    public interface OnFragmentViewCreated{
        public void onFragmentViewCreated(View view);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentViewCreated) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement OnFragmentViewCreated");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    /*
        //View v = inflater.inflate(R.layout.donwloadfragment , container , false);
        fileDownload.add(new FilesModels("test" , 100000));

        downloadList = (TextView)getView().findViewById(R.id.download_list);
        downloadList.setText("after");
        adapter = new DownloadListAdapter(getActivity().getApplicationContext() , fileDownload);
        //downloadList.setAdapter(adapter);
*/
        return inflater.inflate(R.layout.donwloadfragment , container , false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentViewCreated(getView());
    }

    @Override
    public void onStart() {
        super.onStart();
//        fileDownload.add(new FilesModels("test" , 100000));

  //      downloadList = (TextView)getView().findViewById(R.id.download_list);
    //    downloadList.setText("after");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        fileDownload.add(new FilesModels("test" , 100000));

        downloadList = (ListView)getView().findViewById(R.id.download_list);
        adapter = new DownloadListAdapter(getActivity().getApplicationContext() , fileDownload);
        downloadList.setAdapter(adapter);
*/

    }
}
