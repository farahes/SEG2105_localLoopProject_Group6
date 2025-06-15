package com.example.localloopapp_android.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.localloopapp_android.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class CategoryViewModel extends ViewModel {

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("categories");

    /**
     * Returns a LiveData object containing the list of categories.
     * If the categories are not yet loaded, it fetches them from the database.
     *
     * @return LiveData containing the list of categories
     */
    public LiveData<List<Category>> getCategories() {
        if (categories.getValue() == null) {
            fetchCategories();
        }
        return categories;
    }

    /**
     * Fetches all categories from the Firebase database.
     * Updates the LiveData object with the list of categories.
     */
    public void fetchCategories() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Category> categoryList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Category c = child.getValue(Category.class);
                    categoryList.add(c);
                }
                categories.postValue(categoryList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("CategoryViewModel", "Failed to load categories", error.toException());
            }
        });
    }

    /**
     * Adds a new category to the database.
     * Automatically generates a unique ID for the category.
     *
     * @param category The category to add
     */
    public void addCategory(Category category) {
        String id = dbRef.push().getKey();
        category.setCategoryId(id);
        dbRef.child(id).setValue(category);
        fetchCategories(); // refresh
    }

    /**
     * Edits an existing category in the database.
     * The category must already have a valid ID.
     *
     * @param category The category with updated information
     */
    public void editCategory(Category category) {
        dbRef.child(category.getCategoryId()).setValue(category);
        fetchCategories(); // refresh
    }

    /**
     * Deletes a category from the database.
     * The category must have a valid ID.
     *
     * @param categoryId The ID of the category to delete
     */
    public void deleteCategory(String categoryId) {
        dbRef.child(categoryId).removeValue();
        fetchCategories(); // refresh
    }
}
