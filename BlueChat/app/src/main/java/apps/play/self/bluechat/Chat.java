package apps.play.self.bluechat;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class Chat extends Fragment {

    private AbsListView mListView;
    onSendListener mListener;
    private CustomAdapter mAdapter;
    private ArrayList<String> myArray;
    private ArrayList<ArrayList<String>> mMessages;
    private ArrayList<Integer> userTrack;
    private int position = 0;
    private Button button;

    public void attachArray(int pos) {
        int messageSize = mMessages.size();

        if(pos >= messageSize) for(int i = messageSize; i <= pos; i++) mMessages.add(new ArrayList<String>());

        ArrayList<String> newArray = mMessages.get(pos);
        ArrayList<String> oldArray = new ArrayList<String>();

        if(myArray.size() > 0) for(int i = 0; i < myArray.size(); i++)oldArray.add(myArray.get(i));
        mMessages.set(position, oldArray);
        myArray.clear();
        if(newArray.size() > 0) for(int i = 0; i < newArray.size(); i++)myArray.add(newArray.get(i));
        mMessages.set(pos, new ArrayList<String>());

        position = pos;
        mAdapter.notifyDataSetChanged();

        // Enable button
        button.setEnabled(true);

    }

    public Chat(){
        // This just needs to be here
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessages = new ArrayList<ArrayList<String>>();
        myArray = new ArrayList<String>();
        userTrack = new ArrayList<Integer>();
        mAdapter = new CustomAdapter(getActivity(), myArray, userTrack);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mListView = (AbsListView) view.findViewById(R.id.listView2);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        button = (Button) view.findViewById(R.id.button_send);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) getView().findViewById(R.id.edit_message);
                String message = editText.getText().toString();
                Log.i("myArray", "myArray: " + myArray);
                myArray.add(message);
                mMessages.get(position).add(message);
                //userTrack.add(2);
                mAdapter.notifyDataSetChanged();
                mListener.onSendListener(message);
                editText.setText("");
                // collapse keyboard
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onSendListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSendListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void addMessage(String newMessage){
        myArray.add(newMessage);
        //userTrack.add(1);
        mAdapter.notifyDataSetChanged();
    }

    public interface onSendListener{
        public void onSendListener(String message);
    }

}
