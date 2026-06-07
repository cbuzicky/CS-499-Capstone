package com.zybooks.c499_buzicky_cheryl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class InventoryTrackingActivity extends AppCompatActivity {

    private InventoryTrackingAdapter adapter;
        private ArrayList<InventoryTracking> trackingList;
        private InventoryDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_tracking_grid);

        db = new InventoryDatabase(this);

        RecyclerView recyclerView = findViewById(R.id.inventory_tracking_grid);

        Button pdfBtn = findViewById(R.id.pdf_button);

        trackingList = new ArrayList<>();

        adapter = new InventoryTrackingAdapter(this, trackingList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTrackingData();

        // Creates and downloads the pdf of the Inventory Tracking Report
        pdfBtn.setOnClickListener(v -> createPdf());

        // Returns use to the Inventory List screen
        Button backBtn = findViewById(R.id.back_to_inventory);

        backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadTrackingData() {
        trackingList.clear();

        ArrayList<InventoryTracking> data = db.getAllTrackingRecords();

        trackingList.addAll(data);
        adapter.notifyDataSetChanged();
    }

        @Override
        protected void onResume() {
            super.onResume();
            loadTrackingData();
        }

    private void createPdf() {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint linePaint = new Paint();

        linePaint.setStrokeWidth(2f);

        int pageWidth = 1080;
        int pageHeight = 1920;

        PdfDocument.PageInfo pageInfo;

        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        PdfDocument.Page page;
        page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float xStart = 50;
        float y = 80;
        float rowHeight = 50;

        // Column positions to align these with the item information
        float col1 = 50;   // Name
        float col2 = 200;  // SKU
        float col3 = 350;  // Change
        float col4 = 550;  // Old Value
        float col5 = 700;  // New Value
        float col6 = 900;  // Date

        // Title
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("Inventory Tracking Report", xStart, 50, paint);

        paint.setTextSize(14f);
        paint.setFakeBoldText(true);

        // Draw header row
        canvas.drawText("Name", col1, y, paint);
        canvas.drawText("SKU", col2, y, paint);
        canvas.drawText("Change", col3, y, paint);
        canvas.drawText("Old Value", col4, y, paint);
        canvas.drawText("New Value", col5, y, paint);
        canvas.drawText("Date", col6, y, paint);

        // Header line
        canvas.drawLine(xStart, y + 10, pageWidth - 50, y + 10, linePaint);

        y += rowHeight;
        paint.setFakeBoldText(false);

        // Populate data from the Inventory Tracking table into the PDF document
        for (InventoryTracking item : trackingList) {

            canvas.drawText(item.getName(), col1, y, paint);
            canvas.drawText(item.getSku(), col2, y, paint);
            canvas.drawText(item.getChangeType(), col3, y, paint);
            canvas.drawText(item.getOldValue(), col4, y, paint);
            canvas.drawText(item.getNewValue(), col5, y, paint);
            canvas.drawText(item.getTimestamp(), col6, y, paint);

            // Row separator line
            canvas.drawLine(xStart, y + 10, pageWidth - 50, y + 10, linePaint);

            y += rowHeight;

        }

        pdfDocument.finishPage(page);

        savePdfToDownloads(pdfDocument);

        pdfDocument.close();
    }

    // Save PDF to Downloads folder
    private void savePdfToDownloads(PdfDocument pdfDocument) {

        String fileName = "Inventory_Tracking_Report.pdf";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream out = resolver.openOutputStream(uri)) {
                    pdfDocument.writeTo(out);
                    Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: PDF not saved", Toast.LENGTH_SHORT).show();
                }
            }

        } else {

            File downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File file = new File(downloadsDir, fileName);

            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
