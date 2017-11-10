package mx.itesm.imi.harmonator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestRecordAudioPermission() {

        String requiredPermission = Manifest.permission.RECORD_AUDIO;

        // If the user previously denied this permission then show a message explaining why
        // this permission is needed
        if (this.getBaseContext().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {

        } else {

            Toast.makeText(this.getBaseContext(), "This app needs to record audio through the microphone....", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{requiredPermission}, 101);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // This method is called when the  permissions are given
        }

    }

    private PdUiDispatcher dispatcher;
    private void initPD() throws IOException{
        int sampleRate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(sampleRate, 1, 2, 8, true);

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
    }
    SeekBar seekBar;
    SeekBar volseekBar;
    ToggleButton toggleButton;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestRecordAudioPermission();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        volseekBar = (SeekBar) findViewById(R.id.seekBar2);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        try {
            initPD();
            File dir = getFilesDir();
            IoUtils.extractZipResource(getResources().openRawResource(R.raw.adctest), dir, true);
            File patchFile = new File(dir, "adctest.pd");
            PdBase.openPatch(patchFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PdBase.sendFloat("modspeed", i);
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
                float vol = ((float) i)/100;
//                Log.v("Vol", "vol is: " + vol);
                PdBase.sendFloat("vol", vol);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
    protected void onResume() {
        super.onResume();
        if(PdAudio.isRunning()){
            PdAudio.release();
        }else{
            PdAudio.startAudio(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        PdAudio.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PdAudio.stopAudio();
    }
}
