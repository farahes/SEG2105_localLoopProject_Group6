<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"  
    <!-- Ensures content stretches to fill view -->
    android:background="#ffffff"> 
    <!-- optional: for visual clarity -->

    <LinearLayout
        android:orientation="vertical"
        android:padding="16dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <!-- Static welcome message -->
        <TextView
            android:id="@+id/tvWelcomeMessage"
            android:text="Welcome!"
            android:textSize="18sp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:paddingBottom="16dp"/>

        <!-- Dynamic user list -->
        <LinearLayout
            android:id="@+id/userListContainer"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
    </LinearLayout>
</ScrollView>
