package be.thalarion.eventman;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.IOException;

import be.thalarion.eventman.api.APIException;
import be.thalarion.eventman.models.Model;
import be.thalarion.eventman.models.Person;

public class ShowPersonActivity extends ActionBarActivity {

    private TextView name, email, birthDate;
    private ImageView avatar;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_person);

        Bundle data = getIntent().getExtras();
        this.person = Parcels.unwrap(data.getParcelable("person"));

        this.name = ((TextView) this.findViewById(R.id.person_name));
        this.email = ((TextView) this.findViewById(R.id.person_email));
        this.birthDate = ((TextView) this.findViewById(R.id.person_birthdate));
        this.avatar = ((ImageView) this.findViewById(R.id.person_avatar));

        // TODO: null-catching
        this.name.setText(person.getName());
        this.email.setText(person.getEmail());
        this.birthDate.setText(person.getBirthDate().toString());

        Picasso.with(this)
                .load(person.getAvatar(getResources().getDimensionPixelSize(R.dimen.avatar_large)))
                .into(avatar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.person, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.action_edit_person:
                intent = new Intent(this,EditPersonActivity.class);
                intent.putExtra("person", Parcels.wrap(this.person));
                intent.putExtra("action", Model.ACTION.EDIT);
                startActivity(intent);

                break;
            case R.id.action_discard_person:

                new AsyncTask<Void, Void, Exception>() {
                    @Override
                    protected Exception doInBackground(Void... params) {
                        try {
                            person.destroy();
                            // Allow garbage collection
                            person = null;
                        } catch (APIException | IOException e) {
                            e.printStackTrace();
                            return e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        if(e == null) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.info_text_destroy), Toast.LENGTH_LONG).show();
                        } else Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }.execute();

                //TODO: load the peopleFragment here
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                break;
            default:
                return false;
        }
        return true;
    }
}
