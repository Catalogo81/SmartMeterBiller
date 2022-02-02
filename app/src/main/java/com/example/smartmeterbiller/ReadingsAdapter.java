package com.example.smartmeterbiller;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartmeterbiller.activities.CustomerProfile;
import com.example.smartmeterbiller.activities.DownloadImage;
import com.example.smartmeterbiller.activities.Readings;
import com.example.smartmeterbiller.classes.CapturedReadings;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.facebook.FacebookSdk.getApplicationContext;
import static java.lang.System.load;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ReadingsViewHolder> {

    private final List<CapturedReadings> capturedReadings;
    private final Context context;
    private OnItemClickListener mListener;
    String date, units, total, image;

    public ReadingsAdapter(Context context, List<CapturedReadings> capturedReadings, OnItemClickListener mListener) {
        this.context = context ;
        this.capturedReadings = capturedReadings;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ReadingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.elements, parent, false);

        return new ReadingsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingsViewHolder holder, final int position) {
        
        //Context context = holder.ivReadingImage.getContext();
        CapturedReadings capturedReadingsCurrent = capturedReadings.get(position);
        date = capturedReadingsCurrent.getCapturedDate();
        units = capturedReadingsCurrent.getCapturedReading();
        total = capturedReadingsCurrent.getTotalCost();
        image = capturedReadingsCurrent.getCapturedReadingUrl();

        holder.tvReadingDate.setText("Date Posted: " + date);
        holder.tvReadingUnits.setText("Units: " + units);
        holder.tvTotalCostInList.setText("Cost:\nR" + total);

        holder.ivDownloadReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Downloading image", Toast.LENGTH_SHORT).show();
                downloadFile(holder.tvReadingUnits.getContext(), ".pdf", DIRECTORY_DOWNLOADS, image);
            }
        });

//        cost = Double.parseDouble(capturedReadingsCurrent.getTotalCost());
//        if(cost != 0)
//            holder.tvTotalCostInList.setText("Cost R:" + df2.format(cost));

        //holder.tvTotalCostInList.setText("Cost: R" + cost);
        //Toast.makeText(context, "" + capturedReadingsCurrent.getCapturedReadingUrl(), Toast.LENGTH_SHORT).show();

        //ImageView : Loading the image with Glide Library
//        Glide.with(context)
//                .load(capturedReadings.get(position).getCapturedReadingUrl())
//                .into(holder.ivReadingImage);

        //ImageView : Loading the image with Picasso Library
//        if(capturedReadingsCurrent.getCapturedReadingDownloadLink()!=null && !capturedReadingsCurrent.getCapturedReadingDownloadLink().isEmpty())
//        {
//            Picasso.with(context)
//                    .load(capturedReadingsCurrent.getCapturedReadingUrl())
//                    .fit()
//                    .centerCrop()
//                    .into(holder.ivReadingImage);
//
//            Picasso.with(context).load(capturedReadingsCurrent.getCapturedReadingDownloadLink()).into(holder.ivReadingImage);
//        }
//        else
//        {
//            holder.ivReadingImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.readings));
//            Picasso.with(context).load(capturedReadingsCurrent.getCapturedReadingUrl()).into(holder.ivReadingImage);
//        }


        //Picasso.get().load(uri).into(ivReading);
    }

    public void downloadFile(Context context, /*String fileName,*/ String fileExtension, String destinationDirectory, String url)
    {
        DownloadManager downloadManager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, /*fileName + */fileExtension);

        downloadManager.enqueue(request);

    }


    @Override
    public int getItemCount() {
        return capturedReadings.size();
    }

    public class ReadingsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener
    {
        ImageView ivReadingImage, ivDownloadReading;
        TextView tvReadingDate, tvReadingUnits, tvTotalCostInList;

        public ReadingsViewHolder(@NonNull View itemView) {
            super(itemView);

            //ivReadingImage = itemView.findViewById(R.id.ivReadingImage);
            ivDownloadReading = itemView.findViewById(R.id.ivDownloadReading);
            tvReadingDate = itemView.findViewById(R.id.tvReadingDate);
            tvReadingUnits = itemView.findViewById(R.id.tvReadingUnits);
            tvTotalCostInList = itemView.findViewById(R.id.tvTotalCostInList);

            ivDownloadReading.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);


        }


        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, DownloadImage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("date", date);
            intent.putExtra("units", units);
            intent.putExtra("total", total);
            intent.putExtra("image", image);
            context.startActivity(intent);

            if(mListener != null)
            {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION)
                {
                    mListener.onItemClick(position);
                }
            }

            switch (v.getId()) {
                case R.id.ivDownloadReading:
                    //Toast.makeText(context, "Download Clicked", Toast.LENGTH_SHORT).show();
                    break;
//                case R.id.ivShowReport:
//                Intent intent = new Intent(getApplicationContext(), CustomerProfile.class);
//
//                //pass the users details to the next activity using an intent
//                intent.putExtra("name", name);
//                intent.putExtra("surname", surname);
//                intent.putExtra("email", emailAddress);
//                intent.putExtra("phoneNumber", phoneNumber);
//                intent.putExtra("homeAddress", homeAddress);
//                intent.putExtra("meterNumber", meterNumber);
//
//                startActivity(intent);
//                    break;
                default:
                    break;
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem view = menu.add(Menu.NONE, 1,1,"View");
            MenuItem delete = menu.add(Menu.NONE, 2,2,"Delete");

            view.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener != null)
            {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION)
                {
                    switch (item.getItemId())
                    {
                        case 1:
                            mListener.onWhatEverClicked(position);
                            return true;

                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }

    }

    public interface OnItemClickListener
    {
        void onItemClick(int position);

        void onWhatEverClicked(int position);

        void onDeleteClick(int position);

    }

    public  void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }
}
