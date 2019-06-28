package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemOrders {

    public static <B extends DbBeanWithItemOrderInterface<B>> List<B> getRange(
            long firstItemOrder,
            long lastItemOrder,
            List<B> itemInventory)
    {
        boolean reversed = firstItemOrder > lastItemOrder;

        List<B> items = new ArrayList<B>(itemInventory);
        if (reversed)
            Collections.reverse(items);

        long currentItemOrder;
        if (reversed)
            currentItemOrder = Long.MAX_VALUE;
        else
            currentItemOrder = 0;

        List<B> itemsInRange = new ArrayList<B>();
        for (B item: items) {
            if (reversed) {
                if (currentItemOrder <= item.getItemOrder())
                    throw new IllegalArgumentException("Item list is not properly ordered");
            } else {
                if (currentItemOrder >= item.getItemOrder())
                    throw new IllegalArgumentException("Item list is not properly ordered");
            }
            currentItemOrder = item.getItemOrder();

            if (currentItemOrder >= firstItemOrder && currentItemOrder <= lastItemOrder)
                itemsInRange.add(item);
        }

        return itemsInRange;
    }

    public static <B extends DbBeanWithItemOrderInterface<B>> List<B> getRange(
            B firstItem,
            B lastItem,
            List<B> itemInventory)
    {
        return getRange(firstItem.getItemOrder(), lastItem.getItemOrder(), itemInventory);
    }
}
