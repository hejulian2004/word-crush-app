package com.example.wordcrush.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wordcrush.Activity.GameRecordActivity;
import com.example.wordcrush.Activity.LoginActivity;
import com.example.wordcrush.R;
import com.example.wordcrush.Tools.Tools;


public class HomeFragment extends Fragment implements View.OnClickListener{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.logoutButton).setOnClickListener(this);
        view.findViewById(R.id.gameRecordBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logoutButton){
            if(getContext() != null){
                getContext().deleteFile("userFile.txt");
            }
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            if(getActivity() != null){
                getActivity().finish();
            }
        } else if(v.getId() == R.id.gameRecordBtn){
            Intent intent = new Intent(getActivity(), GameRecordActivity.class);
            startActivity(intent);
        }
    }
}
