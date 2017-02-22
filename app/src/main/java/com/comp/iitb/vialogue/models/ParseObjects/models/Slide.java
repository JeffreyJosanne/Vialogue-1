package com.comp.iitb.vialogue.models.ParseObjects.models;

import com.parse.ParseClassName;

import java.util.ArrayList;
import java.util.List;

import com.comp.iitb.vialogue.models.ParseObjects.models.json.BaseJsonClass;
import com.comp.iitb.vialogue.models.ParseObjects.models.json.SlideJson;

/**
 * Created by ironstein on 16/02/17.
 */

@ParseClassName("Slide")
public class Slide extends BaseParseClass {

    private static class Fields {
        public static final String

        PROJECT_SLIDE_ID = "project_slide_id",
        HYPERLINKS = "hyperlinks";
    }

    // default constructor required by Parse
    // DO NOT USE THIS CONSTRUCTOR (ONLY FOR USE BY PARSE)
    // USE THE OTHER CONSTRUCTOR THAT REQUIRES PARAMETERS DURING
    // INSTANTIATING THE OBJECT
    public Slide() {}

    public Slide(int projectSlideId) {
        setProjectSlideId(projectSlideId);
    }

    public Integer getProjectSlideId() {
        return getInt(Fields.PROJECT_SLIDE_ID);
    }

    private void setProjectSlideId(Integer projectSlideId) {
        put(Fields.PROJECT_SLIDE_ID, projectSlideId);
    }

    public ArrayList<String> getHyperlinks() {
        return (ArrayList) getList(Fields.HYPERLINKS);
    }

    private void setHyperlinks(List<String> hyperlinks) {
        put(Fields.HYPERLINKS, hyperlinks);
    }

    public void addHyperlink(String slideId) {
        // TODO decide if validation is required here to check
        // if the slideId passed corresponds to a valid slide in the Slide class in the database

        ArrayList<String> hyperlinks = getHyperlinks();
        hyperlinks.add(slideId);
        setHyperlinks(hyperlinks);
    }

    public void deleteHyperlink(int slideIndex) {
        ArrayList<String> hyperlinks = getHyperlinks();
        if(slideIndex >= hyperlinks.size()) {
            throw new ArrayIndexOutOfBoundsException("slideIndex greater than length of {hyperlinks} array");
        }

        hyperlinks.remove(slideIndex);
        setHyperlinks(hyperlinks);
    }

}
