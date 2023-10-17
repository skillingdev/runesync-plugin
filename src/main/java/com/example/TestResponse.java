package com.example;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class TestResponse {
  @SerializedName("status_code")
  public int statusCode;
  public TestUsers items;
}
