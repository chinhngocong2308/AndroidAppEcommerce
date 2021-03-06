package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Model.Cart;
import com.example.ecommerce.Prevalent.Prevalent;
import com.example.ecommerce.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessBtn;
    private TextView textTotalAmount, txtMsg1;

    private int overTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn = (Button) findViewById(R.id.next_process_btn);
        textTotalAmount = (TextView) findViewById(R.id.total_price);
        txtMsg1 = (TextView) findViewById(R.id.msg1);

        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textTotalAmount.setText("T???ng ti???n: " + String.valueOf(overTotalPrice) + "??");

                Intent intent = new Intent(CartActivity.this,  ConfirmFinalOrderActivity.class);
                intent.putExtra("T???ng ti???n", overTotalPrice + "??");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        CheckOrderState();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        try {
            if (Prevalent.currentOnlineUser == null) {
                Toast.makeText(CartActivity.this, "Xin vui l??ng ????ng nh???p ????? mua h??ng!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getPhone()).child("Products"), Cart.class)
                        .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i, @NonNull Cart cart) {
                cartViewHolder.txtProductQuantity.setText(" S??? l?????ngx" + cart.getQuantity());
                cartViewHolder.txtProductName.setText( "T??n s???n ph???m:" + cart.getPname());
                cartViewHolder.txtProductPrice.setText("Gi?? :"+cart.getPrice() + "??");

                int oneTyprProductTPrice = ((Integer.valueOf(cart.getPrice()))) * Integer.valueOf(cart.getQuantity());
                overTotalPrice = overTotalPrice + oneTyprProductTPrice;

                cartViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "S???a",
                                        "X??a"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("B???n mu???n:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", cart.getPid());
                                    startActivity(intent);
                                }
                                if( i==1)
                                {
                                   try {
                                       cartListRef.child("User View")
                                               .child(Prevalent.currentOnlineUser.getPhone())
                                               .child("Products")
                                               .child(cart.getPid())
                                               .removeValue()
                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       if(task.isSuccessful())
                                                       {
                                                           Toast.makeText(CartActivity.this, "X??a s???n ph???m th??nh c??ng !", Toast.LENGTH_SHORT).show();
                                                           Intent intent = new Intent(CartActivity.this, CartActivity.class);
                                                           startActivity(intent);

                                                       }
                                                   }
                                               });
                                   }catch (Exception e) {
                                       e.printStackTrace();
                                   }
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;

            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckOrderState()
    {
        DatabaseReference ordersRef;
      try {
          ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
          ordersRef.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  if (snapshot.exists())
                  {
                      String shippingState = snapshot.child("state").getValue().toString();
                      String username = snapshot.child("name").getValue().toString();

                      if (shippingState.equals("??ang giao h??ng"))
                      {
                          textTotalAmount.setText("G???i" + username + "\n ????n h??ng ???? ???????c giao.");
                          txtMsg1.setVisibility(View.VISIBLE);
                          txtMsg1.setText("Xin c???m ??n! ????n h??ng c???a b???n ???? ???????c ?????t, h??y ti???p t???c ?????ng h??nh c??ng ch??ng t??i nh?? !");
                          recyclerView.setVisibility(View.GONE);
                          NextProcessBtn.setVisibility(View.GONE);

                          Toast.makeText(CartActivity.this, "B???n ch??? c?? th??? mua h??ng, khi ???? nh???n ???????c ????n h??ng cu???i c??ng c???a m??nh !", Toast.LENGTH_SHORT).show();
                      }
                      else if(shippingState.equals("Ch??a giao h??ng"))
                      {
                          textTotalAmount.setText("Xin vui l??ng ?????i. ????n h??ng c???a b???n ??ang ???????c x??? l??");
                          txtMsg1.setVisibility(View.VISIBLE);
                          recyclerView.setVisibility(View.GONE);
                          NextProcessBtn.setVisibility(View.GONE);

                          Toast.makeText(CartActivity.this, "B???n ch??? c?? th??? mua h??ng, khi ???? nh???n ???????c ????n h??ng cu???i c??ng c???a m??nh !", Toast.LENGTH_SHORT).show();
                      }
                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
      }catch (Exception e)
      {
          e.printStackTrace();
      }

    }
}