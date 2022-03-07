package app.com.trizesolutions.webprint;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
    private WebPrint app;
    private Button serverbtn;
    private TextView stattxt;
    private EditText sourceport;
    private ListView aclListview;
    private CheckBox checkboxStart;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {

                AlertDialog.Builder authAlert = new AlertDialog.Builder(this);
                authAlert.setTitle("Notification");
                authAlert.setMessage("Allow this app to appear on top of other apps you are using.");
                authAlert.setNeutralButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.fromParts("package" , getPackageName(), null);
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                });
                authAlert.setCancelable(false);
                authAlert.create();
                authAlert.show();


            }else{
                return true;
            }
        }else{
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        serverbtn = (Button) findViewById(R.id.serverbtn);
        stattxt = (TextView) findViewById(R.id.stattxt);
        app = (WebPrint) getApplicationContext();
        sourceport = (EditText) findViewById(R.id.sourceport);
        sourceport.setText(app.preferences.getString("prefsourceport", "8080"));
        sourceport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                app.preferences.edit().putString("prefsourceport", sourceport.getText().toString()).apply();
            }
        });

        //permission
        checkPermission();
        //Start WebPrint On System start
        checkboxStart = findViewById(R.id.checkbox_start);
        checkboxStart.setChecked(app.preferences.getBoolean("prefStartYn", true));

        boolean serverstarted = isServiceRunning(RelayService.class);
        setButton(serverstarted);

        aclListview = (ListView) findViewById(R.id.acllist);
        refreshListview();
        aclListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String origin = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.remove_domain)
                        .setMessage(R.string.remove_domain_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                app.accessControl.remove(origin);
                                refreshListview();
                            }
                        })
                        .show();
            }
        });
        TextView emptyView = new TextView(this);
        emptyView.setText(R.string.no_allowed_domains);
        aclListview.setEmptyView(emptyView);
    }

    public void onResume(){
        super.onResume();
        refreshListview();
    }

    private void refreshListview(){
        aclListview.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, app.accessControl.getAcl()));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startService(){
        boolean chkPermission = checkPermission();
        if(chkPermission){
            Intent sintent = new Intent(MainActivity.this, RelayService.class);
            sintent.putExtra("sourceport", ((TextView) findViewById(R.id.sourceport)).getText().toString());
            startService(sintent);
            // set new button click
            setButton(true);
        }
    }

    public void stopService(){
        Intent sintent = new Intent(MainActivity.this, RelayService.class);
        stopService(sintent);
        // set new button click
        setButton(false);
    }

    private void setButton(boolean started){
        if (started){
            serverbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    stopService();
                }
            });
            serverbtn.setText(R.string.stop_server);
            stattxt.setText(R.string.server_running);
        } else {
            serverbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startService();
                }
            });
            serverbtn.setText(R.string.start_server);
            stattxt.setText(R.string.server_stopped);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void onCheckboxStartClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.checkbox_start:
                if (checked){
                    app.preferences.edit().putBoolean("prefStartYn", true).apply();
                }else{
                    app.preferences.edit().putBoolean("prefStartYn", false).apply();
                }
                break;
        }
    }
}
