package com.example.onlineshopping;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.onlineshopping.Model.Products;
import com.example.onlineshopping.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class ProductDetailsActivity extends AppCompatActivity {
    private Button addToCartButton;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productID = "", state = "Normal";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_product_details);
        productID = getIntent ().getStringExtra ("pid");
        addToCartButton = (Button) addToCartButton.findViewById ();
        numberButton = numberButton.findViewById ();
        productImage = (ImageView) productImage.findViewById ();
        productName = productName.findViewById ();
        productDescription = productDescription.findViewById ();
        productPrice = productPrice.findViewById ();
        getProductDetails (productID);
        addToCartButton.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View view) {

                if (state.equals ("Order Placed") || state.equals ("Order Shipped")) {
                    Toast.makeText (ProductDetailsActivity.this, "You can add Purchase more product, once your order is shipped or confirmed", Toast.LENGTH_LONG).show ();
                } else {
                    addingToCartList ();
                }
            }
        });
    }

    public void setContentView (int activity_product_details) {
    }

    /**
     * @return
     */
    public Intent getIntent ( ) {
    }

    @Override
    protected void onStart ( ) {
        super.onStart ();
        CheckOrderState ();
    }

    private void addingToCartList ( ) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calForDate = Calendar.getInstance ();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat ("MMM dd. yyy");
        saveCurrentDate = currentDate.format (calForDate.getTime ());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat ("HH:mm:ss a");
        saveCurrentTime = currentDate.format (calForDate.getTime ());
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance ().getReference ().child ("Cart List");
        final HashMap<String, Object> cartMap = new HashMap<> ();
        cartMap.put ("pid", productID);
        cartMap.put ("pname", productName.getText ().toString ());
        cartMap.put ("price", productPrice.getText ().toString ());
        cartMap.put ("date", saveCurrentDate);
        cartMap.put ("time", saveCurrentTime);
        cartMap.put ("quantity", numberButton.getNumber ());
        cartMap.put ("discount", "");

        cartListRef.child ("User view").child (Prevalent.currentOnlineUser.getPhone ()).child ("Products").child (productID).updateChildren (cartMap).addOnCompleteListener (new OnCompleteListener<Void> () {
            @Override
            public void onComplete (@NonNull Task<Void> task) {
                if (task.isSuccessful ()) {
                    cartListRef.child ("Admin view").child (Prevalent.currentOnlineUser.getPhone ())
                            .child ("Products").child (productID)
                            .updateChildren (cartMap)
                            .addOnCompleteListener (new OnCompleteListener<Void> () {
                                @Override
                                public void onComplete (@NonNull Task<Void> task) {
                                    if (task.isSuccessful ()) {
                                        Toast.makeText (ProductDetailsActivity.this, "Added to cart List", Toast.LENGTH_SHORT).show ();
                                        Intent intent = new Intent (ProductDetailsActivity.this, HomeActivity.class);
                                        startActivity (intent);
                                    }
                                }
                            });
                }
            }
        });


    }

    public void startActivity (Intent intent) {
    }

    private void getProductDetails (String productID) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance ().getReference ().child ("Products");
        productsRef.child (productID).addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists ()) {
                    Products products = dataSnapshot.getValue (Products.class);
                    assert products != null;
                    productName.setText (products.getPname ());
                    productPrice.setText (products.getPrice ());
                    productDescription.setText (products.getDescription ());
                    Picasso.get ().load (products.getImage ()).into (productImage);

                }
            }

            @Override
            public void onCancelled (DatabaseError databaseError) {

            }
        });
    }

    //

    private void CheckOrderState ( ) {
        DatabaseReference ordersRef;
        ordersRef = FirebaseDatabase.getInstance ().getReference ().child ("Orders").child (Prevalent.currentOnlineUser.getPhone ());
        ordersRef.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists ()) {
                    String shippingState = Objects.requireNonNull (dataSnapshot.child ("state").getValue ()).toString ();
                    if (shippingState.equals ("Shipped")) {
                        state = "Order Shipped";
                    } else if (shippingState.equals ("Not Shipped")) {
                        state = "Order Placed";
                    }
                }
            }

            @Override
            public void onCancelled (DatabaseError databaseError) {

            }
        });
    }


}