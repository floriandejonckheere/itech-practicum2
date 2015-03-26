package be.thalarion.eventman.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.thalarion.eventman.R;
import be.thalarion.eventman.api.API;
import be.thalarion.eventman.api.APIException;

/**
 * WARNING: this class contains a lot of synchronous networked methods.
 * Updating the model should run in a separate thread.
 */
public class Event extends Model implements Parcelable {

    private String title, description;
    private Date startDate, endDate;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //                                                      2014-08-04 T 18:00:00.000 Z

    /**
     * Event - create a new event
     * @param title
     * @param description
     * @param startDate
     * @param endDate
     * @throws IOException
     * @throws be.thalarion.eventman.api.APIException
     */
    public Event(String title, String description, Date startDate, Date endDate) throws IOException, APIException {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;

        JSONObject event = new JSONObject();
        try {
            event.put("title", this.title);
            event.put("description", this.description);
            event.put("start", format.format(this.startDate));
            event.put("end", format.format(this.endDate));

            JSONObject response = API.getInstance().create(
                    API.getInstance().resolve("events"),
                    event.toString()
            );
            this.resource = new URL(response.getString("url"));
        } catch (JSONException e) {
            throw new APIException(e);
        }
    }

    private Event(JSONObject json) throws APIException {
        try {
            if(!json.isNull("title")) this.title = json.getString("title");
            if(!json.isNull("description")) this.description = json.getString("description");
            this.resource = new URL(json.getString("url"));

            try {
                this.startDate = format.parse(json.getString("start"));
                this.endDate = format.parse(json.getString("end"));
            } catch (ParseException e) {
                // Thrown if either date is null
            }
        } catch (JSONException | MalformedURLException e) {
            throw new APIException(e);
        }
    }


    /**
     * API Methods
     *
     */
    public static List<Event> findAll() throws IOException, APIException {
        List<Event> events = new ArrayList<>();

        URL resourceRoot = null;
        try {
            resourceRoot = API.getInstance().resolve("events");
        } catch (Exception e) {
            throw new APIException(e);
        }

        // Fetch /events
        JSONObject json = API.getInstance().fetch(resourceRoot);
        try {
            JSONArray jsonArray = json.getJSONArray("events");
            for(int i = 0; i < jsonArray.length(); i++) {
                // Fetch /events/{id}
                JSONObject jsonEvent = API.getInstance().fetch(
                        new URL(
                                jsonArray.getJSONObject(i).getString("url")
                        ));
                events.add(new Event(jsonEvent));
            }
        } catch (JSONException e) {
            throw new APIException(e);
        }

        return events;
    }

    // Getters
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }

    // Setters
    public void setTitle(String title) throws IOException, APIException {
        this.title = title;
        updateField("title", this.title);
    }

    public void setDescription(String description) throws IOException, APIException {
        this.description = description;
        updateField("description", this.description);
    }

    public void setStartDate(Date startDate) throws IOException, APIException {
        this.startDate = startDate;
        updateField("start", format.format(this.startDate));
    }

    public void setEndDate(Date endDate) throws IOException, APIException {
        this.startDate = endDate;
        updateField("end", format.format(this.endDate));
    }

    /**
     * update - Sync model from network to memory
     * @throws IOException
     * @throws APIException
     */
    public void update() throws IOException, APIException {
        this.title = fetchField("title");
        this.description = fetchField("description");

        try {
            this.startDate = format.parse(fetchField("start"));
            this.startDate = format.parse(fetchField("end"));
        } catch (ParseException e) { }
    }

    /**
     * hash - Get icon-presentable hashcode (one or two characters)
     * @param key
     * @return
     */
    public static String hash(String key) {
        String[] split = key.split(" ");
        if(split.length >= 2) {
            return split[0].substring(0, 1).toUpperCase() + split[1].substring(0, 1).toLowerCase();
        } else return split[0].substring(0, 1).toUpperCase();
    }

    private static int[] colors = {
            R.color.md_red,
            R.color.md_pink,
            R.color.md_purple,
            R.color.md_indigo,
            R.color.md_blue,
            R.color.md_green,
            R.color.md_lime,
            R.color.md_yellow,
            R.color.md_amber,
            R.color.md_deep_orange
    };

    /**
     * colorFromString - Synthesize a color from a string
     * @param key
     * @return Color resource ID
     */
    public static int colorFromString(String key) {
        int code = key.charAt(0);
        if(key.length() >= 2)
            code += key.charAt(1);

        code = code % colors.length;
        return colors[code];
    }

    /**
     * Parcelling part
     */
    public Event(Parcel in){
        String[] data = new String[3];
        Date startdate, enddate;

        in.readStringArray(data);
        startdate = new Date(in.readLong());
        enddate = new Date(in.readLong());
        this.title = data[0];
        this.description= data[1];
        try {
            this.resource = new URL(data[2]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.startDate = startdate;
        this.endDate = enddate;

    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.title, this.description,this.resource.toString()});

        //TODO: eigenlijk zou dit toch niet mogelijk moeten zijn? Je kunt toch geen event posten zonder begin/einddatum?
        if(this.startDate!=null) dest.writeLong(this.startDate.getTime());
        else dest.writeLong(new Date().getTime());

        if(this.endDate!=null) dest.writeLong(this.endDate.getTime());
        else dest.writeLong(new Date().getTime());
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
