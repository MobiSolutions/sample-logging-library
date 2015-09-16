package mobi.lab.sample_android_app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mobi.lab.sample_event_logging_library.Log;
import mobi.lab.sample_event_logging_library.data.LogEvent;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoggingSampleActivityFragment extends Fragment {

    private Button viewLogEventBtn;

    private int clickCounter = 0;

    public LoggingSampleActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logging_sample, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewLogEventBtn = (Button) view.findViewById(R.id.btn_log);
        viewLogEventBtn.setText(getString(R.string.label_log_btn, clickCounter));

        viewLogEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCounter++;
                Log.getInstance(this).m(getActivity(), (clickCounter % 2 == 0 ? LogEvent.TYPE_FOO : LogEvent.TYPE_BAR), "comment::" + clickCounter);
                viewLogEventBtn.setText(getString(R.string.label_log_btn, clickCounter));
            }
        });
    }

}
