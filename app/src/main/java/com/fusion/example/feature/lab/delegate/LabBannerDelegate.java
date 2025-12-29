package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemBannerPageBinding;
import com.fusion.example.databinding.ItemLabBannerM3Binding;
import com.fusion.example.feature.lab.model.BannerItem;
import com.fusion.example.feature.lab.model.LabBanner;
import com.fusion.example.feature.lab.widget.BannerContainer;
import com.fusion.example.utils.CoilHelperKt;

import java.util.Collections;
import java.util.List;

public class LabBannerDelegate extends JavaDelegate<LabBanner, ItemLabBannerM3Binding> {

    @Nullable
    @Override
    public Object getUniqueKey(@NonNull LabBanner item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemLabBannerM3Binding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        ItemLabBannerM3Binding binding = ItemLabBannerM3Binding.inflate(inflater, parent, false);
        
        // Setup ViewPager visual effects
        binding.viewPager.setOffscreenPageLimit(1);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(16)); // Space between pages
        // Scale effect
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        binding.viewPager.setPageTransformer(transformer);
        
        return binding;
    }

    @Override
    protected void onBind(@NonNull ItemLabBannerM3Binding binding, @NonNull LabBanner item) {
        BannerPagerAdapter adapter;
        if (binding.viewPager.getAdapter() instanceof BannerPagerAdapter) {
            adapter = (BannerPagerAdapter) binding.viewPager.getAdapter();
        } else {
            adapter = new BannerPagerAdapter();
            binding.viewPager.setAdapter(adapter);
            
            // Attach to container for auto-scroll and touch handling
            if (binding.getRoot() instanceof BannerContainer) {
                ((BannerContainer) binding.getRoot()).attachViewPager(binding.viewPager);
            }
        }
        
        adapter.setItems(item.getItems());
        
        // Initialize position to middle for infinite scrolling effect (only if not already set)
        if (item.getItems().size() > 1 && binding.viewPager.getCurrentItem() < 100) {
            int mid = Integer.MAX_VALUE / 2;
            int startPos = mid - (mid % item.getItems().size());
            binding.viewPager.setCurrentItem(startPos, false);
        }
    }

    private static class BannerPagerAdapter extends RecyclerView.Adapter<BannerViewHolder> {
        private List<BannerItem> items = Collections.emptyList();

        public void setItems(List<BannerItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BannerViewHolder(ItemBannerPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            if (items.isEmpty()) return;
            holder.bind(items.get(position % items.size()));
        }

        @Override
        public int getItemCount() {
            return items.size() > 1 ? Integer.MAX_VALUE : items.size();
        }
    }

    private static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ItemBannerPageBinding binding;

        public BannerViewHolder(@NonNull ItemBannerPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BannerItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.ivImage.clearColorFilter();
            CoilHelperKt.loadUrl(binding.ivImage, item.getImageUrl(), false);
            
            binding.getRoot().setOnClickListener(v -> 
                Toast.makeText(v.getContext(), "Clicked Banner: " + item.getTitle(), Toast.LENGTH_SHORT).show()
            );
        }
    }
}