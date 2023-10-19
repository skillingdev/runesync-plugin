package com.runesync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
class SetupUser {
  public String accountHash;
  public String displayName;
}
