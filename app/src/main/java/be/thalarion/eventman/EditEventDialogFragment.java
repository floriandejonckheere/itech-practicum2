package be.thalarion.eventman;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import be.thalarion.eventman.api.APIException;
import be.thalarion.eventman.api.ErrorHandler;
import be.thalarion.eventman.models.Event;
import be.thalarion.eventman.models.Model;
import be.thalarion.eventman.models.Person;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditEventDialogFragment extends EditDialogFragment
        implements View.OnClickListener {

    private Event event;
    private EditText field_title,field_description;
    private TextView field_start_date,field_start_time,field_end_date,field_end_time;

    public EditEventDialogFragment() {
        // Required empty public constructor
    }

    public static EditEventDialogFragment newInstance(Event event, Model.ACTION action) {

        EditEventDialogFragment f = new EditEventDialogFragment();
        Bundle bundle = new Bundle();
        if (event != null) {
            bundle.putParcelable("event", Parcels.wrap(event));
        }

        bundle.putSerializable("action", action);

        f.setArguments(bundle);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //((ActionBarActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_event_dialog, container, false);

        this.field_title = ((EditText) rootView.findViewById(R.id.field_title));
        this.field_description = ((EditText) rootView.findViewById(R.id.field_description));
        this.field_start_date = ((TextView) rootView.findViewById(R.id.field_start_date));
        this.field_end_date = ((TextView) rootView.findViewById(R.id.field_end_date));
        this.field_start_time = ((TextView) rootView.findViewById(R.id.field_start_time));
        this.field_end_time = ((TextView) rootView.findViewById(R.id.field_end_time));

        Bundle data = getArguments();
        if (data.getSerializable("action") == Model.ACTION.EDIT) {
            this.event = Parcels.unwrap(data.getParcelable("event"));


            this.field_title.setText(event.getTitle());
            this.field_description.setText(event.getDescription());
            //TODO: implement null handling
            //TODO: look how the dateformat affects everything
            this.field_start_date.setText(Event.formatDate.format(event.getStartDate()));
            this.field_start_time.setText(Event.formatTime.format(event.getStartDate()));
            //TODO: implement null handling
            this.field_end_date.setText(Event.formatDate.format(event.getEndDate()));
            this.field_end_time.setText(Event.formatTime.format(event.getEndDate()));
        } else if (data.getSerializable("action") == Model.ACTION.NEW) {
            this.event = new Event();
        }

        this.field_start_date.setOnClickListener(this);
        this.field_end_date.setOnClickListener(this);
        this.field_start_time.setOnClickListener(this);
        this.field_end_time.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_save:
                new AsyncTask<Void, Void, Exception>() {
                    @Override
                    protected Exception doInBackground(Void... params) {
                        String title = field_title.getText().toString();
                        String description = field_description.getText().toString();
                        Date startdate,enddate,starttime,endtime;
                        try {
                            startdate = Event.format.parse(field_start_date.getText().toString());
                            enddate = Event.format.parse(field_end_date.getText().toString());
                            starttime = Event.format.parse(field_start_time.getText().toString());
                            endtime = Event.format.parse(field_end_time.getText().toString());
                        } catch (ParseException e) {
                            return e; //TODO: what dafuq dit mag hier niet returnn wi. Zet dan een default waarde als datum en print af dat er een fucking error is. Lul.
                        }
                        //new Event has been created if action==ACTION.NEW
                        event.setTitle(title);
                        event.setDescription(description);
                        event.setStartDate(startdate);
                        event.setEndDate(enddate);

                        try {
                            event.syncModelToNetwork();
                        } catch (IOException | APIException e) {
                            return e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        if (e == null) {
                            //TODO first: crasht hier voor no apparent reason
                            // Toast.makeText(getActivity(), getResources().getText(R.string.info_text_edit), Toast.LENGTH_LONG).show();
                        } else {//ErrorHandler.announce(getActivity(), e);
                        } //TODO: crash
                    }
                }.execute();

                EventsFragment f = new EventsFragment();
                ((MaterialNavigationDrawer) this.getActivity()).setFragmentChild(f, this.getResources().getString(R.string.title_events));

                break;

            default:
                return false;
        }
        return true;
    }


/*
    public void onDateSet(DatePicker view,View target, int year, int monthOfYear, int dayOfMonth) {

        ((TextView) this.getView().findViewById(R.id.field_start_date)).setText(
                new StringBuilder().append(year).append("-")
                        .append(monthOfYear).append("-").append(dayOfMonth));
    }


    public void onTimeSet(TimePicker view,View target, int hourOfDay, int minute) {
        ((TextView) this.getView().findViewById(R.id.field_start_time)).setText(
                new StringBuilder().append(hourOfDay).append(":").append(minute));





    }*/

    @Override
    public void onClick(View v) { // Parameter v stands for the view that was clicked.
        String c = v.getContentDescription().toString();
        if(c.equals("Date_Start") || c.equals("Date_End")){
            DialogFragment f = DateDialogFragment.newInstance(this,v);

            f.show(getActivity().getSupportFragmentManager(), "datePicker");
        }else if(c.equals("Time_Start") || c.equals("Time_End")){
            DialogFragment f = TimeDialogFragment.newInstance(this,v);

            f.show(getActivity().getSupportFragmentManager(), "timePicker");
        }


    }
}
