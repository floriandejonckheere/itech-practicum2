package be.thalarion.eventman.fragments.event;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;

import java.io.IOException;
import java.net.URI;

import be.thalarion.eventman.R;
import be.thalarion.eventman.api.APIException;
import be.thalarion.eventman.api.ErrorHandler;
import be.thalarion.eventman.cache.Cache;
import be.thalarion.eventman.models.Event;
import be.thalarion.eventman.models.Model;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class EventTab extends android.support.v4.app.Fragment {

    private TextView title, description, startDate, endDate;
    private ImageView banner;
    private Event event;

    public static EventTab newInstance(URI uri) {
        EventTab fragment = new EventTab();

        Bundle bundle = new Bundle();
        bundle.putSerializable("uri", uri);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_event, container, false);

        // ActionBar
        setHasOptionsMenu(false);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

        this.title = ((TextView) rootView.findViewById(R.id.event_title));
        this.description = ((TextView) rootView.findViewById(R.id.event_description));
        this.startDate = ((TextView) rootView.findViewById(R.id.event_startdate));
        this.endDate = ((TextView) rootView.findViewById(R.id.event_enddate));
        this.banner = ((ImageView) rootView.findViewById(R.id.event_banner));

        final Context context = this.getActivity();
        new AsyncTask<Bundle, Exception, Event>() {
            @Override
            protected Event doInBackground(Bundle... params) {
                Event event = null;
                Bundle data = params[0];
                try {
                    event = Cache.find(Event.class, (URI) data.getSerializable("uri"));
                } catch (IOException | APIException e) {
                    publishProgress(e);
                }
                return event;
            }

            @Override
            protected void onProgressUpdate(Exception... values) {
                ErrorHandler.announce(context, values[0]);
            }

            @Override
            protected void onPostExecute(Event ev) {
                event = ev;

                title.setText(event.getFormattedTitle(context));
                description.setText(event.getFormattedDescription(context));
                startDate.setText(event.getFormattedStartDate(context, Event.format));
                endDate.setText(event.getFormattedEndDate(context, Event.format));
                String color = Event.hash(event.getFormattedTitle(context));
                TextDrawable drawable = TextDrawable.builder().buildRect(
                        color,
                        context.getResources().getColor(Event.colorFromString(color))
                );
                banner.setImageDrawable(drawable);
            }
        }.execute(getArguments());

        return rootView;
    }

}
