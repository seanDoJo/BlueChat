package apps.play.self.bluechat;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends FragmentActivity implements ItemFragment.OnFragmentInteractionListener, Chat.onSendListener {
    private BluetoothAdapter mBluetooth;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> array;
    private ArrayList<BluetoothDevice> devices;
    private BluetoothSocket socket;
    int REQUEST_ENABLE_BT = 123;
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ItemFragment itemFrag = (ItemFragment) getSupportFragmentManager().findFragmentById(R.id.item_fragment);
                if(device.getName() != null)itemFrag.setText(device.getName() + "--> " + device.getAddress());
                devices.add(device);
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(devices.size() == 0) {
                    ItemFragment itemFrag = (ItemFragment) getSupportFragmentManager().findFragmentById(R.id.item_fragment);
                    itemFrag.setText("NO DEVICES FOUND!");
                }
            }
            else if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                ItemFragment itemFrag = (ItemFragment) getSupportFragmentManager().findFragmentById(R.id.item_fragment);
                itemFrag.setText("Connected!");
            }
        }
    };
    Handler connectionHandler = new Handler(){
        public void handleMessage(Message m){
            if(m.what == 1){
                Chat chatFrag = (Chat) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
                byte[] data = (byte[])m.obj;
                chatFrag.addMessage(new String(data));
            }
        }
    };
    private class BTServer extends Thread {
        private BluetoothServerSocket mmServerSocket;

        public BTServer() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetooth.listenUsingRfcommWithServiceRecord("BlueChat", UUID.fromString("e50d0cd0-134f-11e5-b939-0800200c9a66"));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run(){
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    myConnection = new BTConnection();
                    myConnection.start();
                if (socket != null) {
                    //connectionHandler.obtainMessage(2, 0, -1, null).sendToTarget();
                    mmServerSocket.close();
                    break;
                }
                } catch (IOException e) {
                    break;
                }
            }
            return;
        }

        public void cancel(){
            try{
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }
    private class BTClient extends Thread {
        public BTClient(BluetoothDevice newDevice){
            BluetoothSocket tmp = null;
            try {
                tmp = newDevice.createRfcommSocketToServiceRecord(UUID.fromString("e50d0cd0-134f-11e5-b939-0800200c9a66"));
            } catch (IOException e) { }
            socket = tmp;
        }

        public void run() {
            mBluetooth.cancelDiscovery();

            try {
                socket.connect();
                myConnection = new BTConnection();
                myConnection.start();
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) { }
                return;
            }
            //connectionHandler.obtainMessage(2, 0, -1, null).sendToTarget();
            return;
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }

    }

    private class BTConnection extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public BTConnection() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    connectionHandler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                    buffer = new byte[buffer.length];
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }
    private BTServer myServer;
    private BTClient myClient;
    private BTConnection myConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(btReceiver, filter);

        array = new ArrayList<String>();
        devices = new ArrayList<BluetoothDevice>();

        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(mBluetooth == null) System.exit(1);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);

        myServer = new BTServer();
        myServer.start();

        mBluetooth.startDiscovery();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

    public void onFragmentInteraction(int index){
        myServer.cancel();
        myClient = new BTClient(devices.get(index));
        myClient.start();
        Log.i("print", "Position:" + index);
        Chat myChat = (Chat) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
        myChat.attachArray(index);
    }

    public void onSendListener(String message){
        if(myConnection != null) {
            myConnection.write(message.getBytes());
        }
    }
}