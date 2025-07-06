package com.example.localloopapp_android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap; // Required for image compression
import android.graphics.BitmapFactory; // Checks image dimensions
import android.net.Uri; // A string that identifies a resource
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream; // Compresses bitmaps to byte[]
import java.io.InputStream; // Image byte reading

/**
 * Manager for picking, validating, compressing, and uploading user avatars.
 */
public class AccountAvatarManager {

    public interface Callback {
        void onUploadSuccess(Uri downloadUrl);
        void onUploadFailure(Exception e);
    }

    private static final long MAX_FILE_BYTES = 1_000_000; // 1MB
    private static final int MIN_DIMENSION = 32; // px
    private static final int MAX_DIMENSION = 512; // px

    private final Context context;
    private final StorageReference storageRoot;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Callback callback;

    public AccountAvatarManager(Activity hostActivity) {
        this.context = hostActivity;
        this.storageRoot = FirebaseStorage.getInstance().getReference("avatars");
        // uncomment when fixed
        //setupImagePicker(hostActivity);
    }

    /**
     * TODO: Call to register the ActivityResult callback.
     */
    // uncomment when fixed
    /**
     private void setupImagePicker(Activity activity) {
     pickImageLauncher = activity.registerForActivityResult(
     new ActivityResultContracts.StartActivityForResult(),
     result -> {
     if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null)
     return;
     Uri imageUri = result.getData().getData();
     handlePickedImage(imageUri);
     }
     );
     }
     */

    /**
     * Launches the system picker for images.
     */
    public void pickAvatarImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        pickImageLauncher.launch(i);
    }

    /**
     * Validate dimensions and file size, then upload or toast an error.
     */
    private void handlePickedImage(Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, opts);

            int width  = opts.outWidth;
            int height = opts.outHeight;
            if (width < MIN_DIMENSION || height < MIN_DIMENSION ||
                    width > MAX_DIMENSION || height > MAX_DIMENSION) {
                Toast.makeText(context,
                        "Image must be between 32 by 32 and 512 by 512 pixels",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(context,
                    "Failed to read image dimensions",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-open stream to actually get the bytes
        try (InputStream in2 = context.getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(in2);
            byte[] data = compressToSize(bmp, MAX_FILE_BYTES);
            if (data == null) {
                Toast.makeText(context,
                        "Could not compress below 1MB",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            uploadBytes(data);
        } catch (Exception e) {
            Toast.makeText(context,
                    "Failed to load image",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Compresses the given bitmap to JPEG under maxBytes, or returns null.
     */
    private byte[] compressToSize(Bitmap bmp, long maxBytes) {
        int quality = 90;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);

        while (out.size() > maxBytes && quality > 10) {
            out.reset();
            quality -= 10;
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }
        return out.size() <= maxBytes ? out.toByteArray() : null;
    }

    /**
     * Uploads to Firebase Storage under a UUID path, then notifies via callback.
     */
    private void uploadBytes(byte[] data) {
        String key = java.util.UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storageRoot.child(key);
        UploadTask task = ref.putBytes(data);
        task.addOnSuccessListener(snapshot ->
                ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            if (callback != null) callback.onUploadSuccess(uri);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onUploadFailure(e);
                        })
        ).addOnFailureListener(e -> {
            if (callback != null) callback.onUploadFailure(e);
        });
    }

    /**
     * TODO: Set a callback to be notified when upload finishes.
     */
    public void setCallback(Callback cb) { // temporary unfinished
        //this.callback = cb;
        // uncomment when fixed
    }
}