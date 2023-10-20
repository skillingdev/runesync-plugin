package com.runesync;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LootEntry {
    private String timestamp;
    private String accountHash;
    private String itemName;
    private int itemId;
    private String sourceName;
    private int sourceLevel;
    private EventLocation location;
}
