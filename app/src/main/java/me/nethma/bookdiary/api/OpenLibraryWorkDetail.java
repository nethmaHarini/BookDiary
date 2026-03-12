package me.nethma.bookdiary.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Full works detail from the Open Library /works/{id}.json endpoint */
public class OpenLibraryWorkDetail {

    @SerializedName("key")
    public String key;

    @SerializedName("title")
    public String title;

    /** Description can be a String or an object {type, value} — handle both */
    @SerializedName("description")
    public Object descriptionRaw;

    @SerializedName("subjects")
    public List<String> subjects;

    @SerializedName("first_publish_date")
    public String firstPublishDate;

    @SerializedName("covers")
    public List<Integer> covers;

    /** Returns plain-text description, handling both String and object forms */
    public String getDescription() {
        if (descriptionRaw == null) return null;
        if (descriptionRaw instanceof String) {
            return (String) descriptionRaw;
        }
        // Gson may parse {type, value} as a LinkedTreeMap
        if (descriptionRaw instanceof java.util.Map) {
            Object val = ((java.util.Map<?, ?>) descriptionRaw).get("value");
            return val != null ? val.toString() : null;
        }
        return descriptionRaw.toString();
    }

    /** Returns first cover URL (large) or null */
    public String getCoverUrl() {
        if (covers != null && !covers.isEmpty() && covers.get(0) > 0) {
            return "https://covers.openlibrary.org/b/id/" + covers.get(0) + "-L.jpg";
        }
        return null;
    }
}

