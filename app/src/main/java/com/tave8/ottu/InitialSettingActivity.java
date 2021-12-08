package com.tave8.ottu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.tave8.ottu.data.Genre;
import com.tave8.ottu.data.UserInfo;

import java.util.ArrayList;
import java.util.HashSet;

import static com.tave8.ottu.MainActivity.myInfo;

public class InitialSettingActivity extends AppCompatActivity {
    private boolean isCheckedNick = false;
    private HashSet<View> selectedGenre = null;

    private AppCompatButton btGenre1, btGenre2, btGenre3, btGenre4, btGenre5, btGenre6, btGenre7, btGenre8, btGenre9, btGenre10, btGenre11, btGenre12;
    private EditText etNick, etKakaoId;
    private TextView tvNickInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setting);

        selectedGenre = new HashSet<>();

        etNick = findViewById(R.id.et_initial_nick);
        tvNickInfo = findViewById(R.id.tv_initial_nick_info);
        etKakaoId = findViewById(R.id.et_initial_kakaoid);
        btGenre1 = findViewById(R.id.bt_initial_genre1);
        btGenre2 = findViewById(R.id.bt_initial_genre2);
        btGenre3 = findViewById(R.id.bt_initial_genre3);
        btGenre4 = findViewById(R.id.bt_initial_genre4);
        btGenre5 = findViewById(R.id.bt_initial_genre5);
        btGenre6 = findViewById(R.id.bt_initial_genre6);
        btGenre7 = findViewById(R.id.bt_initial_genre7);
        btGenre8 = findViewById(R.id.bt_initial_genre8);
        btGenre9 = findViewById(R.id.bt_initial_genre9);
        btGenre10 = findViewById(R.id.bt_initial_genre10);
        btGenre11 = findViewById(R.id.bt_initial_genre11);
        btGenre12 = findViewById(R.id.bt_initial_genre12);

        initialTextChangedListener();
        initialClickListener();
    }

    private void initialTextChangedListener() {
        etNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isCheckedNick = false;
                tvNickInfo.setVisibility(View.GONE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void initialClickListener() {
        AppCompatButton btCheckNick = findViewById(R.id.bt_initial_check);
        btCheckNick.setOnClickListener(v -> {
            if (etNick.getText().toString().trim().length() != 0) {
                //TODO: 서버에 전달
                isCheckedNick = true;
                tvNickInfo.setVisibility(View.VISIBLE);

                etNick.clearFocus();
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etNick.getWindowToken(), 0);
            }
        });

        View.OnClickListener genreClickListener = v -> {
            etNick.clearFocus();
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etNick.getWindowToken(), 0);
            etKakaoId.clearFocus();
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etKakaoId.getWindowToken(), 0);

            if (selectedGenre.contains(v)) {
                selectedGenre.remove(v);
                v.setBackgroundResource(R.drawable.bg_button_non_select);
            } else if (selectedGenre.size() == 3) {
                Toast.makeText(InitialSettingActivity.this, "선택할 수 있는 관심 장르의 개수는 최대 3개입니다.", Toast.LENGTH_SHORT).show();
            } else {
                selectedGenre.add(v);
                v.setBackgroundResource(R.drawable.bg_button_select);
            }
        };
        btGenre1.setOnClickListener(genreClickListener);
        btGenre2.setOnClickListener(genreClickListener);
        btGenre3.setOnClickListener(genreClickListener);
        btGenre4.setOnClickListener(genreClickListener);
        btGenre5.setOnClickListener(genreClickListener);
        btGenre6.setOnClickListener(genreClickListener);
        btGenre7.setOnClickListener(genreClickListener);
        btGenre8.setOnClickListener(genreClickListener);
        btGenre9.setOnClickListener(genreClickListener);
        btGenre10.setOnClickListener(genreClickListener);
        btGenre11.setOnClickListener(genreClickListener);
        btGenre12.setOnClickListener(genreClickListener);

        Button btSubmit = findViewById(R.id.bt_initial_submit);
        btSubmit.setOnClickListener(v -> {
            if (!isCheckedNick) {
                etNick.requestFocus();
                Toast.makeText(this, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(etNick, 0);
            } else if (etKakaoId.getText().toString().trim().length() == 0) {
                etKakaoId.requestFocus();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(etKakaoId, 0);
            } else if (selectedGenre.isEmpty()) {
                Toast.makeText(this, "관심 장르를 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
            } else {
                //TODO: 서버에 관심장르와 닉네임 전달
                //TODO: 예시
                ArrayList<Genre> interestGenreList = new ArrayList<>();
                interestGenreList.add(Genre.CRIME);
                interestGenreList.add(Genre.FANTASY);
                myInfo = new UserInfo(1L, "young@naver.com", "youngeun", "영은", 10, true, interestGenreList);

                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}
