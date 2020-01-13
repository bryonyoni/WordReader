package com.color.wordreader.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.color.wordreader.Models.Book;
import com.color.wordreader.Models.Word;
import com.color.wordreader.R;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int PICK_IMAGE = 6969;
    private int PICK_PDF = 1234;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        loadFile();
//        fn_permission();
    }


    private List<String> getPdfFilesFromDevice(Context context) {
        List<String> listOfDirectories = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Uri uriExternal = MediaStore.Files.getContentUri("external");
        Uri uriInternal = MediaStore.Files.getContentUri("internal");

        String[] projection = null;
        String sortOrder = null; // unordered

        // only pdf
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");

        String[] selectionArgsPdf = new String[]{mimeType};
        Cursor allPdfFilesCursor = cr.query(uriExternal, projection, selectionMimeType, selectionArgsPdf, sortOrder);

        if (allPdfFilesCursor != null && allPdfFilesCursor.getCount() != 0) {
            allPdfFilesCursor.moveToFirst();
            do {
                int dataColumn = allPdfFilesCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                String filePath=allPdfFilesCursor.getString(dataColumn);
                Log.e("MainActivity", "Loaded: "+filePath);
                listOfDirectories.add(filePath);
            } while (allPdfFilesCursor.moveToNext());
        }
        allPdfFilesCursor = cr.query(uriInternal, projection, selectionMimeType, selectionArgsPdf, sortOrder);

        if (allPdfFilesCursor != null && allPdfFilesCursor.getCount() != 0) {
            allPdfFilesCursor.moveToFirst();
            do {
                int dataColumn = allPdfFilesCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                String filePath=allPdfFilesCursor.getString(dataColumn);
                Log.e("MainActivity", "Loaded: "+filePath);
                listOfDirectories.add(filePath);
            } while (allPdfFilesCursor.moveToNext());
        }

        if (allPdfFilesCursor != null && !allPdfFilesCursor.isClosed()) {
            allPdfFilesCursor.close();
        }

        return listOfDirectories;
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
            }
        } else {
            List<String> myPdfs = getPdfFilesFromDevice(getApplicationContext());
            getTextFromFile(myPdfs.get(0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                List<String> myPdfs = getPdfFilesFromDevice(getApplicationContext());
                getTextFromFile(myPdfs.get(0));
            } else {
                Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    private Uri mFilepath;
    private void loadFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent,"Select PDF"), PICK_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (data.getData() != null) {
                mFilepath = data.getData();

                try {
                    TextView text = findViewById(R.id.text);
                    text.setText("Loading");

                    String selectedImagePath = getPath(mContext, mFilepath);
                    Log.e("MainActivity", "" + selectedImagePath);

                    loadSelectedPdfTask t = new loadSelectedPdfTask();
                    t.setData(selectedImagePath);
                    t.execute();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String getPath(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }




    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }




    private String getTextFromFile(String allPdfFiles){
        try {
            String parsedText="";
            PdfReader reader = new PdfReader(allPdfFiles);
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(reader, i+1).trim()+"\n"; //Extracting the content from the different pages
            }
            reader.close();
            return parsedText;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private class loadSelectedPdfTask extends AsyncTask<String, Void, String> {
        private String pdfBookData;
        private String selectedImagePath;
        private Bitmap bookImage;

        private void setData(String selectedImagePat){
            selectedImagePath = selectedImagePat;
        }


        @Override
        protected String doInBackground(String... strings) {
            File pdfFile = new File(selectedImagePath);
            bookImage = pdfToBitmap(pdfFile);
            pdfBookData = getTextFromFile(selectedImagePath);
            loadAllWordsAndSentences(pdfBookData);
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            Book newBook = new Book();
            newBook.setSentenceWords(allTheWordsForLoadedBook);
            newBook.setBookUrl(pdfBookData);

            ImageView image = findViewById(R.id.image);
            image.setImageBitmap(bookImage);
        }

    }

    private List<Word> allTheWordsForLoadedBook = new ArrayList<>();
    private String currentWord = "";

    private void loadAllWordsAndSentences(String everything){
        allTheWordsForLoadedBook = new ArrayList<>();

        currentWord = "";
        for (int i = 0; i < everything.length(); i++){
            char c = everything.charAt(i);

            if(c==' '){
                //the word is over,
                if(!currentWord.equals("")) {

                    Word word = new Word();
                    word.setWord(currentWord.trim());
                    allTheWordsForLoadedBook.add(word);
                }
                currentWord = "";
            }else{
                currentWord = String.format("%s%s", currentWord, Character.toString(c));
            }
        }

        Log.e("MainActivity", "We've got: "+ allTheWordsForLoadedBook.size());
    }


    private  Bitmap pdfToBitmap(File pdfFile) {
        Bitmap bitmap = null;

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            PdfRenderer.Page page = renderer.openPage(0);

            int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // close the page
            page.close();

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;

    }


}
