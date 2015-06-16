package apps.play.self.bluechat;

/**
 * Created by seandonohoe on 6/16/15.
 */

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> messages;
    private final ArrayList<Integer> userTrack;

    public CustomAdapter(Context context, ArrayList<String> messages, ArrayList<Integer> userTrack) {
        super(context, -1, messages);
        this.context = context;
        this.messages = messages;
        this.userTrack = userTrack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int user = userTrack.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = null;
        if(user == 1){
            rowView = inflater.inflate(R.layout.row_layout_incoming, parent, false);
        }
        else if(user == 2){
            rowView = inflater.inflate(R.layout.row_layout_outgoing, parent, false);
        }
        TextView title = (TextView) rowView.findViewById(R.id.newText);
        title.setText(messages.get(position));
        return rowView;
    }
}
