package mx.itesm.imi.harmonator;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.triggertrap.seekarc.SeekArc;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private PdService pdService = null;
    private PdUiDispatcher dispatcher;

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            pdService = ((PdService.PdBinder)iBinder).getService();
            try {
                initPD();
                loadPatch();
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void initPD() throws IOException{
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate, 1, 2 , 10.0f);
        pdService.startAudio();
    }
    private void loadPatch() throws IOException{
        File dir = getFilesDir();
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.pdpatch), dir, true);
        File patchFile = new File(dir, "harmonatorv1.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }
    private void startPd() {
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }
    SeekBar seekBar, volseekBar, seekLive, seekTime, seekDamp;
    ToggleButton toggleButton;
    Spinner spinnerIntervalo, spinnerInpSel;
    SeekArc seekArcVol, seekArcArm;
    CheckBox checkBoxRev;
    Button playSample;
    TextView textV, textA;

//    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = (SeekBar) findViewById(R.id.seekBarTono);
        volseekBar = (SeekBar) findViewById(R.id.seekBarDelay);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        spinnerIntervalo = (Spinner) findViewById(R.id.spinnerIntervalos);
        spinnerInpSel = (Spinner) findViewById(R.id.spinnerInpSel);
        seekLive = (SeekBar) findViewById(R.id.seekRlive);
        seekTime = (SeekBar) findViewById(R.id.seekRtime);
        seekDamp = (SeekBar) findViewById(R.id.seekRdamp);
        seekArcVol = (SeekArc) findViewById(R.id.seekArcVol);
        seekArcArm = (SeekArc) findViewById(R.id.seekArcArm);
        checkBoxRev = (CheckBox) findViewById(R.id.rev);
        playSample = (Button) findViewById(R.id.playSample);
        textA = (TextView) findViewById(R.id.textArm);
        textV = (TextView) findViewById(R.id.textVol);
//Live permission request
        requestAudioPermissions();

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

        ArrayAdapter<CharSequence> adapterInt = ArrayAdapter.createFromResource(this,
                R.array.intervalos, android.R.layout.simple_spinner_item);
        adapterInt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntervalo.setAdapter(adapterInt);

        ArrayAdapter<CharSequence> adapterSel = ArrayAdapter.createFromResource(this,
                R.array.inputselect, android.R.layout.simple_spinner_item);
        adapterSel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInpSel.setAdapter(adapterSel);

        spinnerIntervalo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("Item Selected", "index: "+i);
                PdBase.sendFloat("inter", i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerInpSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PdBase.sendFloat("inpsel", i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        seekArcVol.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                float vol = ((float) progress)/100;
                textV.setText(String.valueOf(vol));
                PdBase.sendFloat("volarm", vol);

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        seekArcArm.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                float armvol = ((float) progress)/100;
                textA.setText(String.valueOf(armvol));
                PdBase.sendFloat("volgral", armvol);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PdBase.sendFloat("tonopuro", i);
                Log.v("tono puro", "midi es: "+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                int del = i;
                if(del < 150){
                    del = 150;
                }

                PdBase.sendFloat("del", del);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekLive.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PdBase.sendFloat("rlive", i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PdBase.sendFloat("rtime", i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekDamp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PdBase.sendFloat("rdamp", i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkBoxRev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    PdBase.sendFloat("rev", 1f);
                }else{
                    PdBase.sendFloat("rev", 0f);
                }
            }
        });

        playSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                PdBase.sendBang("playsamp");
                PdBase.sendFloat("playsamp", 1f);
            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    PdBase.sendFloat("stmod", 1f);
                } else {
                    PdBase.sendFloat("stmod", 0f);
                }
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(pdConnection);
    }

    //Requesting run-time permissions

    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            startPd();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startPd();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
