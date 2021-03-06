package be.thalarion.eventman.fragments.person;


import android.content.Context;
import android.graphics.Bitmap;
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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.net.URI;

import be.thalarion.eventman.R;
import be.thalarion.eventman.api.APIException;
import be.thalarion.eventman.api.ErrorHandler;
import be.thalarion.eventman.cache.Cache;
import be.thalarion.eventman.events.BusEvent;
import be.thalarion.eventman.events.PersonBusEvent;
import be.thalarion.eventman.models.Model;
import be.thalarion.eventman.models.Person;
import de.greenrobot.event.EventBus;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;


public class ShowPersonFragment extends android.support.v4.app.Fragment {

    private TextView name, email, birthDate;
    private ImageView avatar;
    private Person person;

    public ShowPersonFragment() {
        // Required empty public constructor
    }

    public static ShowPersonFragment newInstance(URI personUri) {
        ShowPersonFragment fragment = new ShowPersonFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("personUri", personUri);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_person, container, false);

        // ActionBar
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);


        this.name = (TextView) rootView.findViewById(R.id.person_name);
        this.email = (TextView) rootView.findViewById(R.id.person_email);
        this.birthDate = (TextView) rootView.findViewById(R.id.person_birthdate);
        this.avatar = (ImageView) rootView.findViewById(R.id.person_avatar);

        final Context context = getActivity();
        final Bundle data = getArguments();
        new AsyncTask<Void, Exception, Person>() {
            @Override
            protected Person doInBackground(Void... params) {
                try {
                    return Cache.find(Person.class, (URI) data.getSerializable("personUri"));
                } catch (IOException | APIException e) {
                    publishProgress(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Person p) {
                person = p;
                if (p.getName() != null)
                    name.setText(person.getName());
                else
                    name.setText(R.string.error_text_noname);

                if (p.getEmail() != null)
                    email.setText(person.getEmail());
                else
                    email.setText(R.string.error_text_noemail);

                if (p.getBirthDate() != null)
                    birthDate.setText(Person.format.format(person.getBirthDate()));
                else
                    birthDate.setText(R.string.error_text_nobirthdate);

                ImageLoader.getInstance().loadImage(p.getAvatar(Person.AVATAR.LARGE), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        avatar.setImageBitmap(loadedImage);
                    }
                });

            }
            @Override
            protected void onProgressUpdate(Exception... values) {
                ErrorHandler.announce(context, values[0]);
            }
        }.execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_discard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                EditPersonDialogFragment editPersonFrag = EditPersonDialogFragment.newInstance(
                        this.person.getResource(),
                        Model.ACTION.EDIT);

                ((MaterialNavigationDrawer) this.getActivity()).setFragmentChild(editPersonFrag, this.getActivity().getResources().getString(R.string.title_edit_person));
                return true;
            case R.id.action_discard:
                final Context context = getActivity();
                new AsyncTask<Void, Void, Exception>() {
                    @Override
                    protected Exception doInBackground(Void... params) {
                        try {
                            // Post deletion event
                            EventBus.getDefault().post(new PersonBusEvent(person, BusEvent.ACTION.DELETE));
                            person.destroy();
                            // Allow garbage collection
                            person = null;
                        } catch (APIException | IOException e) {
                            return e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        if (e == null) {
                            Toast.makeText(context, context.getResources().getText(R.string.info_text_destroy), Toast.LENGTH_LONG).show();
                        } else ErrorHandler.announce(context, e);
                    }
                }.execute();

                getActivity().onBackPressed();
                return true;
            default:
                return false;
        }
    }

}
