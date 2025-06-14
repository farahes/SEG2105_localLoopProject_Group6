package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private CategoryViewModel categoryViewModel;
    private LinearLayout categoryListContainer;
    private Button btnAddCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        categoryListContainer = findViewById(R.id.categoryListContainer);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        btnAddCategory.setOnClickListener(v -> showAddOrEditDialog(null));

        categoryViewModel.getCategories().observe(this, this::displayCategories);

        categoryViewModel.fetchCategories();
    }

    private void displayCategories(List<Category> categories) {
        categoryListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Category category : categories) {
            View row = inflater.inflate(R.layout.item_category_admin, categoryListContainer, false);

            TextView tvName = row.findViewById(R.id.tvCategoryName);
            TextView tvDesc = row.findViewById(R.id.tvCategoryDesc);
            ImageView btnEdit = row.findViewById(R.id.ivEditCategory);
            ImageView btnDelete = row.findViewById(R.id.ivDeleteCategory);

            tvName.setText(category.getName());
            tvDesc.setText(category.getDescription());

            btnEdit.setOnClickListener(v -> showAddOrEditDialog(category));
            btnDelete.setOnClickListener(v -> confirmAndDeleteCategory(category));

            categoryListContainer.addView(row);
        }
    }

    private void showAddOrEditDialog(@Nullable Category categoryToEdit) {
        boolean isEdit = categoryToEdit != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Edit Category" : "Add Category");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null, false);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        EditText etDesc = dialogView.findViewById(R.id.etCategoryDesc);

        if (isEdit) {
            etName.setText(categoryToEdit.getName());
            etDesc.setText(categoryToEdit.getDescription());
        }

        builder.setView(dialogView);
        builder.setPositiveButton(isEdit ? "Save" : "Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                categoryToEdit.setName(name);
                categoryToEdit.setDescription(desc);
                categoryViewModel.editCategory(categoryToEdit);
            } else {
                Category newCategory = new Category(null, name, desc);
                categoryViewModel.addCategory(newCategory);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void confirmAndDeleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category?")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> categoryViewModel.deleteCategory(category.getCategoryId()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}