package apps.play.self.bluechat;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class Chat extends Fragment {

    onSendListener mListener;

    public Chat(){
        // This just needs to be here
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
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

    public void onClickSend(View view){
        EditText editText = (EditText) getView().findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        mListener.onSendListener(message);
    }

    public interface onSendListener{
        public void onSendListener(String message);
    }

}
