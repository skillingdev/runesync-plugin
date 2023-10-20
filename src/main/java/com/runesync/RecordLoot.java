package com.runesync;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecordLoot {
    private List<LootEntry> items;
}
