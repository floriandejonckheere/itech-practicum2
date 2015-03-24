package be.thalarion.eventman.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import be.thalarion.eventman.R;
import be.thalarion.eventman.models.Person;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {

    private List<Person> dataSet;
    private Context context;

    public PeopleAdapter(Context context) {
        this.context = context;
        this.dataSet = new ArrayList<>();
    }

    public void setDataSet(List<Person> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView email;
        public ImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = ((TextView) itemView.findViewById(R.id.person_list_view_name));
            this.email = ((TextView) itemView.findViewById(R.id.person_list_view_email));
            this.avatar = ((ImageView) itemView.findViewById(R.id.person_list_view_avatar));
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PeopleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_person, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(dataSet.get(position).getName() != null) {
            holder.name.setTextAppearance(context, R.style.CardTitle);
            holder.name.setText(dataSet.get(position).getName());
        } else {
            holder.name.setTextAppearance(context, R.style.CardTitleMissing);
            holder.name.setText(R.string.error_text_noname);
        }
        if(dataSet.get(position).getEmail() != null) {
            holder.email.setTextAppearance(context, R.style.CardSubTitle);
            holder.email.setText(dataSet.get(position).getEmail());
        } else {
            holder.email.setTextAppearance(context, R.style.CardSubTitleMissing);
            holder.email.setText(R.string.error_text_noemail);
        }

        // TODO: find out if/how Picasso handles memory management on a large number of files
        Picasso.with(this.context)
                .load(dataSet.get(position).getAvatar())
                .into(holder.avatar);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(dataSet == null) return 0;
        return dataSet.size();
    }

}
