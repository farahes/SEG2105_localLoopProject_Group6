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

    public LiveData<List<Category>> getCategories() {
        if (categories.getValue() == null) {
            fetchCategories();
        }
        return categories;
    }

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

    public void addCategory(Category category) {
        String id = dbRef.push().getKey();
        category.setCategoryId(id);
        dbRef.child(id).setValue(category);
        fetchCategories(); // refresh
    }

    public void deleteCategory(String categoryId) {
        dbRef.child(categoryId).removeValue();
        fetchCategories(); // refresh
    }
}
