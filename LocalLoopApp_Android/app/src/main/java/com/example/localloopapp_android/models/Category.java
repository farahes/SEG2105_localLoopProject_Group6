package com.example.localloopapp_android;

public class Category {
    private String categoryId;  // Firebase key
    private String name;
    private String description;

    public Category(){}
    public Category(String categoryId, String name, String description){
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public String getCategoryId(){return categoryId;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public void setCategoryId(String categoryId){this.categoryId = categoryId;}
    public void setName(String name){this.name = name;}
    public void setDescription(String description){this.description = description;}

    public String toString() {
        return "com.example.localloopapp_android.Category{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
