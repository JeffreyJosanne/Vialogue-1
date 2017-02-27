package com.comp.iitb.vialogue.models.ParseObjects.models;

import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Audio;
import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Image;
import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Question;
import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Video;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.BaseParseClass;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.BaseResourceClass;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.ParseObjectsCollection;
import com.parse.ParseClassName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ironstein on 16/02/17.
 */

@ParseClassName("Slide")
public class Slide extends BaseParseClass {

    private static class Fields {
        public static final String

        HYPERLINKS = "hyperlinks";
    }

    public static enum ResourceType {
        IMAGE,
        VIDEO,
        QUESTION,
        AUDIO
    }

    // default constructor required by Parse
    // DO NOT USE THIS CONSTRUCTOR (ONLY FOR USE BY PARSE)
    // USE THE OTHER CONSTRUCTOR THAT REQUIRES PARAMETERS DURING
    // INSTANTIATING THE OBJECT
    public Slide() {}

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
        // if hyperlinks array not initialized
        if(hyperlinks == null) {
            hyperlinks = new ArrayList<String>();
        }
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

    public void addResource(BaseResourceClass resource, ResourceType resourceType) throws Exception {
        if(resourceType == ResourceType.IMAGE) {
            addImage((Image) resource);
        } else if(resourceType == ResourceType.VIDEO) {
            addVideo((Video) resource);
        } else if(resourceType == ResourceType.QUESTION) {
            addQuestion((Question) resource);
        } else if(resourceType == ResourceType.AUDIO) {
            addAudio((Audio) resource);
        }
    }

    public void addImage(Image image) {
        ParseObjectsCollection<BaseResourceClass> childrenResources = getChildrenResources();
        childrenResources.removeAll();
        childrenResources.add(image);
        setChildrenResources(childrenResources);
    }

    public void addVideo(Video video) {
        ParseObjectsCollection<BaseResourceClass> childrenResources = getChildrenResources();
        childrenResources.removeAll();
        childrenResources.add(video);
        setChildrenResources(childrenResources);
    }

    public void addQuestion(Question question) {
        ParseObjectsCollection<BaseResourceClass> childrenResources = getChildrenResources();
        childrenResources.removeAll();
        childrenResources.add(question);
        setChildrenResources(childrenResources);
    }

    public void addAudio(Audio audio) throws Exception {
        ParseObjectsCollection<BaseResourceClass> childrenResources = getChildrenResources();
        if(childrenResources == null || childrenResources.size() == 0) {
            throw new Exception("trying to add audio to empty slide");
        }

        String childResourceParseClassName = ((BaseResourceClass) childrenResources.get(0)).getClassName();
        if(!(childResourceParseClassName.equals("Video") || childResourceParseClassName.equals("Image"))) {
            throw new Exception("trying to add audio to a slide of type : " + childResourceParseClassName);
        }

        ParseObjectsCollection<BaseResourceClass> child = new ParseObjectsCollection<BaseResourceClass>();
        child.add(audio);
        ((BaseResourceClass) childrenResources.get(0)).setChildrenResources(child);
        setChildrenResources(childrenResources);
    }

}
