package com.comp.iitb.vialogue.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.adapters.CategoriesExpandableAdapter;
import com.comp.iitb.vialogue.adapters.ViewCategoryAdapter;
import com.comp.iitb.vialogue.coordinators.OnFragmentInteractionListener;
import com.comp.iitb.vialogue.models.Category;
import com.comp.iitb.vialogue.models.ParseObjects.models.CategoryType;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewVideos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewVideosCategory extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View mView;
    private JSONArray vidArray;
    private JSONArray catArray;
    private static String objectId;
    private OnFragmentInteractionListener mListener;
    List<Category> categoryList = new ArrayList<Category>();
    List<String> groupList = new ArrayList<String>();
    List<String> childList = new ArrayList<String>();
    Map<String, Category> laptopCollection;
    ExpandableListView expListView;
    RecyclerView recyclerView;
    ViewCategoryAdapter mAdapter;
    private int limit = 5;
    List<ParseObject> receiveEM;
    List<ParseObject> receivedVideos;
    private List<CategoryType> categoryTypeList = new ArrayList<>();
    public ViewVideosCategory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewVideos.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewVideosCategory newInstance(String objectId) {
        ViewVideosCategory.objectId =objectId;
        ViewVideosCategory fragment = new ViewVideosCategory();
        Bundle args = new Bundle();/*
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_view_videos, container, false);
        expListView = (ExpandableListView) mView.findViewById(R.id.video_list);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);

            expListView.setVisibility(View.VISIBLE);
            new GetCategories().execute("OK");


        //setGroupIndicatorToRight();

        return mView;
    }
/*
    private void prepareMovieData() {
        for(int i=0; i<5;i++) {
            CategoryType temp = new CategoryType(i,"name","desc", "");
            categoryTypeList.add(temp);
        }
    }*/

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void createGroupList() {
        for(Category i :categoryList)
            groupList.add(i.getName());
    }

    private void createCollection() {
        laptopCollection = new LinkedHashMap<String, Category>();
        for (Category i : categoryList) {
            childList.add(i.getDesc());

            laptopCollection.put(i.getName(), i);
        }

    }
    private void loadChild(String[] laptopModels) {
        childList = new ArrayList<String>();
        for (String model : laptopModels)
            childList.add(model);
    }

    private class GetCategoryType extends AsyncTask<String, Void, String> {
        ProgressDialog pd;
        @Override
        protected String doInBackground(String... params) {

            try {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
                        "Category");
                query.orderByAscending("createdAt");
                query.setLimit(limit += 5);
                receiveEM = query.find();
                for (ParseObject num : receiveEM) {
                    String id=((String) num.get("ids"));
                    String name=((String) num.get("name"));
                    String desc=((String) num.get("desc"));
                    String imageURL=((String) num.get("imageURL"));

                    CategoryType map = new CategoryType(id, name, desc, imageURL);
                    categoryTypeList.add(map);
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }


            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();

            mAdapter = new ViewCategoryAdapter(categoryTypeList,getActivity());
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(mView.getContext(), 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onPreExecute() {

            pd=new ProgressDialog(getContext());
            pd.setMessage("Loading bru!");
            pd.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





    private class GetCategories extends AsyncTask<String, Void, String> {
        ProgressDialog videoPD;
        @Override
        protected String doInBackground(String... params) {
            try{
                ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Category");
                innerQuery.whereEqualTo("objectId",objectId);
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Project");
                query.whereMatchesQuery("category",innerQuery);

                query.orderByAscending("createdAt");
                receiveEM = query.find();

                for (ParseObject num : receiveEM) {
                    Category map = new Category();
                    map.setId((String) num.getObjectId());
                    Log.e("----id",""+(String) num.getObjectId());
                    map.setName((String) num.get("name"));
                    map.setDesc((String) num.get("description"));
                    /*ParseFile video = num.getParseFile("project_video");
                    map.setImageURL(video.getUrl());*/
                    categoryList.add(map);
                }
        } catch (ParseException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
            videoPD.dismiss();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            final CategoriesExpandableAdapter expListAdapter = new CategoriesExpandableAdapter(getActivity(), categoryList);
            expListView.setAdapter(expListAdapter);

        }

        @Override
        protected void onPreExecute() {

            videoPD=new ProgressDialog(getContext());
            videoPD.setMessage("Loading bru!");
            videoPD.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





}
