package com.runesync;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventLocation {
    private int x;
    private int y;
    private int plane;
    private int region;
}
