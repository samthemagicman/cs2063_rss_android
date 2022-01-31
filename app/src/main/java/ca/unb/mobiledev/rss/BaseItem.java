package ca.unb.mobiledev.rss;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;

public final class BaseItem implements Serializable, Comparable<BaseItem>
{
        String title = "";
        String link = "";
        String description = "";
        String bitmapLink = "";
        String publicationDate = "";
        Date dateTimestamp = new Date();
        double lat = 0.0;
        double lon = 0.0;
        String price = "";

        Bitmap bitmapImage = null;

        boolean showIndicator = true; // Indicator of new item

        @Override
        public int compareTo(BaseItem o)
        {
            boolean linkMatch = this.link.equals(o.link);
            if(linkMatch) return 0;
            else return 1;
        }

        public boolean isSameItem(BaseItem o)
        {
            return (this.link.equals(o.link));
        }

        public boolean itemHasBeenUpdatedOnline(BaseItem o) {
            boolean isSameItem = isSameItem(o);
            boolean hasPriceUpdate = !this.price.equals(o.price);
            boolean hasDateUpdate = (this.dateTimestamp.getTime() - o.dateTimestamp.getTime() != 0);

            return isSameItem && (hasPriceUpdate || hasDateUpdate);
        }
}
