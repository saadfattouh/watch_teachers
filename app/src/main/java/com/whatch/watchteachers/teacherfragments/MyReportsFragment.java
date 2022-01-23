package com.whatch.watchteachers.teacherfragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.whatch.watchteachers.R;
import com.whatch.watchteachers.adapters.ReportsAdapter;
import com.whatch.watchteachers.model.Report;
import com.whatch.watchteachers.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyReportsFragment extends Fragment {

    public static final String TAG = "myReportsFragment";
    ArrayList<Report> reports;
    RecyclerView reportsList;
    ReportsAdapter reportsAdapter;

    int myId;



    public MyReportsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getting teacher id to get all reports associated with him
        myId = SharedPrefManager.getInstance(requireContext()).getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportsList = view.findViewById(R.id.reports_list);

        getReportsByTeacherId();


    }

    void getReportsByTeacherId(){

        final ProgressDialog pDialog = new ProgressDialog(requireContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        reports = new ArrayList<>();


        AndroidNetworking.get("http://nawar.scit.co/oup/school-reports/api/reports/user.php?user_id=" + myId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                //getting the user from the response
                                JSONArray reportsArray = obj.getJSONArray("data");
                                Report report;
                                for(int i = 0; i < reportsArray.length(); i++){
                                    JSONObject reportObj = reportsArray.getJSONObject(i);
                                    report = new Report(
                                            Integer.parseInt(reportObj.getString("id")),
                                            Integer.parseInt(reportObj.getString("user_id")),
                                            reportObj.getString("user_name"),
                                            reportObj.getString("content"),
                                            reportObj.getString("created_at")
                                    );
                                    reports.add(report);
                                }

                                reportsAdapter = new ReportsAdapter(requireContext(), reports);
                                reportsList.setAdapter(reportsAdapter);
                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();

                        Toast.makeText(requireContext(), anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
}