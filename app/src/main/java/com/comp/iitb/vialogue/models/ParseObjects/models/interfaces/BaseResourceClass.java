package com.comp.iitb.vialogue.models.ParseObjects.models.interfaces;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.comp.iitb.vialogue.models.ParseObjects.models.Slide;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.BaseParseClass;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ironstein on 16/02/17.
 */

public abstract class BaseResourceClass extends BaseParseClass {

    private static final class Fields {
        public static final String

        FILE = "file",
        TEMP_URL = "temp_url";
    }

    public static File makeTempResourceFile(Slide.ResourceType resourceType, Context context) {
        String extension;
        File storageDirectory;

        if(resourceType == Slide.ResourceType.AUDIO) {
            extension = "wav";
        } else if(resourceType == Slide.ResourceType.IMAGE) {
            extension = "png";
        } else if(resourceType == Slide.ResourceType.VIDEO) {
            extension = "mp4";
        } else {
            extension = null;
        }

        storageDirectory = new File(context.getFilesDir(), extension);
        if(!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = extension + "_" + timeStamp + "_";
        File a = null;
        try {
            a = File.createTempFile(
                    imageFileName,      /* prefix */
                    extension,          /* suffix */
                    storageDirectory    /* directory */
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return a;
    }

    // default constructor required by Parse
    // DO NOT USE THIS CONSTRUCTOR (ONLY FOR USE BY PARSE)
    // USE THE OTHER CONSTRUCTOR THAT REQUIRES PARAMETERS DURING
    // INSTANTIATING THE OBJECT
    public BaseResourceClass() {}

    public BaseResourceClass(Uri uri) {
        setUri(uri);
    }

    public Uri getUri() {
        Uri uri = null;
        try {
            uri = Uri.fromFile(getParseFile(Fields.FILE).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(uri == null) {
            uri =  Uri.fromFile(new File(getString(Fields.TEMP_URL)));
        }
        return uri;
    }

    public File getResourceFile() {
        return new File(getUri().getPath());
    }

    public void setUri(Uri uri) {
        put(Fields.TEMP_URL, new File(uri.getPath()).getAbsolutePath());
    }

    private ParseFile getFile() {
        return getParseFile(Fields.FILE);
    }

    private void setFile(ParseFile file) {
        put(Fields.FILE, file);
    }

//    public Uri getUri() {
//        return Uri.fromFile(new File(getFile().getUrl()));
//    }
}
