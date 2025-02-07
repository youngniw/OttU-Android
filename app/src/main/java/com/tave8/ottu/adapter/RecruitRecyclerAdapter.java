package com.tave8.ottu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.tave8.ottu.LoginActivity;
import com.tave8.ottu.OttURetrofitClient;
import com.tave8.ottu.PreferenceManager;
import com.tave8.ottu.R;
import com.tave8.ottu.RecruitActivity;
import com.tave8.ottu.data.Genre;
import com.tave8.ottu.data.RatePlanInfo;
import com.tave8.ottu.data.RecruitInfo;
import com.tave8.ottu.data.RecruitRequestInfo;
import com.tave8.ottu.data.SingletonPlatform;
import com.tave8.ottu.data.UserEssentialInfo;
import com.tave8.ottu.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tave8.ottu.MainActivity.myInfo;

public class RecruitRecyclerAdapter extends RecyclerView.Adapter<RecruitRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<RecruitInfo> recruitPostList;

    public RecruitRecyclerAdapter(ArrayList<RecruitInfo> recruitPostList) {
        this.recruitPostList = recruitPostList;
    }

    @NonNull
    @Override
    public RecruitRecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_recruit, parent, false);
        RecruitRecyclerAdapter.ItemViewHolder viewHolder = new RecruitRecyclerAdapter.ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecruitRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.tvWriterNick.setText(recruitPostList.get(position).getWriterInfo().getNick());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        holder.tvCreatedDate.setText(recruitPostList.get(position).getRecruitDateTime().format(dateTimeFormatter));
        if (recruitPostList.get(position).isCompleted()) {
            holder.llitem.setBackgroundColor(context.getColor(R.color.bg_black_color1_4));
            holder.tvRecruitIng.setVisibility(View.GONE);
            holder.tvRecruitCompleted.setVisibility(View.VISIBLE);
            holder.tvChoiceNum.setTextColor(context.getColor(R.color.sub_text_color));
            holder.tvSlash.setTextColor(context.getColor(R.color.sub_text_color));
            holder.tvHeadCount.setTextColor(context.getColor(R.color.sub_text_color));
        }
        else {
            holder.llitem.setBackgroundColor(context.getColor(R.color.bg_black_color2_4));
            holder.tvRecruitIng.setVisibility(View.VISIBLE);
            holder.tvRecruitCompleted.setVisibility(View.GONE);
            holder.tvChoiceNum.setTextColor(context.getColor(R.color.white));
            holder.tvSlash.setTextColor(context.getColor(R.color.main_color));
            holder.tvHeadCount.setTextColor(context.getColor(R.color.main_color));
        }
        holder.tvChoiceNum.setText(String.valueOf(recruitPostList.get(position).getChoiceNum()));
        holder.tvHeadCount.setText(String.valueOf(recruitPostList.get(position).getHeadCount()));

        if (recruitPostList.get(position).isApplying())
            holder.tvParticipated.setVisibility(View.VISIBLE);
        else
            holder.tvParticipated.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return recruitPostList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llitem;
        TextView tvWriterNick, tvCreatedDate, tvRecruitIng, tvRecruitCompleted, tvChoiceNum, tvSlash, tvHeadCount, tvParticipated;

        //참여 요청 다이얼로그용
        ArrayList<RecruitRequestInfo> userRequestList;
        AppCompatButton btRequestYes;
        AppCompatButton btRequestConfirmed;
        RecruitRequestRecyclerAdapter recruitRequestRecyclerAdapter;
        TextView tvRequestNum;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            llitem = itemView.findViewById(R.id.ll_item_recruit_bg);
            tvWriterNick = itemView.findViewById(R.id.tv_item_recruit_nick);
            tvCreatedDate = itemView.findViewById(R.id.tv_item_recruit_date);
            tvRecruitIng = itemView.findViewById(R.id.tv_item_recruit_ing);
            tvRecruitCompleted = itemView.findViewById(R.id.tv_item_recruit_completed);
            tvChoiceNum = itemView.findViewById(R.id.tv_item_recruit_choice);
            tvSlash = itemView.findViewById(R.id.tv_item_recruit_slash);
            tvHeadCount = itemView.findViewById(R.id.tv_item_recruit_headcount);
            tvParticipated = itemView.findViewById(R.id.tv_item_recruit_participated);

            userRequestList = new ArrayList<>();
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Long recruitIdx = recruitPostList.get(pos).getRecruitIdx();

                    if (myInfo.getUserIdx().equals(recruitPostList.get(pos).getWriterInfo().getUserIdx())) {      //작성자이므로 참여 수락을 할 수 있는 다이얼로그 보임
                        View requestDialogView = View.inflate(context, R.layout.dialog_recruit_request, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(requestDialogView);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
                        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                        Point size = new Point();
                        display.getRealSize(size);
                        int width = size.x;
                        params.width = (int) (width*0.89);
                        alertDialog.getWindow().setAttributes(params);

                        tvRequestNum = requestDialogView.findViewById(R.id.tv_dialog_request_num);
                        tvRequestNum.setText(String.valueOf(recruitPostList.get(pos).getChoiceNum()));

                        TextView tvRequestFullNum = requestDialogView.findViewById(R.id.tv_dialog_request_full_num);
                        tvRequestFullNum.setText(String.valueOf(recruitPostList.get(pos).getHeadCount()));

                        btRequestYes = requestDialogView.findViewById(R.id.bt_dialog_request_yes);
                        btRequestConfirmed = requestDialogView.findViewById(R.id.bt_dialog_request_confirmed);
                        AppCompatButton btRequestDelete = requestDialogView.findViewById(R.id.bt_dialog_request_delete);

                        RecyclerView rvRecruitRequest = requestDialogView.findViewById(R.id.rv_dialog_request);
                        if (userRequestList.size() > 3) {
                            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics());
                            LinearLayout.LayoutParams rvRecruitRequestParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                            rvRecruitRequest.setLayoutParams(rvRecruitRequestParams);

                        }
                        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false);
                        rvRecruitRequest.setLayoutManager(manager);
                        recruitRequestRecyclerAdapter = new RecruitRequestRecyclerAdapter(userRequestList, this, recruitPostList.get(pos));
                        rvRecruitRequest.setAdapter(recruitRequestRecyclerAdapter);

                        //참여 정보 받아옴
                        updateRequestInfo(recruitIdx, recruitPostList.get(pos).getHeadCount());
                        alertDialog.show();

                        btRequestYes.setOnClickListener(view -> alertDialog.dismiss());
                        btRequestConfirmed.setOnClickListener(view -> {
                            alertDialog.dismiss();

                            View teamDialogView = View.inflate(context, R.layout.dialog_confirmed_team, null);

                            AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                            builder2.setView(teamDialogView);
                            AlertDialog alertDialogTeam = builder2.create();
                            alertDialogTeam.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            WindowManager.LayoutParams params2 = alertDialogTeam.getWindow().getAttributes();
                            Display display2 = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                            Point size2 = new Point();
                            display2.getRealSize(size2);
                            int width2 = size2.x;
                            params2.width = (int) (width2*0.89);
                            alertDialogTeam.getWindow().setAttributes(params2);

                            ArrayList<UserInfo> recruitMemberList = new ArrayList<>();
                            RecyclerView rvRecruitMembers = teamDialogView.findViewById(R.id.rv_dialog_confirmed_members);
                            LinearLayoutManager teamManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false);
                            rvRecruitMembers.setLayoutManager(teamManager);
                            TeamMemberRecyclerAdapter teamMemberRecyclerAdapter = new TeamMemberRecyclerAdapter(recruitMemberList);
                            rvRecruitMembers.setAdapter(teamMemberRecyclerAdapter);

                            //수락된 멤버들의 정보를 조회
                            alertDialogTeam.show();
                            updateTeamMemberInfo(recruitIdx, recruitMemberList, teamMemberRecyclerAdapter, alertDialogTeam);

                            EditText etPaymentDay = teamDialogView.findViewById(R.id.et_dialog_confirmed_team_day);
                            etPaymentDay.setOnClickListener(viewTeam -> {
                                final NumberPicker dayPick = new NumberPicker(context);
                                dayPick.setMinValue(1);
                                dayPick.setMaxValue(30);
                                if (etPaymentDay.getText().toString().equals(""))
                                    dayPick.setValue(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                else
                                    dayPick.setValue(Integer.parseInt(etPaymentDay.getText().toString()));

                                androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
                                dialog.setTitle("결제일 입력");
                                dialog.setView(dayPick);
                                dialog.setPositiveButton("확인", (dialogInterface, which) -> etPaymentDay.setText(String.valueOf(dayPick.getValue())));
                                dialog.setNegativeButton("취소", (dialogInterface, which) -> dialogInterface.dismiss());
                                dialog.show();
                            });

                            AppCompatImageButton btCancel = teamDialogView.findViewById(R.id.ibt_dialog_confirmed_team_cancel);
                            btCancel.setOnClickListener(viewTeam -> alertDialogTeam.dismiss());

                            AppCompatButton btSubmit = teamDialogView.findViewById(R.id.bt_dialog_confirmed_team_submit);
                            btSubmit.setOnClickListener(viewTeam -> {
                                if (etPaymentDay.getText().length() == 0) {
                                    Toast.makeText(context, "결제일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    JsonObject requestData = new JsonObject();
                                    requestData.addProperty("recruitIdx", recruitIdx);
                                    requestData.addProperty("userIdx", myInfo.getUserIdx());
                                    requestData.addProperty("paymentDay", etPaymentDay.getText().toString());
                                    OttURetrofitClient.getApiService().postRecruitTeam(PreferenceManager.getString(context, "jwt"), requestData).enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.code() == 201) {
                                                alertDialogTeam.dismiss();
                                                Toast.makeText(context, "Ott 서비스 팀 생성에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                                ((RecruitActivity) context).updateRecruitList(false);
                                            }
                                            else if (response.code() == 400) {
                                                Toast.makeText(context, "이전에 이미 팀이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                                                alertDialogTeam.dismiss();
                                            }
                                            else if (response.code() == 403) {
                                                Toast.makeText(context, "모집 확정 후 7일이 지났습니다.", Toast.LENGTH_SHORT).show();
                                                alertDialogTeam.dismiss();
                                            }
                                            else if (response.code() == 401) {
                                                Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                                PreferenceManager.removeKey(context, "jwt");
                                                Intent reLogin = new Intent(context, LoginActivity.class);
                                                reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                context.startActivity(reLogin);
                                                ((RecruitActivity) context).finish();
                                            }
                                            else
                                                Toast.makeText(context, "팀 확정에 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                            Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요:)", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        });

                        btRequestDelete.setOnClickListener(view -> {
                            OttURetrofitClient.getApiService().deleteRecruit(PreferenceManager.getString(context, "jwt"), recruitIdx).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.code() == 200) {
                                        alertDialog.dismiss();
                                        Toast.makeText(context, "모집글 삭제에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                        ((RecruitActivity) context).updateRecruitList(false);
                                    }
                                    else if (response.code() == 401) {
                                        Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                        PreferenceManager.removeKey(context, "jwt");
                                        Intent reLogin = new Intent(context, LoginActivity.class);
                                        reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        context.startActivity(reLogin);
                                        ((RecruitActivity) context).finish();
                                    }
                                    else
                                        Toast.makeText(context, "해당 모집글 삭제에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                    else if (!recruitPostList.get(pos).isApplying() && !recruitPostList.get(pos).isCompleted()) {      //참여 다이얼로그가 뜸(참여 완료가 아닐 시에)
                        View participateDialogView = View.inflate(context, R.layout.dialog_recruit_participate, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(participateDialogView);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog.show();

                        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
                        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                        Point size = new Point();
                        display.getRealSize(size);
                        int width = size.x;
                        params.width = (int) (width*0.89);
                        alertDialog.getWindow().setAttributes(params);

                        int headcount = recruitPostList.get(pos).getHeadCount();
                        RatePlanInfo ratePlanInfo = SingletonPlatform.getPlatform().getPlatformRatePlanInfo(recruitPostList.get(pos).getPlatformIdx(), headcount);

                        TextView tvRecruiter = participateDialogView.findViewById(R.id.tv_dialog_participate_recruiter);
                        tvRecruiter.setText(recruitPostList.get(pos).getWriterInfo().getNick());

                        DecimalFormat chargeFormatter = new DecimalFormat("###,##0");
                        TextView tvRatePlan = participateDialogView.findViewById(R.id.tv_dialog_participate_rateplan);
                        tvRatePlan.setText(ratePlanInfo.getRatePlanName());
                        TextView tvRatePlanCharge = participateDialogView.findViewById(R.id.tv_dialog_participate_charge);
                        tvRatePlanCharge.setText(chargeFormatter.format(ratePlanInfo.getCharge()));

                        TextView tvTeamNum = participateDialogView.findViewById(R.id.tv_dialog_participate_team_num);
                        tvTeamNum.setText(String.valueOf(headcount));
                        TextView tvMyCharge = participateDialogView.findViewById(R.id.tv_dialog_participate_my_charge);
                        int myCharge = ratePlanInfo.getCharge()/(headcount);
                        tvMyCharge.setText(chargeFormatter.format(myCharge));

                        AppCompatButton btParticipateYes = participateDialogView.findViewById(R.id.bt_dialog_participate_yes);
                        btParticipateYes.setOnClickListener(view -> {
                            JsonObject requestData = new JsonObject();
                            requestData.addProperty("recruitIdx", recruitIdx);
                            requestData.addProperty("userIdx", myInfo.getUserIdx());
                            OttURetrofitClient.getApiService().postRecruitParticipate(PreferenceManager.getString(context, "jwt"), requestData).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.code() == 200) {
                                        alertDialog.dismiss();
                                        Toast.makeText(context, "모집글 참여 요청이 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                        ((RecruitActivity) context).updateRecruitList(false);
                                    }
                                    else if (response.code() == 401) {
                                        Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                        PreferenceManager.removeKey(context, "jwt");
                                        Intent reLogin = new Intent(context, LoginActivity.class);
                                        reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        context.startActivity(reLogin);
                                        ((RecruitActivity) context).finish();
                                    }
                                    else
                                        Toast.makeText(context, "해당 모집글 참여 요청에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        AppCompatButton btParticipateNo = participateDialogView.findViewById(R.id.bt_dialog_participate_no);
                        btParticipateNo.setOnClickListener(view -> alertDialog.dismiss());
                    }
                }
            });

            tvWriterNick.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    View profileDialogView = View.inflate(context, R.layout.dialog_user_profile, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(profileDialogView);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
                    Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    Point size = new Point();
                    display.getRealSize(size);
                    int width = size.x;
                    params.width = (int) (width*0.89);
                    alertDialog.getWindow().setAttributes(params);

                    AppCompatImageButton ibtCancel = profileDialogView.findViewById(R.id.ibt_dialog_profile_cancel);
                    ibtCancel.setOnClickListener(view -> alertDialog.dismiss());

                    TextView tvUserNick = profileDialogView.findViewById(R.id.tv_dialog_profile_nick);
                    TextView tvOttULevel = profileDialogView.findViewById(R.id.tv_dialog_profile_level);
                    ProgressBar pbOttULevel = profileDialogView.findViewById(R.id.pb_dialog_profile);
                    AppCompatButton btGenre1 = profileDialogView.findViewById(R.id.bt_dialog_profile_genre1);
                    AppCompatButton btGenre2 = profileDialogView.findViewById(R.id.bt_dialog_profile_genre2);
                    AppCompatButton btGenre3 = profileDialogView.findViewById(R.id.bt_dialog_profile_genre3);

                    Long userIdx = recruitPostList.get(pos).getWriterInfo().getUserIdx();
                    OttURetrofitClient.getApiService().getUser(PreferenceManager.getString(context, "jwt"), userIdx).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.code() == 200) {
                                try {
                                    JSONObject userInfo = new JSONObject(Objects.requireNonNull(response.body()));
                                    JSONObject user = userInfo.getJSONObject("user");

                                    ArrayList<Genre> interestGenreList = new ArrayList<>();
                                    JSONArray genres = user.getJSONArray("genres");
                                    for (int i=0; i<genres.length(); i++) {
                                        JSONObject genre = genres.getJSONObject(i);
                                        interestGenreList.add(new Genre(genre.getInt("genreIdx"), genre.getString("genreName")));
                                    }
                                    UserInfo otherUserInfo = new UserInfo(userIdx, user.getString("nickname"), user.getInt("reliability"),
                                            user.getBoolean("isFirst"), interestGenreList);

                                    tvUserNick.setText(otherUserInfo.getNick());

                                    pbOttULevel.setProgress(otherUserInfo.getReliability());
                                    tvOttULevel.setText(String.valueOf(otherUserInfo.getReliability()));
                                    if (otherUserInfo.isFirst()) {
                                        pbOttULevel.setProgressDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_progress_first));
                                        tvOttULevel.setTextColor(context.getColor(R.color.sub_text_color));
                                    }

                                    if (otherUserInfo.getInterestGenre().size() == 1) {
                                        btGenre1.setText(otherUserInfo.getInterestGenre().get(0).getGenreName());
                                        btGenre2.setVisibility(View.INVISIBLE);
                                        btGenre3.setVisibility(View.INVISIBLE);
                                    } else if (otherUserInfo.getInterestGenre().size() == 2) {
                                        btGenre1.setText(otherUserInfo.getInterestGenre().get(0).getGenreName());
                                        btGenre2.setText(otherUserInfo.getInterestGenre().get(1).getGenreName());
                                        btGenre3.setVisibility(View.INVISIBLE);
                                    } else if (otherUserInfo.getInterestGenre().size() == 3) {
                                        btGenre1.setText(otherUserInfo.getInterestGenre().get(0).getGenreName());
                                        btGenre2.setText(otherUserInfo.getInterestGenre().get(1).getGenreName());
                                        btGenre3.setText(otherUserInfo.getInterestGenre().get(2).getGenreName());
                                    }
                                    alertDialog.show();
                                } catch (JSONException e) { e.printStackTrace(); }
                            }
                            else if (response.code() == 401) {
                                Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                PreferenceManager.removeKey(context, "jwt");
                                Intent reLogin = new Intent(context, LoginActivity.class);
                                reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(reLogin);
                                ((RecruitActivity) context).finish();
                            }
                            else
                                Toast.makeText(context, "회원 정보 불러오기에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        public void updateRequestInfo(Long recruitIdx, int headcount) {
            OttURetrofitClient.getApiService().getRecruitWaitlist(PreferenceManager.getString(context, "jwt"), recruitIdx).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200) {
                        userRequestList.clear();
                        try {
                            JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                            int choiceNum = (int) result.getLong("choiceNum");
                            tvRequestNum.setText(String.valueOf(choiceNum));

                            JSONArray jsonWaitlist = result.getJSONArray("waitlist");
                            for (int i=0; i<jsonWaitlist.length(); i++) {
                                JSONObject waiting = jsonWaitlist.getJSONObject(i);
                                Long waitlistIdx = waiting.getLong("waitlistIdx");
                                JSONObject user = waiting.getJSONObject("user");
                                Long userIdx = user.getLong("userIdx");
                                String nickname = user.getString("nickname");
                                boolean isAccepted = waiting.getBoolean("isAccepted");
                                userRequestList.add(new RecruitRequestInfo(waitlistIdx, new UserEssentialInfo(userIdx, nickname), isAccepted));
                            }

                            if (choiceNum == headcount && !result.getBoolean("timeout") && !result.getBoolean("isTeam")) {
                                btRequestYes.setVisibility(View.GONE);
                                btRequestConfirmed.setVisibility(View.VISIBLE);
                            }

                            recruitRequestRecyclerAdapter.notifyDataSetChanged();
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else if (response.code() == 401) {
                        Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.removeKey(context, "jwt");
                        Intent reLogin = new Intent(context, LoginActivity.class);
                        reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(reLogin);
                        ((RecruitActivity) context).finish();
                    }
                    else
                        Toast.makeText(context, "해당 모집글 참여 정보 불러오기에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateTeamMemberInfo(Long recruitIdx, ArrayList<UserInfo> recruitMemberList, TeamMemberRecyclerAdapter teamMemberRecyclerAdapter, AlertDialog alertDialogTeam) {
            OttURetrofitClient.getApiService().getRecruitMembers(PreferenceManager.getString(context, "jwt"), recruitIdx).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200) {
                        try {
                            JSONObject result = new JSONObject(Objects.requireNonNull(response.body()));
                            JSONArray members = result.getJSONArray("members");
                            for (int i=0; i<members.length(); i++) {
                                JSONObject waitingMember = members.getJSONObject(i);
                                JSONObject member = waitingMember.getJSONObject("user");
                                Long userIdx = member.getLong("userIdx");
                                String nickname = member.getString("nickname");
                                String kakaotalkId = member.getString("kakaotalkId");
                                recruitMemberList.add(new UserInfo(userIdx, nickname, kakaotalkId));
                            }

                            teamMemberRecyclerAdapter.notifyDataSetChanged();
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    else if (response.code() == 400) {
                        alertDialogTeam.dismiss();
                        Toast.makeText(context, "모집 인원 수와 수락 인원 수가 같지 않습니다. 확인 부탁드립니다.", Toast.LENGTH_SHORT).show();
                    }
                    else if (response.code() == 401) {
                        Toast.makeText(context, "로그인 기한이 만료되어\n 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.removeKey(context, "jwt");
                        Intent reLogin = new Intent(context, LoginActivity.class);
                        reLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(reLogin);
                        ((RecruitActivity) context).finish();
                    }
                    else
                        Toast.makeText(context, "모집글 팀원 정보 불러오기에 문제가 생겼습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    alertDialogTeam.dismiss();
                    Toast.makeText(context, "서버와 연결되지 않았습니다. 확인해 주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
